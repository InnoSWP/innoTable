package org.innoswp.innotable.model;

import org.innoswp.innotable.model.event.CalendarEvent;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface Model {
    List<String> getGroups() throws Exception;

    void saveGroup(String group) throws Exception;

    String dropGroup(String group) throws Exception;

    List<String> getRoles() throws Exception;

    void saveRole(String String) throws Exception;

    void saveEvent(CalendarEvent calendarEvent, String String) throws Exception;

    Pair<String, CalendarEvent> dropEvent(String title) throws Exception;

    List<Pair<String, CalendarEvent>> loadEventsByUser(User user) throws Exception;

    List<Pair<String, CalendarEvent>> loadEventsByGroup(String group) throws Exception;

    List<Pair<String, CalendarEvent>> loadEventsByDay(LocalDate date) throws Exception;

    List<Pair<String, CalendarEvent>> loadEventsIn(LocalDateTime start, LocalDateTime end) throws Exception;

    List<Pair<String, CalendarEvent>> loadEventsDuring(LocalDateTime time) throws Exception;

    List<Pair<String, CalendarEvent>> loadEventsByTitle(String title) throws Exception;

    List<Pair<String, CalendarEvent>> loadAllEvents() throws Exception;

    void addUser(User user) throws Exception;

    void assignUsersToGroup(List<User> users, String group) throws Exception;

    User dropUser(String email) throws Exception;

    List<User> getUsers() throws Exception;

    List<User> getUsersByGroup(String String) throws Exception;

    List<User> getUsersByRole(String String) throws Exception;
}
