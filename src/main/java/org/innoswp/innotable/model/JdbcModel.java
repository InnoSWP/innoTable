package org.innoswp.innotable.model;

import org.innoswp.innotable.model.event.CalendarEvent;
import org.innoswp.innotable.model.event.Location;
import org.innoswp.innotable.model.user.Group;
import org.innoswp.innotable.model.user.Role;
import org.innoswp.innotable.model.user.User;
import org.innoswp.innotable.model.util.ConnectionManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

public class JdbcModel implements Model {


    @Override
    public List<Group> getGroups() throws SQLException {
        String sql = """
                SELECT label
                FROM "group"
                """;
        List<Group> result = new LinkedList<>();
        try (var connection = ConnectionManager.open();
             var statement = connection.createStatement()) {
            var resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                result.add(new Group(resultSet.getObject("label", String.class)));
            }
        }
        return result;
    }

    @Override
    public void saveGroup(Group group) throws SQLException {
        String sql = """
                INSERT INTO "group"(label)
                VALUES (?)
                """;
        try (var connection = ConnectionManager.open();
             var preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, group.label());
            preparedStatement.execute();
        }
    }

    @Override
    public Group dropGroup(Group group) throws SQLException {
        String sql = """
                DELETE FROM "group"
                WHERE label = ?
                RETURNING label
                """;
        try (var connection = ConnectionManager.open();
             var preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, group.label());
            var resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return new Group(resultSet.getObject("label", String.class));
            }
        }
        return null;
    }

    @Override
    public List<Role> getRoles() throws SQLException {
        String sql = """
                SELECT label
                FROM role
                """;
        List<Role> result = new LinkedList<>();
        try (var connection = ConnectionManager.open();
             var statement = connection.createStatement()) {
            var resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                result.add(new Role(resultSet.getObject("label", String.class)));
            }
        }
        return result;
    }

    @Override
    public void saveRole(Role role) throws SQLException {
        String sql = """
                INSERT INTO role(label)
                VALUES (?)
                """;
        try (var connection = ConnectionManager.open();
             var preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, role.label());
            preparedStatement.execute();
        }
    }

    @Override
    public void saveEvent(CalendarEvent event, Group group) throws SQLException {
        String sql = """
                INSERT INTO event(title, description, location, start_dt, end_dt, group_id)
                VALUES (?, ?, ?, ?, ?, (SELECT id FROM "group" WHERE label = ?))
                """;
        try (var connection = ConnectionManager.open();
             var preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, event.title());
            preparedStatement.setString(2, event.description());
            preparedStatement.setString(3, event.location().label());
            preparedStatement.setTimestamp(4, Timestamp.valueOf(event.startTime()));
            preparedStatement.setTimestamp(5, Timestamp.valueOf(event.endTime()));
            preparedStatement.setString(6, group.label());
            preparedStatement.execute();
        }
    }

    @Override
    public Pair<Group, CalendarEvent> dropEvent(String title) throws SQLException {
        String sql = """
                DELETE FROM event
                WHERE title = ?
                RETURNING title, description, location, start_dt, end_dt, (SELECT label FROM "group" WHERE "group".id = (SELECT group_id FROM event WHERE title = ?)) grp
                """;
        try (var connection = ConnectionManager.open();
             var preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, title);
            preparedStatement.setString(2, title);
            var resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return new Pair<>(new Group(resultSet.getObject("grp", String.class)), new CalendarEvent(resultSet.getObject("title", String.class), resultSet.getObject("description", String.class),
                        new Location(resultSet.getObject("location", String.class)), resultSet.getObject("start_dt", LocalDateTime.class),
                        resultSet.getObject("end_dt", LocalDateTime.class)));
            }
        }
        return null;
    }

    @Override
    public List<Pair<Group, CalendarEvent>> loadEventsByUser(User user) throws Exception {
        String sql = """
                SELECT title, description, location, start_dt, end_dt, (SELECT label FROM "group" WHERE event.group_id = id) "group" FROM event
                WHERE (SELECT label FROM "group" WHERE event.group_id = id) = ?
                """;
        var events = new LinkedList<Pair<Group, CalendarEvent>>();
        try (var connection = ConnectionManager.open()) {
            for (var group : user.group()) {
                try (var preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setString(1, group.label());
                    formEventList(events, preparedStatement.executeQuery());
                }
            }
        }
        return events;
    }

    @Override
    public List<Pair<Group, CalendarEvent>> loadEventsByGroup(Group group) throws Exception {
        String sql = """
                SELECT title, description, location, start_dt, end_dt, (SELECT label FROM "group" WHERE event.group_id = id) "group" FROM event
                WHERE (SELECT label FROM "group" WHERE event.group_id = id) = ?
                """;
        var events = new LinkedList<Pair<Group, CalendarEvent>>();
        try (var connection = ConnectionManager.open();
             var preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, group.label());
            formEventList(events, preparedStatement.executeQuery());
        }
        return events;
    }

    @Override
    public List<Pair<Group, CalendarEvent>> loadEventsByDay(LocalDate date) throws Exception {
        return getEventsFromInterval(date.atStartOfDay(), date.atTime(23, 59, 59, 999999999));
    }

    @Override
    public List<Pair<Group, CalendarEvent>> loadEventsIn(LocalDateTime start, LocalDateTime end) throws Exception {
        return getEventsFromInterval(start, end);
    }

    @Override
    public List<Pair<Group, CalendarEvent>> loadEventsDuring(LocalDateTime time) throws Exception {
        return getEventsFromInterval(time, time);
    }

    private List<Pair<Group, CalendarEvent>> getEventsFromInterval(LocalDateTime start, LocalDateTime end) throws SQLException {
        String sql = """
                SELECT title, description, location, start_dt, end_dt, (SELECT label FROM "group" WHERE event.group_id = id) "group" FROM event
                WHERE start_dt <= ? AND ? <= end_dt;
                """;
        var events = new LinkedList<Pair<Group, CalendarEvent>>();
        try (var connection = ConnectionManager.open();
             var preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setTimestamp(1, Timestamp.valueOf(end));
            preparedStatement.setTimestamp(2, Timestamp.valueOf(start));
            var resultSet = preparedStatement.executeQuery();
            formEventList(events, resultSet);
        }
        return events;
    }

    @Override
    public LinkedList<Pair<Group, CalendarEvent>> loadEventsByTitle(String title) throws SQLException {
        String sql = """
                SELECT title, description, location, start_dt, end_dt, (SELECT label FROM "group" WHERE event.group_id = id) "group" FROM event
                WHERE title = ?;
                """;
        var events = new LinkedList<Pair<Group, CalendarEvent>>();
        try (var connection = ConnectionManager.open();
             var preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, title);
            var resultSet = preparedStatement.executeQuery();
            formEventList(events, resultSet);
        }
        return events;
    }

    private void formEventList(LinkedList<Pair<Group, CalendarEvent>> events, ResultSet resultSet) throws SQLException {
        while (resultSet.next()) {
            events.add(new Pair<>(new Group(resultSet.getObject("group", String.class)), new CalendarEvent(resultSet.getObject("title", String.class), resultSet.getObject("description", String.class),
                    new Location(resultSet.getObject("location", String.class)), resultSet.getObject("start_dt", LocalDateTime.class),
                    resultSet.getObject("end_dt", LocalDateTime.class))));
        }
    }

    @Override
    public List<Pair<Group, CalendarEvent>> loadAllEvents() throws SQLException {
        String sql = """
                SELECT title, description, location, start_dt, end_dt, (SELECT label FROM "group" WHERE event.group_id = id) "group" FROM event
                ORDER BY start_dt;
                """;
        var events = new LinkedList<Pair<Group, CalendarEvent>>();
        try (var connection = ConnectionManager.open();
             var preparedStatement = connection.prepareStatement(sql)) {
            var resultSet = preparedStatement.executeQuery();
            formEventList(events, resultSet);
        }
        return events;
    }

    @Override
    public void addUser(User user) throws SQLException {
        String sql = """
                INSERT INTO "user"(email, password, role_id)
                VALUES (?, ?, (SELECT id FROM "role" WHERE label = ?));
                                
                                
                """;
        String sql1 = """
                INSERT INTO user_group(user_id, group_id)
                VALUES ((SELECT id FROM "user" WHERE email = ?), (SELECT id FROM "group" WHERE label = ?))
                """;
        try (var connection = ConnectionManager.open();
             var preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, user.email());
            preparedStatement.setString(2, user.password());
            preparedStatement.setString(3, user.role().label());
            preparedStatement.execute();
            for (Group group : user.group()) {
                try (var pStatement = connection.prepareStatement(sql1)) {
                    pStatement.setString(1, user.email());
                    pStatement.setString(2, group.label());
                    pStatement.execute();
                }
            }
        }
    }

    @Override
    public void assignUsersToGroup(List<User> users, Group group) throws SQLException {
        String sql = """
                INSERT INTO user_group(user_id, group_id)
                VALUES ((SELECT id FROM "user" WHERE email = ?), (SELECT id FROM "group" WHERE label = ?))
                 """;
        try (var connection = ConnectionManager.open()) {
            for (User user : users) {
                user.group().add(group);
                try (var pStatement = connection.prepareStatement(sql)) {
                    pStatement.setString(1, user.email());
                    pStatement.setString(2, group.label());
                    pStatement.execute();
                }
            }
        }
    }

    @Override
    public User dropUser(String email) throws SQLException {
        String sql = """
                DELETE FROM "user"
                WHERE email = ?
                RETURNING email, password, (SELECT label FROM "role" WHERE "role".id = (SELECT role_id FROM "user" WHERE email = ?)) rl
                """;
        try (var connection = ConnectionManager.open();
             var preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, email);
            preparedStatement.setString(2, email);
            var resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return new User(resultSet.getObject("email", String.class), resultSet.getObject("password", String.class),
                        new LinkedList<>(), new Role(resultSet.getObject("rl", String.class)));
            }
        }
        return null;
    }

    @Override
    public List<User> getUsers() throws SQLException {
        String sql = """
                SELECT email, password, (SELECT label FROM "role" WHERE "role".id = "user".role_id) rl FROM "user";
                """;
        List<User> users = new LinkedList<>();
        try (var connection = ConnectionManager.open();
             var preparedStatement = connection.prepareStatement(sql)) {
            var resultSet = preparedStatement.executeQuery();
            getUsersFromResultSet(users, connection, resultSet);
        }
        return users;
    }

    @Override
    public List<User> getUsersByGroup(Group group) throws SQLException {
        String sql = """
                SELECT email, password, (SELECT label FROM "role" WHERE "role".id = "user".role_id) rl FROM "user"
                                         WHERE id IN (SELECT user_id FROM user_group
                                                            WHERE group_id = (SELECT id FROM "group"
                                                                              WHERE label = ?));
                """;
        List<User> users = new LinkedList<>();
        try (var connection = ConnectionManager.open();
             var preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, group.label());
            var resultSet = preparedStatement.executeQuery();
            getUsersFromResultSet(users, connection, resultSet);
        }
        return users;
    }

    private void getUsersFromResultSet(List<User> users, Connection connection, ResultSet resultSet) throws SQLException {
        String sql1 = """
                SELECT label FROM "group"
                WHERE id IN (SELECT group_id FROM user_group
                            WHERE user_id = (SELECT id FROM "user"
                                            WHERE email = ?))
                """;
        while (resultSet.next()) {
            var user = new User(resultSet.getObject("email", String.class), resultSet.getObject("password", String.class),
                    new LinkedList<>(), new Role(resultSet.getObject("rl", String.class)));
            try (var ps = connection.prepareStatement(sql1)) {
                ps.setString(1, user.email());
                var resultSet1 = ps.executeQuery();
                while (resultSet1.next()) {
                    user.group().add(new Group(resultSet1.getObject("label", String.class)));
                }
            }
            users.add(user);
        }
    }

    @Override
    public List<User> getUsersByRole(Role role) throws SQLException {
        String sql = """
                SELECT email, password, (SELECT label FROM "role" WHERE "role".id = "user".role_id) rl FROM "user"
                                         WHERE role_id = (SELECT id FROM role
                                                          WHERE label = ?);
                """;
        var users = new LinkedList<User>();
        try (var connection = ConnectionManager.open();
             var preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, role.label());
            var resultSet = preparedStatement.executeQuery();
            getUsersFromResultSet(users, connection, resultSet);
        }
        return users;
    }
}
