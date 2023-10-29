package com.renthouses.enviroment.services;

import com.renthouses.enviroment.buttons.Button;
import com.renthouses.enviroment.messages.Message;
import com.renthouses.enviroment.properties.TelegramProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import javax.annotation.PostConstruct;
import java.text.ParseException;
import java.util.List;

/**
 * Telegram adapter.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class Telegram extends TelegramLongPollingBot {

    private final TelegramProperties properties;
    private final List<Message> messages;
    private final List<Button> buttons;

    @PostConstruct
    private void init() {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(this);
        } catch (TelegramApiException e) {
            throw new RuntimeException("Unable to register telegram bot", e);
        }
    }

    @Override
    public void onUpdateReceived(Update update) {

        if(update.hasMessage()) {
            SendMessage sendMessage = null;
            try {
                sendMessage = messages.stream()
                        .filter(i -> i.support(update.getMessage().getText()))
                        .findFirst()
                        .get()
                        .sendMessage(update);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

            try {
                execute(sendMessage);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }

        if (update.hasCallbackQuery()) {

            EditMessageText sendMessage = buttons.stream()
                    .filter(i -> i.support(update.getCallbackQuery().getData()))
                    .findFirst()
                    .get()
                    .editMessage(update);

            try {
                execute(sendMessage);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void onUpdatesReceived(List<Update> updates) {
        updates.forEach(this::onUpdateReceived);
    }

    @Override
    public String getBotToken() {
        return properties.getBotToken();
    }

    @Override
    public String getBotUsername() {
        return properties.getBotUserName();
    }

}
