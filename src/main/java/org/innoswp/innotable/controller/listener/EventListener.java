package org.innoswp.innotable.controller.listener;

import org.innoswp.innotable.model.data.Event;
import org.innoswp.innotable.model.data.Group;

import java.util.List;

public interface EventListener {
    List<Event> getNewEvents();

    default List<Event> getNewEventsOf(Group group) {
        return getNewEvents().stream()
                .filter(x -> x.getGroup().equals(group))
                .toList();
    }
}
