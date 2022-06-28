package org.innoswp.innotable.model;

import lombok.extern.slf4j.Slf4j;
import org.innoswp.innotable.model.event.CalendarEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

@Slf4j
@Repository
public class JdbcModel implements Model {

    private final String datasourceUrl;

    private final String datasourceUsername;

    private final String datasourcePassword;

    public JdbcModel(@Value("${spring.datasource.url}") String datasourceUrl,
                     @Value("${spring.datasource.username}") String datasourceUsername,
                     @Value("${spring.datasource.password}") String datasourcePassword) {
        this.datasourceUrl = datasourceUrl;
        this.datasourceUsername = datasourceUsername;
        this.datasourcePassword = datasourcePassword;
    }

    private Connection open() {
        try {
            return DriverManager.getConnection(
                    datasourceUrl,
                    datasourceUsername,
                    datasourcePassword
            );

        } catch (SQLException e) {
            log.error("Cannot establish connection with JdbcModel");
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> getGroups() throws SQLException {
        var query = """
                SELECT label
                FROM "group"
                """;

        var response = new LinkedList<String>();

        try (
                var connection = open();
                var statement = connection.createStatement()
        ) {
            var resultSet = statement.executeQuery(query);

            while (resultSet.next())
                response.add(resultSet.getObject("label", String.class));
        }

        log.trace("Loaded groups list");
        return response;
    }

    @Override
    public void saveGroup(String group) throws SQLException {
        var query = """
                INSERT INTO "group" (label)
                VALUES (?)
                """;
        try (
                var connection = open();
                var preparedStatement = connection.prepareStatement(query)
        ) {
            preparedStatement.setString(1, group);
            preparedStatement.execute();
        }

        log.trace("Saved group " + group);
    }

    @Override
    public String dropGroup(String group) throws SQLException {
        var query = """
                DELETE FROM "group"
                WHERE label = ?
                RETURNING label
                """;
        try (
                var connection = open();
                var preparedStatement = connection.prepareStatement(query)
        ) {
            preparedStatement.setString(1, group);
            var resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                log.trace("Dropped group " + group);
                return resultSet.getObject("label", String.class);
            }
        }

        log.warn("In dropGroup query group " + group + " does not exist, returning null!");
        return null;
    }

    @Override
    public List<String> getRoles() throws SQLException {
        var query = """
                SELECT label
                FROM role
                """;
        var result = new LinkedList<String>();
        try (
                var connection = open();
                var statement = connection.createStatement()
        ) {
            var resultSet = statement.executeQuery(query);

            while (resultSet.next())
                result.add(resultSet.getObject("label", String.class));
        }

        log.trace("Loaded roles list");

        return result;
    }

    @Override
    public void saveRole(String role) throws SQLException {
        var query = """
                INSERT INTO role (label)
                VALUES (?)
                """;
        try (
                var connection = open();
                var preparedStatement = connection.prepareStatement(query)
        ) {
            preparedStatement.setString(1, role);
            preparedStatement.execute();
        }

        log.trace("Saved role " + role);
    }

    @Override
    public void saveEvent(CalendarEvent event, String group) throws SQLException {
        var query = """
                INSERT INTO event(title, description, location, start_dt, end_dt, group_id)
                VALUES (?, ?, ?, ?, ?, (SELECT id FROM "group" WHERE label = ?))
                """;
        try (
                var connection = open();
                var preparedStatement = connection.prepareStatement(query)
        ) {
            preparedStatement.setString(1, event.title());
            preparedStatement.setString(2, event.description());
            preparedStatement.setString(3, event.location());

            preparedStatement.setTimestamp(4, Timestamp.valueOf(event.startTime().toString()));
            preparedStatement.setTimestamp(5, Timestamp.valueOf(event.endTime().toString()));

            preparedStatement.setString(6, group);

            preparedStatement.execute();

            log.trace("Saved event for group " + group);
        }
    }

    @Override
    public Pair<String, CalendarEvent> dropEvent(String title) throws SQLException {
        var query = """
                DELETE FROM event
                WHERE title = ?
                RETURNING title, description, location, start_dt, end_dt,
                    (SELECT label FROM "group" WHERE "group".id =
                        (SELECT group_id FROM event WHERE title = ?)) grp
                """;
        try (
                var connection = open();
                var preparedStatement = connection.prepareStatement(query)
        ) {
            preparedStatement.setString(1, title);
            preparedStatement.setString(2, title);

            var resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {

                log.trace("Dropped event by its title: " + title);

                return new Pair<>(
                        resultSet.getObject("grp", String.class),

                        new CalendarEvent(
                                resultSet.getObject("title", String.class),
                                resultSet.getObject("description", String.class),
                                resultSet.getObject("location", String.class),
                                resultSet.getObject("start_dt", Date.class),
                                resultSet.getObject("end_dt", Date.class)
                        )
                );
            }
        }

        log.warn("In dropEvent query event with title (" + title + ") does not exist, returning null!");
        return null;
    }

    @Override
    public List<Pair<String, CalendarEvent>> loadEventsByUser(User user) throws Exception {
        var query = """
                SELECT title, description, location, start_dt, end_dt,
                    (SELECT label FROM "group" WHERE event.group_id = id) "group" FROM event
                WHERE (SELECT label FROM "group" WHERE event.group_id = id) = ?
                """;
        var events = new LinkedList<Pair<String, CalendarEvent>>();
        try (var connection = open()) {
            for (var group : user.groups()) {
                try (var preparedStatement = connection.prepareStatement(query)) {
                    preparedStatement.setString(1, group);
                    formEventList(events, preparedStatement.executeQuery());
                }
            }
        }

        log.trace("Loaded events list by the given user");
        return events;
    }

    @Override
    public List<Pair<String, CalendarEvent>> loadEventsByGroup(String group) throws Exception {
        var query = """
                SELECT title, description, location, start_dt, end_dt,
                    (SELECT label FROM "group" WHERE event.group_id = id) "group" FROM event
                WHERE (SELECT label FROM "group" WHERE event.group_id = id) = ?
                """;
        var events = new LinkedList<Pair<String, CalendarEvent>>();
        try (
                var connection = open();
                var preparedStatement = connection.prepareStatement(query)
        ) {
            preparedStatement.setString(1, group);
            formEventList(events, preparedStatement.executeQuery());
        }

        log.trace("Loaded events list by the given group: " + group);
        return events;
    }

    @Override
    public List<Pair<String, CalendarEvent>> loadEventsByDay(LocalDate date) throws Exception {

        log.trace("Loaded events list by the given day: " + date);
        return getEventsFromInterval(
                date.atStartOfDay(),
                date.atTime(23, 59, 59, 999999999)
        );
    }

    @Override
    public List<Pair<String, CalendarEvent>> loadEventsIn(LocalDateTime start, LocalDateTime end)
            throws Exception {

        log.trace("Loaded events list by the given duration: " + start + " - " + end);
        return getEventsFromInterval(start, end);
    }

    @Override
    public List<Pair<String, CalendarEvent>> loadEventsDuring(LocalDateTime time) throws Exception {
        log.trace("Loaded actual events list by the given time: " + time);
        return getEventsFromInterval(time, time);
    }

    private List<Pair<String, CalendarEvent>> getEventsFromInterval(LocalDateTime start, LocalDateTime end)
            throws SQLException {
        var query = """
                SELECT title, description, location, start_dt, end_dt,
                    (SELECT label FROM "group" WHERE event.group_id = id) "group" FROM event
                WHERE start_dt <= ? AND ? <= end_dt;
                """;
        var events = new LinkedList<Pair<String, CalendarEvent>>();
        try (
                var connection = open();
                var preparedStatement = connection.prepareStatement(query)
        ) {
            preparedStatement.setTimestamp(1, Timestamp.valueOf(end));
            preparedStatement.setTimestamp(2, Timestamp.valueOf(start));

            var resultSet = preparedStatement.executeQuery();
            formEventList(events, resultSet);
        }

        return events;
    }

    @Override
    public LinkedList<Pair<String, CalendarEvent>> loadEventsByTitle(String title) throws SQLException {
        var query = """
                SELECT title, description, location, start_dt, end_dt,
                    (SELECT label FROM "group" WHERE event.group_id = id) "group" FROM event
                WHERE title = ?;
                """;
        var events = new LinkedList<Pair<String, CalendarEvent>>();
        try (
                var connection = open();
                var preparedStatement = connection.prepareStatement(query)
        ) {
            preparedStatement.setString(1, title);
            var resultSet = preparedStatement.executeQuery();
            formEventList(events, resultSet);
        }

        log.trace("Loaded events list by the given title: " + title);
        return events;
    }

    private void formEventList(LinkedList<Pair<String, CalendarEvent>> events, ResultSet resultSet)
            throws SQLException {
        while (resultSet.next())
            events.add(new Pair<>(
                            resultSet.getObject("group", String.class),
                            new CalendarEvent(
                                    resultSet.getObject("title", String.class),
                                    resultSet.getObject("description", String.class),
                                    resultSet.getObject("location", String.class),
                                    resultSet.getObject("start_dt", Date.class),
                                    resultSet.getObject("end_dt", Date.class)
                            )
                    )
            );
    }

    @Override
    public List<Pair<String, CalendarEvent>> loadAllEvents() throws SQLException {
        var query = """
                SELECT title, description, location, start_dt, end_dt,
                    (SELECT label FROM "group" WHERE event.group_id = id) "group" FROM event
                ORDER BY start_dt;
                """;

        var events = new LinkedList<Pair<String, CalendarEvent>>();
        try (
                var connection = open();
                var preparedStatement = connection.prepareStatement(query)
        ) {
            var resultSet = preparedStatement.executeQuery();
            formEventList(events, resultSet);
        }

        log.trace("Loaded all events list");
        return events;
    }

    @Override
    public void addUser(User user) throws SQLException {
        var firstQuery = """
                INSERT INTO "user"(email, password, role_id)
                VALUES (?, ?, (SELECT id FROM "role" WHERE label = ?));
                                
                                
                """;
        var secondQuery = """
                INSERT INTO user_group(user_id, group_id)
                VALUES ((SELECT id FROM "user" WHERE email = ?), (SELECT id FROM "group" WHERE label = ?))
                """;
        try (
                var connection = open();
                var preparedStatement = connection.prepareStatement(firstQuery)
        ) {
            preparedStatement.setString(1, user.email());
            preparedStatement.setString(2, user.password());
            preparedStatement.setString(3, user.role());

            preparedStatement.execute();

            for (var group : user.groups())
                try (var pStatement = connection.prepareStatement(secondQuery)) {
                    pStatement.setString(1, user.email());
                    pStatement.setString(2, group);
                    pStatement.execute();
                }
        }

        log.trace("Saved user");
    }

    @Override
    public void assignUsersToGroup(List<User> users, String group) throws SQLException {
        var query = """
                INSERT INTO user_group(user_id, group_id)
                VALUES ((SELECT id FROM "user" WHERE email = ?), (SELECT id FROM "group" WHERE label = ?))
                 """;

        try (var connection = open()) {
            for (User user : users) {
                user.groups().add(group);

                try (var pStatement = connection.prepareStatement(query)) {
                    pStatement.setString(1, user.email());
                    pStatement.setString(2, group);
                    pStatement.execute();
                }
            }
        }

        log.trace("Assigned list of users to the group: " + group);
    }

    @Override
    public User dropUser(String email) throws SQLException {
        var query = """
                DELETE FROM "user"
                WHERE email = ?
                RETURNING email, password, (SELECT label FROM "role" WHERE "role".id =
                    (SELECT role_id FROM "user" WHERE email = ?)) rl
                """;

        try (
                var connection = open();
                var preparedStatement = connection.prepareStatement(query)
        ) {
            preparedStatement.setString(1, email);
            preparedStatement.setString(2, email);

            var resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {

                log.trace("Dropped user");
                return new User(
                        resultSet.getObject("email", String.class),
                        resultSet.getObject("password", String.class),
                        new LinkedList<>(),
                        resultSet.getObject("rl", String.class)
                );
            }
        }

        log.warn("In dropUser query user with email: " + email + " does not exist, returning null!");
        return null;
    }

    @Override
    public List<User> getUsers() throws SQLException {
        var query = """
                SELECT email, password,
                    (SELECT label FROM "role" WHERE "role".id = "user".role_id) rl FROM "user";
                """;

        var users = new LinkedList<User>();

        try (
                var connection = open();
                var preparedStatement = connection.prepareStatement(query)
        ) {
            var resultSet = preparedStatement.executeQuery();
            getUsersFromResultSet(users, connection, resultSet);
        }

        log.trace("Loaded users");
        return users;
    }

    @Override
    public List<User> getUsersByGroup(String group) throws SQLException {
        var query = """
                SELECT email, password, (SELECT label FROM "role" WHERE "role".id = "user".role_id) rl FROM "user"
                                         WHERE id IN (SELECT user_id FROM user_group
                                                            WHERE group_id = (SELECT id FROM "group"
                                                                              WHERE label = ?));
                """;
        List<User> users = new LinkedList<>();
        try (
                var connection = open();
                var preparedStatement = connection.prepareStatement(query)
        ) {
            preparedStatement.setString(1, group);
            var resultSet = preparedStatement.executeQuery();
            getUsersFromResultSet(users, connection, resultSet);
        }

        log.trace("Loaded users by the given group: " + group);
        return users;
    }

    private void getUsersFromResultSet(List<User> users, Connection connection, ResultSet resultSet)
            throws SQLException {
        var query = """
                SELECT label FROM "group"
                WHERE id IN (SELECT group_id FROM user_group
                            WHERE user_id = (SELECT id FROM "user"
                                            WHERE email = ?))
                """;

        while (resultSet.next()) {

            var user = new User(
                    resultSet.getObject("email", String.class),
                    resultSet.getObject("password", String.class),
                    new LinkedList<>(),
                    resultSet.getObject("rl", String.class)
            );

            try (var pStatement = connection.prepareStatement(query)) {
                pStatement.setString(1, user.email());
                var resultSet1 = pStatement.executeQuery();
                while (resultSet1.next())
                    user.groups().add(resultSet1.getObject("label", String.class));
            }

            users.add(user);
        }
    }

    @Override
    public List<User> getUsersByRole(String role) throws SQLException {
        var query = """
                SELECT email, password, (SELECT label FROM "role" WHERE "role".id = "user".role_id) rl FROM "user"
                                         WHERE role_id = (SELECT id FROM role
                                                          WHERE label = ?);
                """;
        var users = new LinkedList<User>();
        try (
                var connection = open();
                var preparedStatement = connection.prepareStatement(query)
        ) {
            preparedStatement.setString(1, role);
            var resultSet = preparedStatement.executeQuery();
            getUsersFromResultSet(users, connection, resultSet);
        }

        log.trace("Loaded users by the given role: " + role);
        return users;
    }
}
