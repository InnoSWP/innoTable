package org.innoswp.innotable.model;

import org.innoswp.innotable.model.event.CalendarEvent;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public interface Model {
    List<String> getGroups() throws SQLException;

    void saveGroup(String group) throws SQLException;

    String dropGroup(String group) throws SQLException;

    List<String> getRoles() throws SQLException;

    void saveRole(String String) throws SQLException;

    void saveEvent(CalendarEvent calendarEvent, String String) throws SQLException;

    Pair<String, CalendarEvent> dropEvent(String title) throws SQLException;

    List<Pair<String, CalendarEvent>> loadEventsByUser(User user) throws SQLException;

    List<Pair<String, CalendarEvent>> loadEventsByGroup(String group) throws SQLException;

    List<Pair<String, CalendarEvent>> loadEventsIn(Date start, Date end) throws SQLException;

    List<Pair<String, CalendarEvent>> loadEventsDuring(Date time) throws SQLException;

    List<Pair<String, CalendarEvent>> loadEventsByTitle(String title) throws SQLException;

    List<Pair<String, CalendarEvent>> loadAllEvents() throws SQLException;

    void addUser(User user) throws SQLException;

    void assignUsersToGroup(List<User> users, String group) throws SQLException;

    User dropUser(String email) throws SQLException;

    List<User> getUsers() throws SQLException;

    List<User> getUsersByGroup(String group) throws SQLException;

    List<User> getUsersByRole(String role) throws SQLException;
}
