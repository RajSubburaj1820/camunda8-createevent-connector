package io.camunda.connector;

import io.camunda.connector.api.annotation.Secret;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Objects;

public class CreateEventInput {

  public static final String INPUT_SERVICE_ACCOUNT_JSON_TOKEN = "serviceAccountJSONToken";
  public static final String INPUT_SERVICE_ACCOUNT_USER = "serviceAccountUser";
  public static final String INPUT_APPLICATION_NAME = "applicationName";
  public static final String INPUT_START_DATE = "startDate";
  public static final String INPUT_END_DATE = "endDate";
  public static final String INPUT_DESCRIPTION = "description";
  public static final String INPUT_SOURCE_TITLE = "sourceTitle";
  public static final String INPUT_START_DATE_STRING = "startDateString";
  public static final String INPUT_END_DATE_STRING = "endDateString";
  public static final String INPUT_CALENDAR_ID = "calendarId";

  @NotEmpty
  private String serviceAccountJSONToken;
  private String serviceAccountUser;
  private String applicationName;
  private ZonedDateTime startDate;
  private ZonedDateTime endDate;
  private String description;
  private String sourceTitle;
  private String startDateString;
  private String endDateString;
  private String calendarId;

  public String getStartDateString() {
    return startDateString;
  }

  public String getEndDateString() {
    return endDateString;
  }

  public String getServiceAccountJSONToken() {
    return serviceAccountJSONToken;
  }

  public String getServiceAccountUser() {
    return serviceAccountUser;
  }

  public String getApplicationName() {
    return applicationName;
  }

  public ZonedDateTime getStartDate() {
    return startDate;
  }

  public ZonedDateTime getEndDate() {
    return endDate;
  }

  public String getDescription() {
    return description;
  }

  public String getSourceTitle() {
    return sourceTitle;
  }

  public String getCalendarId() {
    return calendarId;
  }
}
