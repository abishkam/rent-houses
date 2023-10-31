package com.renthouses.enviroment.util;

import com.renthouses.enviroment.dto.FreeDateDto;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Util for working with UI telegram.
 */
@Component
public class RowUtil {

    public InlineKeyboardMarkup createRowsDateMessage(FreeDateDto dto) {
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
                    .callbackData("DateButton#"
                            + c[i]+"#"
                            +dto.getStartDate()+"#"
                            +dto.getColorId())
                    .build());
            quantityOfDays-=10;
        }
        markupLine.setKeyboard(Collections.singletonList(rows));

        return markupLine;
    }

    public InlineKeyboardMarkup createRowsDateButton(int quantityOfDays, String dateTime, String colorId) {
        int closestMultipleOfTen = quantityOfDays%10==0? quantityOfDays-9:((quantityOfDays / 10) * 10)+1;

        InlineKeyboardMarkup markupLine = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> rows1 = new ArrayList<>();
        List<InlineKeyboardButton> rows2 = new ArrayList<>();
        List<List<InlineKeyboardButton>> keyboards = new ArrayList<>();

        IntStream.rangeClosed(closestMultipleOfTen, quantityOfDays)
                .sorted()
                .forEach(i -> {
                    if (i%10<=5 && i%10!=0) {

                        rows1.add(InlineKeyboardButton.builder()
                                .text("" + i)
                                .callbackData("DateButton#" + i + "#" + dateTime + "#" + colorId)
                                .build());
                    } else {

                        rows2.add(InlineKeyboardButton.builder()
                                .text("" + i)
                                .callbackData("DateButton#" + i + "#" + dateTime + "#" + colorId)
                                .build());
                    }
                });

        keyboards.add(rows1);
        keyboards.add(rows2);
        markupLine.setKeyboard(keyboards);

        return markupLine;
    }

//    public InlineKeyboardMarkup getBackRow() {
//        InlineKeyboardMarkup markupLine = new InlineKeyboardMarkup();
//        List<InlineKeyboardButton> rows1 = new ArrayList<>();
//        List<InlineKeyboardButton> rows2 = new ArrayList<>();
//        List<List<InlineKeyboardButton>> keyboards = new ArrayList<>();
//
//
//    }
}
