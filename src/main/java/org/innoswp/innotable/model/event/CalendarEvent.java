package org.innoswp.innotable.model.event;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public abstract class CalendarEvent {
    private final long id;
    private String title;
    private String description;
    private Location location;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
