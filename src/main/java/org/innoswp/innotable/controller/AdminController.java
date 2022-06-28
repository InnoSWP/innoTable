package org.innoswp.innotable.controller;

import org.innoswp.innotable.controller.forwarder.EventForwarder;
import org.innoswp.innotable.controller.forwarder.EwsForwarder;
import org.innoswp.innotable.model.JdbcModel;
import org.innoswp.innotable.model.event.AdminEvent;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
public class AdminController {

    private final JdbcModel model = new JdbcModel();
    private final EventForwarder forwarder = new EwsForwarder(model);

    @PutMapping(value = "/api/create_event",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public HttpStatus getEvent(@RequestBody AdminEvent adminEvent) throws Exception {

        var calendarEvent = adminEvent.toCalendarEvent();

        for (var group : adminEvent.getGroups())
            forwarder.pushEventsForGroup(group, List.of(calendarEvent));

        return HttpStatus.OK;
    }
}
