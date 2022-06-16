package org.innoswp.innotable.model.data;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public abstract class Event {
    private final long id;
    private String title;
    private Location location;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Group group;
}
