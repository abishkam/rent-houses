package com.renthouses.enviroment.buttons;

import com.renthouses.enviroment.dto.FreeDateDto;
import com.renthouses.enviroment.services.CalendarService;
import com.renthouses.enviroment.util.RowUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class DateMessageButton extends Button{

    private final CalendarService calendarService;
    private final RowUtil rowUtil;

    @Override
    public EditMessageText editMessage(Update update) {
        String chatId = update.getCallbackQuery().getMessage().getChatId().toString();
        String startDate = update.getCallbackQuery().getData().split("#")[1];

        try {
            FreeDateDto dto =
                    calendarService.getFreeDate(startDate.substring(0,10));

            if(dto.isFree()){
                return EditMessageText.builder()
                        .chatId(chatId)
                        .messageId(update.getCallbackQuery().getMessage().getMessageId())
                        .text(dto.getMessage())
                        .replyMarkup(rowUtil.createRowsDateMessage(dto))
                        .build();

            } else {
                return EditMessageText.builder()
                        .chatId(chatId)
                        .messageId(update.getCallbackQuery().getMessage().getMessageId())
                        .text(dto.getMessage())
                        .replyMarkup(rowUtil.createRowsDateMessage(dto))
                        .build();
            }

        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean support(String cmd) {
        return cmd.startsWith("DateMessageButton");
    }
}
