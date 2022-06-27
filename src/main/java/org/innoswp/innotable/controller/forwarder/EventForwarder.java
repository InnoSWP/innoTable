package org.innoswp.innotable.controller.forwarder;

import org.innoswp.innotable.model.Pair;
import org.innoswp.innotable.model.User;
import org.innoswp.innotable.model.event.CalendarEvent;

import java.util.List;

public interface EventForwarder {

    void pushEventForUser(User user, List<CalendarEvent> userEvents) throws Exception;

    void pushEventsForGroup(String group, List<CalendarEvent> groupEvents) throws Exception;

    void pushAllEvents(List<Pair<String, List<CalendarEvent>>> events) throws Exception;
}
