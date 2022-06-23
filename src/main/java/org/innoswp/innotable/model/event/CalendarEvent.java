package org.innoswp.innotable.model.event;

import org.innoswp.innotable.model.user.Group;

import java.time.LocalDateTime;
import java.util.List;

public record CalendarEvent(String title, String description, Location location, LocalDateTime startTime,
                            LocalDateTime endTime, List<Group> groups) {
}