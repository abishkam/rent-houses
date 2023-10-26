package com.renthouses.enviroment.services;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.renthouses.enviroment.dto.FreeDateDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CalendarApiConfiguration {

    /**
     * Application name.
     */
    private static final String APPLICATION_NAME = "Google Calendar API Java Quickstart";
    /**
     * Global instance of the JSON factory.
     */
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    /**
     * Directory to store authorization tokens for this application.
     */
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES =
            Collections.singletonList(CalendarScopes.CALENDAR);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
    String calendarId = "7c5f9b9d6dfbae0b42a428dd0162ad10cde6214cb84b3a0cb87fa9f2bd23a436@group.calendar.google.com";

    private Credential credential;
    private String refreshToken;
    private org.joda.time.DateTime expirationDate;
    
    private Credential authorize() throws IOException, GeneralSecurityException {
//        if (/*убрал проверка на локалхост*/ expirationDate == null
//                || expirationDate != null && expirationDate.isEqualNow()
//                || expirationDate != null && expirationDate.isBeforeNow()) {
//            refreshToken();
//            expirationDate = new org.joda.time.DateTime(credential.getExpirationTimeMilliseconds());
//        } else
            if (credential == null) {
            HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            GoogleClientSecrets clientSecrets = loadClientSecrets();

            // Запрашиваем offline access чтобы узнать токен
            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
                    .setAccessType("offline")  // Запрашиваем offline access чтобы узнать токен
                    .build();

            LocalServerReceiver receiver = new LocalServerReceiver.Builder()
                    .setPort(8080)
                    .build();

            credential = new AuthorizationCodeInstalledApp(
                    flow, receiver).authorize("807338646599-316qm58inficv53a9bqgvjj3n44k80b9.apps.googleusercontent.com");
            refreshToken = credential.getRefreshToken();
            log.info("---NEW REFRESH TOKEN----------------------------------------------------");
            log.info(refreshToken);
            log.info("------------------------------------------------------------------------");
            expirationDate = new org.joda.time.DateTime(credential.getExpirationTimeMilliseconds());
        }

        return credential;
    }


    private void refreshToken() throws IOException, GeneralSecurityException {
        GoogleClientSecrets clientSecrets = loadClientSecrets();
        GoogleTokenResponse tokenResponse = new GoogleRefreshTokenRequest(
                new NetHttpTransport(), new JacksonFactory(), refreshToken,
                clientSecrets.getDetails().getClientId(), clientSecrets.getDetails().getClientSecret())
                .execute();

        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        credential = new GoogleCredential.Builder()
                .setTransport(httpTransport)
                .setJsonFactory(JSON_FACTORY)
                .setClientSecrets(clientSecrets)
                .build()
                .setRefreshToken(refreshToken)
                .setFromTokenResponse(tokenResponse);
        refreshToken = credential.getRefreshToken();
    }

    private GoogleClientSecrets loadClientSecrets()
            throws IOException {

        InputStream in = CalendarApiConfiguration.class.getResourceAsStream(CREDENTIALS_FILE_PATH);

        return GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
    }

    public static Calendar getService() throws IOException, GeneralSecurityException {
        // Build a new authorized API client service.
        CalendarApiConfiguration calendarApiConfiguration = new CalendarApiConfiguration();
        Credential credential = calendarApiConfiguration.authorize();
        Calendar service = new Calendar.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();

        return service;
    }

    public static void main(String[] args) throws GeneralSecurityException, IOException {
        Event event = new Event();
        String date = "2023-10-25";
        int quantityOfDays = 3;
        String calendarId = "7c5f9b9d6dfbae0b42a428dd0162ad10cde6214cb84b3a0cb87fa9f2bd23a436@group.calendar.google.com";

        DateTime dateToBook = new DateTime(date+"T14:00:00+03:00");
        EventDateTime start = new EventDateTime()
                .setDateTime(dateToBook)
                .setTimeZone("Europe/Moscow");
        event.setStart(start);

        org.joda.time.DateTime endDateTime = new org.joda.time.DateTime(dateToBook.getValue());
        endDateTime = endDateTime.plusDays(quantityOfDays);
        endDateTime = endDateTime.minusHours(3);

        DateTime lastDate = new DateTime(endDateTime.getMillis());
        EventDateTime end = new EventDateTime()
                .setDateTime(lastDate)
                .setTimeZone("Europe/Moscow");
        event.setEnd(end);

        endDateTime = endDateTime.plusDays(27);
        EventDateTime extremeNumber = new EventDateTime()
                .setDateTime(new DateTime(endDateTime.getMillis()))
                .setTimeZone("Europe/Moscow");

        List<Integer> allColors = new ArrayList<>(List.of(new Integer[]{11, 8, 5}));

        Map<Integer, org.joda.time.DateTime> collectTime = getService().events().list(calendarId)
                .setTimeMin(dateToBook)
                .setMaxResults(20)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute()
                .getItems()
                .stream()
                .collect(Collectors.toMap(
                        i -> Integer.valueOf(i.getColorId()),
                        i -> new org.joda.time.DateTime(i.getStart().getDateTime().getValue()),
                        (o,n) -> o
                        )
                );

        for (int c: allColors) {
            collectTime.putIfAbsent(c, new org.joda.time.DateTime(extremeNumber.getDateTime().getValue()));
        }


//        event = getService().events().insert(calendarId, event).execute();
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
        DateTime lastDate = new DateTime(dateTime.getMillis());
        EventDateTime eventlastDate = new EventDateTime();
        eventlastDate.setDateTime(lastDate);

        List<Integer> allColors = new ArrayList<>(List.of(new Integer[]{11, 8, 5}));
        Map<Integer, List<EventDateTime>> startTime = getService().events().list(calendarId)
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
                    boolean comparison = dateValue >= eventlastDate.getDateTime().getValue();
                    return FreeDateDto.builder()
                            //todo починить выбор варианта
                            .message(comparison ? "Домик свободный до "
                                    + new DateTime(dateValue).toString().substring(0, 10)
                                    + "\nВыберите количество дней"
                                    : "Выберите количество дней")
                            .colorId(i.getKey())
                            .freeDate(new DateTime(dateValue))
                            .isFree(true)
                            .build();
                })
                .get();


        return freeDateDto;
    }
}
