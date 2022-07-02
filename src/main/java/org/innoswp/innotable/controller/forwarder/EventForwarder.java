package org.innoswp.innotable.controller.forwarder;

import org.innoswp.innotable.model.User;
import org.innoswp.innotable.model.event.CalendarEvent;

public interface EventForwarder {
    void pushEventForUser(User user, CalendarEvent calendarEvent) throws Exception;
}
