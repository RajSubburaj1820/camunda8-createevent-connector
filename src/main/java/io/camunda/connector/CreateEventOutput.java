package io.camunda.connector;

import java.util.Objects;

public class CreateEventOutput {

  public static final String OUTPUT_EVENT_ID = "eventID";
  public static final String OUTPUT_STATUS = "status";


  // TODO: define connector result properties, which are returned to the process engine
  public String eventID;
  public String status;

  public String geteventID() {
    return eventID;
  }

  public String getstatus() {
    return status;
  }
}
