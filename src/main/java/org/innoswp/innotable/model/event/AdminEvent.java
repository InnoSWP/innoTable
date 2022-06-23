package org.innoswp.innotable.model.event;

import lombok.AllArgsConstructor;
import org.innoswp.innotable.model.user.Group;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
public class AdminEvent {
    private String name;
    private String description;
    private String location;
    private String startDate;
    private String startTime;
    private String endDate;
    private String endTime;
    private List<String> groups;

    public CalendarEvent toCalendarEvent() {
        var formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        return new CalendarEvent(
                name,
                description,
                new Location(location),
                LocalDateTime.parse(startDate + " " + startTime, formatter),
                LocalDateTime.parse(endDate + " " + endDate, formatter),
                groups.stream().map(Group::new).collect(Collectors.toList())
        );
    }

    @Override
    public String toString() {
        return "AdminEvent{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", location='" + location + '\'' +
                ", startDate='" + startDate + '\'' +
                ", startTime='" + startTime + '\'' +
                ", endDate='" + endDate + '\'' +
                ", endTime='" + endTime + '\'' +
                ", groups=" + groups +
                '}';
    }
}
