package org.innoswp.innotable.model;

import org.innoswp.innotable.model.data.Event;
import org.innoswp.innotable.model.data.Group;
import org.innoswp.innotable.model.data.Role;
import org.innoswp.innotable.model.data.User;

import java.util.List;

public interface Model {
    void addUser(User user);

    void pushEvent(Event event);

    List<User> getUsers();

    List<User> getUsers(Group group);

    List<User> getUsers(Role role);

    Event loadEvent(long id);

    List<Event> loadEvents();
}
