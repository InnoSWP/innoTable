package org.innoswp.innotable.controller;

import org.innoswp.innotable.controller.forwarder.EwsForwarder;
import org.innoswp.innotable.model.JdbcModel;
import org.innoswp.innotable.model.event.AdminEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
public class AdminController {

    @Autowired
    private JdbcModel model;

    @Autowired
    private EwsForwarder forwarder;

    @PutMapping(value = "/api/create_event",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public HttpStatus getEvent(@RequestBody AdminEvent adminEvent) throws Exception {

        var calendarEvent = adminEvent.toCalendarEvent();

        // TODO - fix the logic of sending events by group (duplicating events)
        for (var group : adminEvent.getGroups())
            forwarder.pushEventsForGroup(group, List.of(calendarEvent));

        return HttpStatus.OK;
    }
}
