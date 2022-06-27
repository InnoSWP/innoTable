package org.innoswp.innotable.controller.forwarder;

import lombok.extern.slf4j.Slf4j;
import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion;
import microsoft.exchange.webservices.data.core.service.item.Appointment;
import microsoft.exchange.webservices.data.credential.WebCredentials;
import microsoft.exchange.webservices.data.property.complex.MessageBody;
import org.innoswp.innotable.model.Model;
import org.innoswp.innotable.model.Pair;
import org.innoswp.innotable.model.User;
import org.innoswp.innotable.model.event.CalendarEvent;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Properties;

@Slf4j
public class EwsForwarder implements EventForwarder {

    private static final Properties properties = new Properties();

    static {
        try {
            properties.load(new BufferedReader(new FileReader("src/main/resources/application.properties")));
        } catch (IOException e) {
            log.error("Cannot read from application.properties file");
            throw new RuntimeException(e);
        }

    }

    private final ExchangeService service = new ExchangeService(ExchangeVersion.Exchange2010_SP2);

    private final Model model;

    private final String SERVICE_URL = properties.getProperty("ews.service.url");

    private final String SERVICE_EMAIL = properties.getProperty("ews.service.email");

    private final String SERVICE_PASSWORD = properties.getProperty("ews.service.password");

    {
        service.setUrl(URI.create(SERVICE_URL));

        service.setCredentials(new WebCredentials(
                SERVICE_EMAIL,
                SERVICE_PASSWORD
        ));

        service.setPreAuthenticate(true);

        log.info("Configuration of EwsForwarder service completed");
    }

    public EwsForwarder(Model model) {
        this.model = model;
    }

    @Override
    public void pushEventForUser(User user, List<CalendarEvent> userEvents) throws Exception {
        for (var event : userEvents) {
            var appointment = createAppointment(event);

            appointment.getRequiredAttendees().add(user.email());
            appointment.save();

            log.info("Pushed event " + event.title() + " for user");
        }
    }

    @Override
    public void pushEventsForGroup(String group, List<CalendarEvent> groupEvents) throws Exception {
        for (var user : model.getUsersByGroup(group))
            pushEventForUser(user, groupEvents);

        log.info("Pushed events for group " + group);
    }

    @Override
    public void pushAllEvents(List<Pair<String, List<CalendarEvent>>> events) throws Exception {
        for (var pair : events)
            pushEventsForGroup(pair.first(), pair.second());

        log.info("Pushed all events");
    }

    private Appointment createAppointment(CalendarEvent event) throws Exception {
        var appointment = new Appointment(service);

        appointment.setSubject(event.title());
        appointment.setBody(MessageBody.getMessageBodyFromText(event.description()));

        appointment.setLocation(event.location());

        appointment.setStart(event.startTime());
        appointment.setEnd(event.endTime());

        return appointment;
    }
}