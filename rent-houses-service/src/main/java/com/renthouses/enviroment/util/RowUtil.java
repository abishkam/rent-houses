package com.renthouses.enviroment.util;

import com.renthouses.enviroment.dto.FreeDateDto;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Util for working with UI telegram.
 */
@Component
public class RowUtil {

    public InlineKeyboardMarkup createRows(FreeDateDto dto) {
        // Вычисляем разницу между датами в миллисекундах
        long differenceInMillis = dto.getEndDate().getMillis() - dto.getStartDate().getMillis();

        // Конвертируем разницу в дни
        int quantityOfDays = (int) Math.min(differenceInMillis / (24 * 60 * 60 * 1000), 30);

        int nCol = (int) Math.ceil(quantityOfDays/10.0);
        int[] c = new int[nCol+1];//оставляю 1-й элемент равным 0

        InlineKeyboardMarkup markupLine = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> rows = new ArrayList<>();

        for (int i = 1; i < c.length; i++) {

            c[i] =c[i-1] + Math.min(quantityOfDays, 10);
            rows.add(InlineKeyboardButton.builder()
                    .text("до " + c[i])
                    .callbackData("DateMessage#"
                            + c[i]+"#"
                            +dto.getStartDate()+"#"
                            +dto.getColorId())
                    .build());
            quantityOfDays-=10;
        }
        markupLine.setKeyboard(Collections.singletonList(rows));

        return markupLine;
    }
}
