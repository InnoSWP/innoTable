package org.innoswp.innotable.model;

import org.innoswp.innotable.model.event.CalendarEvent;
import org.innoswp.innotable.model.user.Group;
import org.innoswp.innotable.model.user.Role;
import org.innoswp.innotable.model.user.User;

import java.util.List;

public interface Model {

    void saveEvent(CalendarEvent calendarEvent, Group group);

    Pair<User, List<CalendarEvent>> loadEventsByUser();

    List<Pair<Group, CalendarEvent>> loadAllEvents();

    List<User> getUsers();

    List<User> getUsersByGroup(Group group);

    List<User> getUsersByRole(Role role);
}
