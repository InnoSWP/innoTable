package org.innoswp.innotable.model.event;


import org.innoswp.innotable.pojo.AdminEvent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public record CalendarEvent(String title, String description, String location, Date startDateTime,
                            Date endDateTime) {
    public AdminEvent toAdminEvent() {
        var dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        var timeFormatter = new SimpleDateFormat("hh:mm");

        return new AdminEvent(
                title,
                description,
                location,
                dateFormatter.format(startDateTime),
                timeFormatter.format(startDateTime),
                dateFormatter.format(endDateTime),
                timeFormatter.format(endDateTime),
                new ArrayList<>()
        );
    }
}