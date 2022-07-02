package org.innoswp.innotable.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.innoswp.innotable.model.event.CalendarEvent;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

@AllArgsConstructor
public class AdminEvent {
    private String name;
    private String description;
    private String location;
    private String startDate;
    private String startTime;
    private String endDate;
    private String endTime;

    @Getter
    private List<String> groups;

    public CalendarEvent toCalendarEvent() throws ParseException {
        var formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm");

        if (name == null || name.equals(""))
            throw new ParseException("Name is empty", 0);

        if (description == null || description.equals(""))
            throw new ParseException("Description is empty", 0);

        if (location == null || location.equals(""))
            throw new ParseException("Location is empty", 0);

        return new CalendarEvent(
                name,
                description,
                location,
                formatter.parse(startDate + " " + startTime),
                formatter.parse(endDate + " " + endTime)
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
