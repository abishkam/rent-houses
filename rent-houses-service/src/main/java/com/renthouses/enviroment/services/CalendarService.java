package com.renthouses.enviroment.services;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.renthouses.enviroment.confifuration.CalendarApiConfiguration;
import com.renthouses.enviroment.dto.FreeDateDto;
import com.renthouses.enviroment.dto.Pair;
import lombok.RequiredArgsConstructor;
import org.joda.time.DateTimeConstants;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class CalendarService {

    private final CalendarApiConfiguration cac;

    public void book(String date, int quantityOfDays) throws GeneralSecurityException, IOException {

        DateTime dateToBook = new DateTime(date);
        org.joda.time.DateTime dateToBookJoda = new org.joda.time.DateTime(dateToBook.getValue());
        EventDateTime start = createEdt(dateToBook);

        org.joda.time.DateTime endJoda = dateToBookJoda
                .plusDays(quantityOfDays)
                .minusHours(3);
        DateTime lastDate = new DateTime(endJoda.getMillis());
        EventDateTime end = createEdt(lastDate);
        endJoda = endJoda.plusDays(27);
        final org.joda.time.DateTime finalEndJoda = endJoda;

        List<Integer> allColors = new ArrayList<>(List.of(new Integer[]{11, 8, 5}));

        Map<Integer, List<org.joda.time.DateTime>> collectTime = cac.getService().events().list(cac.getCalendarId())
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
                                    return finalEndJoda;
                                }
                            } else {
                                return i.getValue().get(0);
                            }
                        }
                ));

        for (int c: allColors) {
            endTime.putIfAbsent(c, endJoda);
        }

        Integer first = endTime.entrySet()
                .stream()
                .filter(i -> !startTime.containsKey(i.getKey()))
                .filter(i -> i.getValue().isAfter(dateToBookJoda.minusHours(3).plusDays(quantityOfDays)))
                .map(Map.Entry::getKey)
                .findFirst()
                .get();

        Optional<Integer> second = endTime.entrySet()
                .stream()
                .filter(i -> !startTime.containsKey(i.getKey()))
                .filter(i -> i.getValue().isEqual(dateToBookJoda.plusDays(quantityOfDays)))
                .map(Map.Entry::getKey)
                .findFirst();

        second.ifPresentOrElse(
                i -> commitCng(start, end, i),
                () -> {
                    commitCng(start, end, first);
                }
        );

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

        org.joda.time.DateTime dateToBookJoda= new org.joda.time.DateTime(dateToBook.getValue());
        DateTime lastDate = new DateTime(dateToBookJoda.plusMonths(1).getMillis());

        List<Integer> allColors = new ArrayList<>(List.of(new Integer[]{11, 8, 5}));
        Map<Integer, Pair<org.joda.time.DateTime>> startTime = cac.getService().events().list(cac.getCalendarId())
                .setTimeMin(dateToBook)
                .setTimeMax(lastDate)//+30 дней
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute()
                .getItems()
                .stream()
                .collect(Collectors.toMap(
                        i -> Integer.parseInt(i.getColorId()),
                        i -> new Pair<org.joda.time.DateTime>(
                                        new org.joda.time.DateTime(i.getStart().getDateTime().getValue()),
                                        new org.joda.time.DateTime(i.getEnd().getDateTime().getValue())
                                ),
                                (o,n) -> o
                        )
                        );

        for (int c: allColors) {
            startTime.putIfAbsent(c, new Pair<org.joda.time.DateTime>(dateToBookJoda.plusMonths(1), dateToBookJoda.plusMonths(1)));
        }

        if(startTime.size()>0 && startTime
                .values()
                .stream()
                .allMatch(i ->
                        ((i.getFirst().isBefore(dateToBookJoda) || i.getFirst().isEqual(dateToBookJoda))
                     && i.getSecond().isAfter(dateToBookJoda) || (i.getFirst().isEqual(dateToBookJoda))
                        ))){

            return FreeDateDto.builder()
                    .message("На эту дату все домики заняты")
                    .startDate(dateToBookJoda)
                    .isFree(false)
                    .build();
        }

        FreeDateDto freeDateDto = startTime
                .entrySet()
                .stream()
                //Убираем те домики, которые уже забронированы на этот день
                .filter(eventDateTimes -> eventDateTimes.getValue().getFirst().isAfter(dateToBook.getValue()))
                //Вычисляем до какого крайнего числа можно забронировать домик
                .max(Comparator.comparing(a -> a.getValue().getFirst()))
                .map(i -> {
                    long dateValue = i.getValue().getFirst().getMillis();
                    return FreeDateDto.builder()
                            //todo починить выбор варианта
                            .message("Домик свободный до "
                                    + new DateTime(dateValue).toString().substring(0, 10)
                                    + "\nВыберите количество дней")
                            .colorId(i.getKey())
                            .startDate(dateToBookJoda)
                            .endDate(new org.joda.time.DateTime(dateValue))
                            .isFree(true)
                            .build();
                })
                .get();

        return freeDateDto;
    }

    public List<FreeDateDto> getFreeDateForAWeek(org.joda.time.DateTime date) throws GeneralSecurityException, IOException {

        List<FreeDateDto> list = new ArrayList<>();

        for (int i = date.getDayOfWeek(); i <= date.withDayOfWeek(DateTimeConstants.SUNDAY).getDayOfWeek(); i++) {
            list.add(getFreeDate(date.withDayOfWeek(i).toString().substring(0, 10)));
        }

        return list;

    }

    private void commitCng(EventDateTime start, EventDateTime end, int color) {
        try {
            cac.getService().events().insert(cac.getCalendarId(), new Event()
                    .setStart(start)
                    .setEnd(end)
                    .setColorId(String.valueOf(color))).execute();
        } catch (IOException | GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    private EventDateTime createEdt(DateTime dateTime) {
        return new EventDateTime()
                .setDateTime(dateTime)
                .setTimeZone("Europe/Moscow");
    }

}
