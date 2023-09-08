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
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
public class CalendarApiServices {

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

        InputStream in = CalendarApiServices.class.getResourceAsStream(CREDENTIALS_FILE_PATH);

        return GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
    }

    public static void main(String... args) throws IOException, GeneralSecurityException {
        // Build a new authorized API client service.
        CalendarApiServices calendarApiServices = new CalendarApiServices();
        Credential credential = calendarApiServices.authorize();
        Calendar service = new Calendar.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();


        Event event = new Event()
                .setSummary("Тут")
                .setDescription("Description");

        DateTime startDateTime = new DateTime("2023-09-11T10:00:00+03:00");
        EventDateTime start = new EventDateTime()
                .setDateTime(startDateTime)
                .setTimeZone("Europe/Moscow");
        event.setStart(start);

        DateTime endDateTime = new DateTime("2023-09-11T11:00:00+03:00");
        EventDateTime end = new EventDateTime()
                .setDateTime(endDateTime)
                .setTimeZone("Europe/Moscow");
        event.setEnd(end);

        String[] recurrence = new String[] {"RRULE:FREQ=DAILY;COUNT=2"};
        event.setRecurrence(Arrays.asList(recurrence));

        EventAttendee[] attendees = new EventAttendee[] {
                new EventAttendee().setEmail("mollaev6@mail.ru"),
        };
        event.setAttendees(Arrays.asList(attendees));

        String calendarId = "7c5f9b9d6dfbae0b42a428dd0162ad10cde6214cb84b3a0cb87fa9f2bd23a436@group.calendar.google.com";
        event = service.events().insert(calendarId, event).execute();
        System.out.printf("Event created: %s\n", event.getHtmlLink());

        // List the next 10 events from the primary calendar.
        DateTime now = new DateTime(System.currentTimeMillis());
        Events events = service.events().list("7c5f9b9d6dfbae0b42a428dd0162ad10cde6214cb84b3a0cb87fa9f2bd23a436@group.calendar.google.com")
                .setMaxResults(10)
                .setTimeMin(now)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();


        List<Event> items = events.getItems();
        if (items.isEmpty()) {
            System.out.println("No upcoming events found.");
        } else {
            System.out.println("Upcoming events");
            for (Event event1 : items) {
                DateTime start1 = event.getStart().getDateTime();
                if (start1 == null) {
                    start1 = event.getStart().getDate();
                }
                System.out.printf("%s (%s)\n", event1.getSummary(), start);
            }
        }
    }
}
