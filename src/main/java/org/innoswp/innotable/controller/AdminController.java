package org.innoswp.innotable.controller;

import org.innoswp.innotable.model.event.AdminEvent;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class AdminController {

    @PutMapping(value = "/api/create_event",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public HttpStatus getEvent(@RequestBody AdminEvent adminEvent) {
        System.out.println(adminEvent);
        return HttpStatus.OK;
    }


}
