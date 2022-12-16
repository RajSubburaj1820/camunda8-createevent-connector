package io.camunda.connector;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.EventDateTime;
import io.camunda.connector.api.annotation.OutboundConnector;
import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.api.outbound.OutboundConnectorFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarRequest;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.api.services.calendar.Calendar.Events.Insert;

@OutboundConnector(
        name = "GoogleEvent",
        inputVariables = {
                CreateEventInput.INPUT_SERVICE_ACCOUNT_JSON_TOKEN,
                CreateEventInput.INPUT_SERVICE_ACCOUNT_USER,
                CreateEventInput.INPUT_APPLICATION_NAME,
                CreateEventInput.INPUT_START_DATE,
                CreateEventInput.INPUT_END_DATE,
                CreateEventInput.INPUT_DESCRIPTION,
                CreateEventInput.INPUT_SOURCE_TITLE,
                CreateEventInput.INPUT_END_DATE_STRING,
                CreateEventInput.INPUT_START_DATE_STRING,
                CreateEventInput.INPUT_CALENDAR_ID
        },
        type = CreateEventFunction.CONNECTOR_TYPE)
public class CreateEventFunction implements OutboundConnectorFunction {
    public static final String CONNECTOR_TYPE = "GoogleCalendar";
    public static final String BPMNERROR_BAD_CONNECTION = "BadConnection";
    public static final String BPMNERROR_EVENT_CREATION = "EventCreation";
    public final static String BPMNERROR_BAD_DATE_FORMAT = "BAD_DATE_FORMAT";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final Logger LOGGER = LoggerFactory.getLogger(CreateEventFunction.class);

    @Override
    public Object execute(OutboundConnectorContext context) throws Exception {
        var connectorRequest = context.getVariablesAsType(CreateEventInput.class);

        context.validate(connectorRequest);
        context.replaceSecrets(connectorRequest);

        return executeConnector(connectorRequest);
    }

    private CreateEventOutput executeConnector(final CreateEventInput createEventInput) {
        final NetHttpTransport httpTransport;
        HttpRequestInitializer requestInitializer = null;
        try (InputStream jsonTokenStream = new ByteArrayInputStream(
                createEventInput.getServiceAccountJSONToken().getBytes())) {
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            requestInitializer = new HttpCredentialsAdapter(
                    ServiceAccountCredentials.fromStream(jsonTokenStream)
                            .toBuilder()
                            .setServiceAccountUser(createEventInput.getServiceAccountUser())
                            .build()
                            .createScoped(CalendarScopes.CALENDAR));
        } catch (IOException|GeneralSecurityException e) {
            throw new ConnectorException(BPMNERROR_BAD_CONNECTION,"Exception "+e );
        }
        Calendar calendar = new Calendar.Builder(httpTransport, JSON_FACTORY, requestInitializer)
                .setApplicationName(createEventInput.getApplicationName())
                .build();
        final Event eventToInsert = new Event();
        try {
            buildEvent(eventToInsert,createEventInput);
            final Insert insert = calendar.events().insert(createEventInput.getCalendarId(), eventToInsert);
            final Event insertedEvent = insert.execute();
            CreateEventOutput createEventResult = new CreateEventOutput();
            createEventResult.eventID = insertedEvent.getId();
            createEventResult.status = insertedEvent.getStatus();
            return createEventResult;
        } catch (IOException e) {
            throw new ConnectorException(BPMNERROR_EVENT_CREATION,"Exception "+e);
        }
    }

    protected void buildEvent(final Event event, CreateEventInput createEventInput) {
        event.setStart(
                createEventInput.getStartDate() != null ? buildEventDateTime(createEventInput.getStartDate(), createEventInput.getStartDateString()): null);
        event.setEnd(createEventInput.getEndDate() != null ? buildEventDateTime(createEventInput.getEndDate(), createEventInput.getEndDateString()): null);
       // event.setSummary(getSummary());
        event.setDescription(createEventInput.getDescription());
       // event.setLocation(getLocation());
      //  event.setAnyoneCanAddSelf(getAnyoneCanAddSelf());
       // event.setColorId(getColorId());
       /* if (getGadgetTitle() != null) {
            createGadget(event);
        }
        event.setGuestsCanInviteOthers(getGuestsCanInviteOthers());
        event.setGuestsCanSeeOtherGuests(getGuestsCanSeeOtherGuests());
        event.setId(getId());
        event.setSequence(getSequence());
        if (getSourceTitle() != null) {
            final Source source = new Source();
            source.setTitle(getSourceTitle());
            source.setUrl(getSourceUrl());
            event.setSource(source);
        }
        event.setStatus(getStatus());
        event.setTransparency(getTransparency());
        event.setVisibility(getVisibility());
        if (getOriginalStartDate() != null) {
            event.setOriginalStartTime(
                    buildEventDateTime(getOriginalStartDate(), getOriginalStartTime(), getOriginalStartTimeZone()));
        }
        event.setRecurrence(getRecurrence());
        if (getAttendeesEmails() != null) {
            addAttendees(event);
        }
        if (getReminderUseDefault() != null || getReminderOverrides() != null) {
            setReminders(event);
        }*/
    }

    private static final DateTimeFormatter RFC3339_FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .withZone(ZoneId.of("UTC"));



    protected EventDateTime buildEventDateTime(final ZonedDateTime zonedDateTime, final String dateString) throws ConnectorException {

      //  DateTimeFormatter formatter = DateTimeFormatter.ofPattern(RFC3339_FORMATTER);
        try {
            ZonedDateTime eventZonedDateTime = zonedDateTime;
            if (zonedDateTime == null) {
                eventZonedDateTime = ZonedDateTime.parse(dateString, RFC3339_FORMATTER);
            }

            final EventDateTime edt = new EventDateTime();
            edt.setDateTime(DateTime.parseRfc3339(eventZonedDateTime.format(RFC3339_FORMATTER)));
            return edt;
        } catch (Exception e) {
            throw new ConnectorException(BPMNERROR_BAD_DATE_FORMAT, "Date [" + dateString + "] is not the expected format (yyyy-MM-dd'T'HH:mm:ss.SSS'Z')");
        }
    }
}



