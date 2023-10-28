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
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CalendarApiConfiguration {


    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    private final GoogleCalendarProperties properties;
    private Credential credential;
    private String refreshToken;
    private org.joda.time.DateTime expirationDate;

    private static final String APPLICATION_NAME = "Google Calendar API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String ACCESS_TYPE = "offline";
    private static final List<String> SCOPES =
            Collections.singletonList(CalendarScopes.CALENDAR);

    private Credential authorize() throws IOException, GeneralSecurityException {
        if (!properties.getLocalhost() && expirationDate == null
                || (expirationDate != null && expirationDate.isEqualNow())
                || (expirationDate != null && expirationDate.isBeforeNow())) {
            refreshToken();
            expirationDate = new org.joda.time.DateTime(credential.getExpirationTimeMilliseconds());
        } else
            if (credential == null) {
            HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            GoogleClientSecrets clientSecrets = loadClientSecrets();

            // Запрашиваем offline access чтобы узнать токен
            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
                    .setAccessType(ACCESS_TYPE)  // Запрашиваем offline access чтобы узнать токен
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
                clientSecrets.getDetails().getClientId(),
                clientSecrets.getDetails().getClientSecret())
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
        String initialString = properties.toString();
        InputStream targetStream = new ByteArrayInputStream(initialString.getBytes());
        return GoogleClientSecrets.load(
                JSON_FACTORY,
                new InputStreamReader(targetStream, StandardCharsets.UTF_8)
        );
    }

    public Calendar getService() throws IOException, GeneralSecurityException {
        // Build a new authorized API client service.
        Calendar service = new Calendar.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, authorize())
                .setApplicationName(APPLICATION_NAME)
                .build();

        return service;
    }


}
