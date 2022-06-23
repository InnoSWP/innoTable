package org.innoswp.innotable.model.event;

import java.time.LocalDateTime;

public record CalendarEvent(String title, String description, Location location, LocalDateTime startTime,
                            LocalDateTime endTime) {
}