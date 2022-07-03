package org.innoswp.innotable.controller.forwarder;

import org.innoswp.innotable.model.User;
import org.innoswp.innotable.model.event.CalendarEvent;

import java.util.Collection;

public interface EventForwarder {
    void pushEventForUsers(Collection<User> users, CalendarEvent calendarEvent) throws Exception;
}
