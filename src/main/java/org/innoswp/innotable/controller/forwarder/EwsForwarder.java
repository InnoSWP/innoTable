package org.innoswp.innotable.controller.forwarder;

import lombok.extern.slf4j.Slf4j;
import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion;
import microsoft.exchange.webservices.data.core.service.item.Appointment;
import microsoft.exchange.webservices.data.credential.WebCredentials;
import microsoft.exchange.webservices.data.property.complex.MessageBody;
import org.innoswp.innotable.model.User;
import org.innoswp.innotable.model.event.CalendarEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;

@Slf4j
@Component
public class EwsForwarder implements EventForwarder {

    private final ExchangeService service = new ExchangeService(ExchangeVersion.Exchange2010_SP2);

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
    public void pushEventForUser(User user, CalendarEvent calendarEvent) throws Exception {
        var appointment = createAppointment(calendarEvent);

        appointment.getRequiredAttendees().add(user.email());
        appointment.save();

        log.info("Pushed event " + calendarEvent.title() + " for user");
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