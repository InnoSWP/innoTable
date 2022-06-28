package org.innoswp.innotable.controller.forwarder;

import lombok.extern.slf4j.Slf4j;
import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion;
import microsoft.exchange.webservices.data.core.service.item.Appointment;
import microsoft.exchange.webservices.data.credential.WebCredentials;
import microsoft.exchange.webservices.data.property.complex.MessageBody;
import org.innoswp.innotable.model.JdbcModel;
import org.innoswp.innotable.model.Pair;
import org.innoswp.innotable.model.User;
import org.innoswp.innotable.model.event.CalendarEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.List;

@Slf4j
@Component
public class EwsForwarder implements EventForwarder {

    private final ExchangeService service = new ExchangeService(ExchangeVersion.Exchange2010_SP2);

    @Autowired
    private JdbcModel model;

    public EwsForwarder(@Value("${ews.service.url}") String serviceUrl,
                        @Value("${ews.service.email}") String serviceEmail,
                        @Value("${ews.service.password}") String servicePassword) {
        service.setUrl(URI.create(serviceUrl));

        service.setCredentials(new WebCredentials(
                serviceEmail,
                servicePassword
        ));

        service.setPreAuthenticate(true);

        log.info("Configuration of EwsForwarder service completed");
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