package com.renthouses.enviroment.buttons;

import com.renthouses.enviroment.util.RowUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DateButton extends Button {

    private final RowUtil rowUtil;
    @Override
    public EditMessageText editMessage(Update update) {
        //todo Сделать bold шрифт
        //todo Дополнить message

        String chatId = update.getCallbackQuery().getMessage().getChatId().toString();
        int messageId = update.getCallbackQuery().getMessage().getMessageId();
        String message =
                "Выберите количество дней на которое вы хотите заехать.";
        String[] callback = update.getCallbackQuery().getData().split("#");
        int quantityOfDays = Integer.parseInt(callback[1]);
        String dateTime = callback[2];
        String colorId = callback[3];


        return EditMessageText
                .builder()
                .chatId(chatId)
                .messageId(messageId)
                .text(message)
                .replyMarkup(rowUtil.createRowsDateButton(quantityOfDays, dateTime, colorId))
                .build();
    }

    @Override
    public boolean support(String cmd) {
        //todo Написать pattern
        return cmd.startsWith("DateMessage");
    }
}
