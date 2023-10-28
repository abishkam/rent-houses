package com.renthouses.enviroment.services;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.renthouses.enviroment.dto.FreeDateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class CalendarService {

    private final CalendarApiConfiguration cac;
    String calendarId = "7c5f9b9d6dfbae0b42a428dd0162ad10cde6214cb84b3a0cb87fa9f2bd23a436@group.calendar.google.com";

    public void book(String date, int quantityOfDays) throws GeneralSecurityException, IOException {

        Event event = new Event();

        DateTime dateToBook = new DateTime(date);
        org.joda.time.DateTime dateToBookJoda = new org.joda.time.DateTime(dateToBook.getValue());
        EventDateTime start = new EventDateTime()
                .setDateTime(dateToBook)
                .setTimeZone("Europe/Moscow");
        event.setStart(start);

        org.joda.time.DateTime endJoda = new org.joda.time.DateTime(dateToBook.getValue());
        endJoda = endJoda.plusDays(quantityOfDays);
        endJoda = endJoda.minusHours(3);

        DateTime lastDate = new DateTime(endJoda.getMillis());
        EventDateTime end = new EventDateTime()
                .setDateTime(lastDate)
                .setTimeZone("Europe/Moscow");
        event.setEnd(end);

        endJoda = endJoda.plusDays(27);
        org.joda.time.DateTime endJoda2 = new org.joda.time.DateTime(endJoda);

        List<Integer> allColors = new ArrayList<>(List.of(new Integer[]{11, 8, 5}));

        Map<Integer, List<org.joda.time.DateTime>> collectTime = cac.getService().events().list(calendarId)
                .setTimeMin(dateToBook)
                .setMaxResults(20)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute()
                .getItems()
                .stream()
                .collect(Collectors.groupingBy(
                        i -> Integer.valueOf(i.getColorId()),
                        Collectors.mapping(i -> new org.joda.time.DateTime(i.getStart().getDateTime().getValue()), Collectors.toList()))
                );

        Map<Integer, org.joda.time.DateTime> startTime = collectTime.entrySet()
                .stream()
                .filter(i -> i.getValue().get(0).getMillis() == start.getDateTime().getValue())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        i -> i.getValue().get(0)
                ));

        Map<Integer, org.joda.time.DateTime> endTime = collectTime.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        i -> {
                            if(i.getValue().get(0).getMillis() == start.getDateTime().getValue()){
                                if(i.getValue().size()>1){
                                    return i.getValue().get(1);
                                } else {
                                    return endJoda2;
                                }
                            } else {
                                return i.getValue().get(0);
                            }
                        }
                ));

        for (int c: allColors) {
            endTime.putIfAbsent(c, endJoda2);
        }

        Integer first = endTime.entrySet()
                .stream()
                .filter(i -> !startTime.containsKey(i.getKey()))
                .filter(i -> i.getValue().isAfter(dateToBookJoda.minusHours(3).plusDays(quantityOfDays)))
                .map(Map.Entry::getKey)
                .findFirst()
                .get();

        cac.getService().events().insert(calendarId, new Event()
                .setStart(start)
                .setEnd(end)
                .setColorId(String.valueOf(first))).execute();
    }

    public FreeDateDto getFreeDate(String date) throws GeneralSecurityException, IOException {

        //todo process port io exception

        if(LocalDate.parse(date).isBefore(LocalDate.now())){
            return FreeDateDto.builder()
                    .message("Напишите актуальную дату")
                    .isFree(false)
                    .build();
        }

        DateTime dateToBook = new DateTime(date+"T14:00:00+03:00");

        org.joda.time.DateTime dateTime = new org.joda.time.DateTime(dateToBook.getValue());
        dateTime = dateTime.plusMonths(1);
        DateTime lastDate = new DateTime(dateTime.plusMonths(1).getMillis());
        EventDateTime eventlastDate = new EventDateTime();
        eventlastDate.setDateTime(lastDate);

        List<Integer> allColors = new ArrayList<>(List.of(new Integer[]{11, 8, 5}));
        Map<Integer, List<EventDateTime>> startTime = cac.getService().events().list(calendarId)
                .setTimeMin(dateToBook)
                .setTimeMax(lastDate)//+30 дней
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute()
                .getItems()
                .stream()
                .collect(Collectors.groupingBy(
                        i -> Integer.parseInt(i.getColorId()),
                        Collectors.mapping(Event::getStart, Collectors.toList())
                ));


        if(startTime.size()>0 && startTime
                .values()
                .stream()
                .allMatch(i ->i.get(0).getDateTime().equals(dateToBook))){

            return FreeDateDto.builder()
                    .message("На эту дату все домики заняты")
                    .isFree(false)
                    .build();
        }

        for (int c: allColors) {
            startTime.putIfAbsent(c, Collections.singletonList(eventlastDate));
        }

        FreeDateDto freeDateDto = startTime
                .entrySet()
                .stream()
                .filter(eventDateTimes -> !(eventDateTimes.getValue().get(0).getDateTime().equals(dateToBook)))
                .max((a, b) -> (int) (a.getValue().get(0).getDateTime().getValue() - b.getValue().get(0).getDateTime().getValue()))
                .map(i -> {
                    long dateValue = i.getValue().get(0).getDateTime().getValue();
                    return FreeDateDto.builder()
                            //todo починить выбор варианта
                            .message("Домик свободный до "
                                    + new DateTime(dateValue).toString().substring(0, 10)
                                    + "\nВыберите количество дней")
                            .colorId(i.getKey())
                            .startDate(new org.joda.time.DateTime(dateToBook.getValue()))
                            .endDate(new org.joda.time.DateTime(dateValue))
                            .isFree(true)
                            .build();
                })
                .get();


        return freeDateDto;
    }
}
