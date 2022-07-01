package org.innoswp.innotable.model;

import org.innoswp.innotable.model.event.CalendarEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JdbcModelTest {
    @Autowired
    private JdbcModel testModel;
    private final Pair<String, CalendarEvent> event1 = new Pair<>("bachelor", new CalendarEvent("DROP", "DROP of bachelors from IU", "IU", new Date(2022 - 1900, Calendar.JULY, 1, 9, 3, 50), new Date(2022 - 1900, Calendar.JULY, 2, 9, 3, 50)));
    private final Pair<String, CalendarEvent> event2 = new Pair<>("master", new CalendarEvent("RETAKE", "RETAKE for all master students", "108", new Date(2022 - 1900, Calendar.JULY, 3, 9, 0, 0), new Date(2022 - 1900, Calendar.JULY, 3, 15, 0, 0)));
    private final Pair<String, CalendarEvent> event3 = new Pair<>("319", new CalendarEvent("VACATION", "VACATION for all 319 staff", "319", new Date(2022 - 1900, Calendar.JULY, 4, 0, 0, 0), new Date(2022 - 1900, Calendar.JULY, 11, 0, 0, 0)));
    private final Pair<String, CalendarEvent> event4 = new Pair<>("phd", new CalendarEvent("EXTRA LECTURE", "phd students have extra lecture on ***", "107", new Date(2022 - 1900, Calendar.JULY, 6, 9, 0, 0), new Date(2022 - 1900, Calendar.JULY, 6, 10, 30, 0)));
    private final Pair<String, CalendarEvent> event5 = new Pair<>("bachelor", new CalendarEvent("RETAKE1", "RETAKE on AGLA", "IU", new Date(2022 - 1900, Calendar.JUNE, 30, 9, 0, 0), new Date(2022 - 1900, Calendar.JUNE, 30, 12, 0, 0)));
    private final User user1 = new User("user1@innopolis.university", "pwd0", new ArrayList<>(List.of("319")), "staff");
    private final User user2 = new User("user2@innopolis.university", "pwd1", new ArrayList<>(List.of("319", "phd")), "staff");
    private final User user3 = new User("user3@innopolis.university", "pwd2", new ArrayList<>(List.of("319", "master")), "student");
    private final User user4 = new User("user4@innopolis.university", "pwd3", new ArrayList<>(List.of("master")), "student");
    private final User user5 = new User("user5@innopolis.university", "pwd4", new ArrayList<>(List.of("bachelor")), "student");
    private final String group1 = "bachelor";
    private final String group2 = "master";
    private final String group3 = "phd";
    private final String group4 = "319";
    private final String role1 = "staff";
    private final String role2 = "student";
    private final Pair<String, CalendarEvent> newEvent = new Pair<>(group2, new CalendarEvent("EXTRA LECTURE", "master students have extra lecture on ***", "106", new Date(2022 - 1900, Calendar.JULY, 7, 9, 0, 0), new Date(2022 - 1900, Calendar.JULY, 8, 10, 30, 0)));
    private final User newUser = new User("new@iu", "pwd_new", new ArrayList<>(List.of(group4, group1)), "staff");

    void execute(String query) throws SQLException {
        try (
                var connection = testModel.open();
                var preparedStatement = connection.prepareStatement(query)
        ) {
            preparedStatement.execute();
        }
    }

    void clear() throws SQLException {
        execute("""
                DELETE FROM event;
                DELETE FROM "user";
                DELETE FROM "group";
                DELETE FROM role;
                """);
    }

    void fill() throws SQLException {
        execute("""
                INSERT INTO role(label)
                VALUES ('staff'),
                        ('student');
                INSERT INTO "user"(email, password, role_id)
                VALUES ('user1@innopolis.university', 'pwd0', (SELECT id FROM role WHERE label = 'staff')),
                ('user2@innopolis.university', 'pwd1', (SELECT id FROM role WHERE label = 'staff')),
                ('user3@innopolis.university', 'pwd2', (SELECT id FROM role WHERE label = 'student')),
                ('user4@innopolis.university', 'pwd3', (SELECT id FROM role WHERE label = 'student')),
                ('user5@innopolis.university', 'pwd4', (SELECT id FROM role WHERE label = 'student'));
                INSERT INTO "group"(label)
                VALUES ('bachelor'),
                        ('master'),
                        ('phd'),
                        ('319');
                INSERT INTO user_group(user_id, group_id)
                VALUES ((SELECT id FROM "user" WHERE email LIKE '%1%'), (SELECT id FROM "group" WHERE label = '319')),
                ((SELECT id FROM "user" WHERE email LIKE '%2%'), (SELECT id FROM "group" WHERE label = '319')),
                ((SELECT id FROM "user" WHERE email LIKE '%2%'), (SELECT id FROM "group" WHERE label = 'phd')),
                ((SELECT id FROM "user" WHERE email LIKE '%3%'), (SELECT id FROM "group" WHERE label = '319')),
                ((SELECT id FROM "user" WHERE email LIKE '%3%'), (SELECT id FROM "group" WHERE label = 'master')),
                ((SELECT id FROM "user" WHERE email LIKE '%4%'), (SELECT id FROM "group" WHERE label = 'master')),
                ((SELECT id FROM "user" WHERE email LIKE '%5%'), (SELECT id FROM "group" WHERE label = 'bachelor'));
                                
                INSERT INTO event(title, description, location, start_dt, end_dt, group_id)
                VALUES ('DROP',   'DROP of bachelors from IU',              'IU', '2022-07-01 09:03:50.000000 +00:00', '2022-07-02 09:03:50.000000 +00:00', (SELECT id FROM "group" WHERE label = 'bachelor')),
                ('RETAKE',        'RETAKE for all master students',         '108', '2022-07-03 09:00:00.000000 +00:00', '2022-07-03 15:00:00.000000 +00:00', (SELECT id FROM "group" WHERE label = 'master')),
                ('VACATION',      'VACATION for all 319 staff',             '319', '2022-07-04 00:00:00.000000 +00:00', '2022-07-11 00:00:00.000000 +00:00', (SELECT id FROM "group" WHERE label = '319')),
                ('EXTRA LECTURE', 'phd students have extra lecture on ***', '107', '2022-07-06 09:00:00.000000 +00:00', '2022-07-06 10:30:00.000000 +00:00', (SELECT id FROM "group" WHERE label = 'phd')),
                ('RETAKE1',       'RETAKE on AGLA',                         'IU', '2022-06-30 09:00:00.000000 +00:00', '2022-06-30 12:00:00.000000 +00:00', (SELECT id FROM "group" WHERE label = 'bachelor'));
                """);
    }

    @Test
    void getGroups() throws SQLException {
        clear();
        assertEquals(0, testModel.getGroups().size());
        fill();
        var expected = new String[]{group4, group1, group2, group3};
        var got = testModel.getGroups();
        assertEquals(4, got.size());
        for (int i = 0; i < 4; i++) assertEquals(expected[i], got.get(i));
    }

    @Test
    void saveGroup() throws SQLException {
        clear();
        fill();
        String NEW_GROUP = "new group";
        testModel.saveGroup(NEW_GROUP);
        var expected = new String[]{group4, group1, group2, NEW_GROUP, group3};
        var got = testModel.getGroups();
        assertEquals(5, got.size());
        for (int i = 0; i < 5; i++) assertEquals(expected[i], got.get(i));
    }

    @Test
    void dropGroup() throws SQLException {
        clear();
        fill();
        testModel.dropGroup(group1);
        var expected = new String[]{group4, group2, group3};
        var got = testModel.getGroups();
        assertEquals(3, got.size());
        for (int i = 0; i < 3; i++) assertEquals(expected[i], got.get(i));
    }

    @Test
    void getRoles() throws SQLException {
        clear();
        assertEquals(0, testModel.getGroups().size());
        fill();
        var expected = new String[]{role1, role2};
        var got = testModel.getRoles();
        assertEquals(2, got.size());
        for (int i = 0; i < 2; i++) assertEquals(expected[i], got.get(i));
    }

    @Test
    void saveRole() throws SQLException {
        clear();
        fill();
        String NEW_ROLE = "new role";
        testModel.saveRole(NEW_ROLE);
        var expected = new String[]{NEW_ROLE, role1, role2};
        var got = testModel.getRoles();
        assertEquals(3, got.size());
        for (int i = 0; i < 3; i++) assertEquals(expected[i], got.get(i));
    }

    @Test
    void saveEvent() throws SQLException {
        clear();
        fill();
        testModel.saveEvent(newEvent.second(), newEvent.first());
        var expected = new Pair[]{event5, event1, event2, event3, event4, newEvent};
        var got = testModel.loadAllEvents();
        assertEquals(6, got.size());
        for (int i = 0; i < 6; i++) assertEquals(expected[i], got.get(i));
    }

    @Test
    void dropEvent() throws SQLException {
        clear();
        fill();
        testModel.dropEvent(event2.second().title());
        var expected = new Pair[]{event5, event1, event3, event4};
        var got = testModel.loadAllEvents();
        assertEquals(4, got.size());
        for (int i = 0; i < 4; i++) assertEquals(expected[i], got.get(i));
    }

    @Test
    void loadEventsByUser() throws SQLException {
        clear();
        var users = new User[]{user1, user2, user3, user4, user5, new User("wrong@innopolis.university", "pwd", new ArrayList<>(), "staff")};
        for (var user : users)
            assertEquals(0, testModel.loadEventsByUser(user).size());
        fill();
        var expected = new Pair[][]{{event3}, {event3, event4}, {event3, event2}, {event2}, {event5, event1}, {}};
        for (int i = 0; i < users.length; i++) {
            var got = testModel.loadEventsByUser(users[i]);
            assertEquals(got.size(), expected[i].length);
            for (int j = 0; j < expected[i].length; j++) {
                assertEquals(expected[i][j], got.get(j));
            }
        }
    }

    @Test
    void loadEventsByGroup() throws SQLException {
        clear();
        var groups = new String[]{group1, group2, group3, group4, "wrong group"};
        for (String group : groups)
            assertEquals(0, testModel.loadEventsByGroup(group).size());
        fill();
        var expected = new Pair[][]{{event5, event1}, {event2}, {event4}, {event3}, {}};
        for (int i = 0; i < groups.length; i++) {
            var got = testModel.loadEventsByGroup(groups[i]);
            assertEquals(got.size(), expected[i].length);
            for (int j = 0; j < expected[i].length; j++) {
                assertEquals(expected[i][j], got.get(j));
            }
        }
    }

    @Test
    void loadEventsIn() throws SQLException {
        clear();
        var time = new Date[]{new Date(2022 - 1900, Calendar.JUNE, 30, 0, 0, 0),
                new Date(2022 - 1900, Calendar.JUNE, 30, 10, 0, 0),
                new Date(2022 - 1900, Calendar.JULY, 6, 9, 1, 0),
                new Date(2023 - 1900, Calendar.JULY, 6, 9, 0, 0)};

        assertEquals(0, testModel.loadEventsIn(time[0], time[3]).size());
        fill();
        var expected = new Pair[][]{{event5, event1, event2, event3, event4}, {event5}, {event3, event4}};
        var got = testModel.loadEventsIn(time[0], time[3]);
        assertEquals(expected[0].length, got.size());
        for (int j = 0; j < expected[0].length; j++) {
            assertEquals(expected[0][j], got.get(j));
        }
        got = testModel.loadEventsIn(time[0], time[1]);
        assertEquals(expected[1].length, got.size());
        for (int j = 0; j < expected[1].length; j++) {
            assertEquals(expected[1][j], got.get(j));
        }
        got = testModel.loadEventsIn(time[1], time[2]);
        assertEquals(expected[0].length, got.size());
        for (int j = 0; j < expected[0].length; j++) {
            assertEquals(expected[0][j], got.get(j));
        }
        got = testModel.loadEventsIn(time[2], time[3]);
        assertEquals(expected[2].length, got.size());
        for (int j = 0; j < expected[2].length; j++) {
            assertEquals(expected[2][j], got.get(j));
        }
    }

    @Test
    void loadEventsDuring() throws SQLException {
        clear();
        var time = new Date[]{new Date(2022 - 1900, Calendar.JUNE, 30, 10, 0, 0), new Date(2022 - 1900, Calendar.JULY, 6, 9, 1, 0), new Date(2023 - 1900, Calendar.JULY, 6, 9, 0, 0)};
        for (var t : time)
            assertEquals(0, testModel.loadEventsDuring(t).size());
        fill();
        var expected = new Pair[][]{{event5}, {event3, event4}, {}};
        for (int i = 0; i < time.length; i++) {
            var got = testModel.loadEventsDuring(time[i]);
            assertEquals(expected[i].length, got.size());
            for (int j = 0; j < expected[i].length; j++) {
                assertEquals(expected[i][j], got.get(j));
            }
        }
    }

    @Test
    void loadEventsByTitle() throws SQLException {
        clear();
        var titles = new String[]{"retake", "drop", "vacation", "lecture", "retake1", "wrong title", "r"};
        for (String title : titles)
            assertEquals(0, testModel.loadEventsByTitle(title).size());
        fill();
        var expected = new Pair[][]{{event5, event2}, {event1}, {event3}, {event4}, {event5}, {}, {event5, event1, event2, event4}};
        for (int i = 0; i < titles.length; i++) {
            var got = testModel.loadEventsByTitle(titles[i]);
            assertEquals(expected[i].length, got.size());
            for (int j = 0; j < expected[i].length; j++) {
                assertEquals(expected[i][j], got.get(j));
            }
        }
    }

    @Test
    void loadAllEvents() throws SQLException {
        clear();
        assertEquals(0, testModel.loadAllEvents().size());
        fill();
        var expectedResults = new Pair[]{event5, event1, event2, event3, event4};
        var loaded = testModel.loadAllEvents();
        assertEquals(5, loaded.size());
        for (int i = 0; i < 5; i++) {
            assertEquals(expectedResults[i], loaded.get(i));
        }
    }

    @Test
    void getUsers() throws SQLException {
        clear();
        assertEquals(0, testModel.loadAllEvents().size());
        fill();
        var expectedResults = new User[]{user1, user2, user3, user4, user5};
        var loaded = testModel.getUsers();
        assertEquals(5, loaded.size());
        for (int i = 0; i < 5; i++) {
            assertEquals(expectedResults[i], loaded.get(i));
        }

    }

    @Test
    void addUser() throws SQLException {
        clear();
        fill();
        testModel.addUser(newUser);
        var expected = new User[]{user1, user2, user3, user4, user5, newUser};
        var got = testModel.getUsers();
        assertEquals(6, got.size());
        for (int i = 0; i < 6; i++) assertEquals(expected[i], got.get(i));
    }

    @Test
    void assignUsersToGroup() throws SQLException {
        clear();
        fill();
        var query = """
                SELECT id FROM user_group
                WHERE (user_id = (SELECT id FROM "user" WHERE email LIKE ?)
                OR user_id = (SELECT id FROM "user" WHERE email LIKE ?))
                AND group_id = (SELECT id FROM "group" WHERE label LIKE ?)
                """;
        ResultSet resultSet;
        try (
                var connection = testModel.open();
                var preparedStatement = connection.prepareStatement(query)
        ) {
            preparedStatement.setString(1, "%1%");
            preparedStatement.setString(2, "%2%");
            preparedStatement.setString(3, "bach%");
            resultSet = preparedStatement.executeQuery();
            assertFalse(resultSet.next());
            testModel.assignUsersToGroup(new ArrayList<>(List.of(user1, user2)), group1);
            resultSet = preparedStatement.executeQuery();
            assertTrue(resultSet.next());
            assertTrue(resultSet.next());
            assertFalse(resultSet.next());
        }
    }

    @Test
    void dropUser() throws SQLException {
        clear();
        fill();
        testModel.dropUser(user4.email());
        var expected = new User[]{user1, user2, user3, user5};
        var got = testModel.getUsers();
        assertEquals(4, got.size());
        for (int i = 0; i < 4; i++) assertEquals(expected[i], got.get(i));
    }

    @Test
    void getUsersByGroup() throws SQLException {
        clear();
        var groups = new String[]{group1, group2, group3, group4, "wrong group"};
        for (String group : groups) {
            assertEquals(0, testModel.getUsersByGroup(group).size());
        }
        fill();
        var expected = new User[][]{{user5}, {user3, user4}, {user2}, {user1, user2, user3}, {}};
        for (int i = 0; i < groups.length; i++) {
            var got = testModel.getUsersByGroup(groups[i]);
            assertEquals(got.size(), expected[i].length);
            for (int j = 0; j < expected[i].length; j++) {
                assertEquals(expected[i][j], got.get(j));
            }
        }
    }

    @Test
    void getUsersByRole() throws SQLException {
        clear();
        var roles = new String[]{role1, role2, "wrong role"};
        for (String role : roles) {
            assertEquals(0, testModel.getUsersByRole(role).size());
        }
        fill();
        var expected = new User[][]{{user1, user2}, {user3, user4, user5}, {}};
        for (int i = 0; i < roles.length; i++) {
            var got = testModel.getUsersByRole(roles[i]);
            assertEquals(got.size(), expected[i].length);
            for (int j = 0; j < expected[i].length; j++) {
                assertEquals(expected[i][j], got.get(j));
            }
        }
    }
}