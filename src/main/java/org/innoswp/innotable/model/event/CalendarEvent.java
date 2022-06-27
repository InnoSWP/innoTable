package org.innoswp.innotable.model.event;


import java.util.Date;

public record CalendarEvent(String title, String description, String location, Date startTime,
                            Date endTime) {
}