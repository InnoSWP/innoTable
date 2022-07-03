package org.innoswp.innotable.controller;

import org.innoswp.innotable.controller.forwarder.EwsForwarder;
import org.innoswp.innotable.model.JdbcModel;
import org.innoswp.innotable.model.User;
import org.innoswp.innotable.pojo.AdminCredentials;
import org.innoswp.innotable.pojo.AdminEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.sql.SQLException;
import java.util.HashSet;


@RestController
public class AdminController {

    @Autowired
    private JdbcModel model;

    @Autowired
    private EwsForwarder forwarder;

    private boolean isLoggedIn = false;

    @RequestMapping("/")
    public ModelAndView index() {
        var modelAndView = new ModelAndView();

        if (!isLoggedIn) {
            modelAndView.setViewName("redirect:/login");
            return modelAndView;
        } else {
            return getCalendarEditor();
        }
    }

    @RequestMapping("/login")
    public ModelAndView login() {
        var modelAndView = new ModelAndView();

        if (!isLoggedIn) {
            modelAndView.setViewName("login.html");
            return modelAndView;
        } else {
            return getCalendarEditor();
        }
    }

    @PostMapping(value = "/login/send")
    public ResponseEntity<HttpStatus> validateCredentials(
            @RequestBody AdminCredentials request
    ) {
        try {
            var admins = model.getUsersByRole("admin");

            var isAdminExists = admins.stream().anyMatch(
                    admin -> admin.email().equals(request.email()) &&
                            admin.password().equals(request.password())
            );

            if (isAdminExists) {
                isLoggedIn = true;
                return new ResponseEntity<>(HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    @GetMapping("/calendar_editor")
    public ModelAndView getCalendarEditor() {
        var modelAndView = new ModelAndView();

        if (!isLoggedIn) {
            modelAndView.setViewName("redirect:/login");
        } else {
            modelAndView.setViewName("calendar_editor.html");
        }
        return modelAndView;
    }

    @PostMapping("/save_event")
    public ResponseEntity<HttpStatus> saveEvent(@RequestBody AdminEvent adminEvent) {
        var groups = adminEvent.getGroups();

        try {
            var calendarEvent = adminEvent.toCalendarEvent();

            var users = new HashSet<User>();

            for (var group : groups) {
                users.addAll(model.getUsersByGroup(group));
//                model.saveEvent(calendarEvent, group);
            }

            forwarder.pushEventForUsers(users, calendarEvent);


            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
