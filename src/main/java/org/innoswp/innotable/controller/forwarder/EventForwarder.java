package org.innoswp.innotable.controller.forwarder;

import org.innoswp.innotable.model.Pair;
import org.innoswp.innotable.model.event.CalendarEvent;
import org.innoswp.innotable.model.user.Group;
import org.innoswp.innotable.model.user.User;

import java.util.List;

public interface EventForwarder {

    void pushEventForUser(User user, List<CalendarEvent> userEvents);

    void pushEventsForGroup(Group group, List<CalendarEvent> groupEvents);

    void pushAllEvents(List<Pair<Group, CalendarEvent>> events);
}
