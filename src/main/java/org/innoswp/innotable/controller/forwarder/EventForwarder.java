package org.innoswp.innotable.controller.forwarder;

import org.innoswp.innotable.model.data.Event;

import java.util.List;

public interface EventForwarder {
    void push(Event event);

    void push(List<? extends Event> events);
}
