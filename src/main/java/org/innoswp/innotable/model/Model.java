package org.innoswp.innotable.model;

import org.innoswp.innotable.model.event.CalendarEvent;
import org.innoswp.innotable.model.user.Group;
import org.innoswp.innotable.model.user.Role;
import org.innoswp.innotable.model.user.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface Model {
    List<Group> getGroups() throws Exception;

    void saveGroup(Group group) throws Exception;

    Group dropGroup(Group group) throws Exception;

    List<Role> getRoles() throws Exception;

    void saveRole(Role role) throws Exception;

    void saveEvent(CalendarEvent calendarEvent, Group group) throws Exception;

    Pair<Group, CalendarEvent> dropEvent(String title) throws Exception;

    List<Pair<Group, CalendarEvent>> loadEventsByUser(User user) throws Exception;

    List<Pair<Group, CalendarEvent>> loadEventsByGroup(Group group) throws Exception;

    List<Pair<Group, CalendarEvent>> loadEventsByDay(LocalDate date) throws Exception;

    List<Pair<Group, CalendarEvent>> loadEventsIn(LocalDateTime start, LocalDateTime end) throws Exception;

    List<Pair<Group, CalendarEvent>> loadEventsDuring(LocalDateTime time) throws Exception;

    List<Pair<Group, CalendarEvent>> loadEventsByTitle(String title) throws Exception;

    List<Pair<Group, CalendarEvent>> loadAllEvents() throws Exception;

    void addUser(User user) throws Exception;

    void assignUsersToGroup(List<User> users, Group group) throws Exception;

    User dropUser(String email) throws Exception;

    List<User> getUsers() throws Exception;

    List<User> getUsersByGroup(Group group) throws Exception;

    List<User> getUsersByRole(Role role) throws Exception;
}
