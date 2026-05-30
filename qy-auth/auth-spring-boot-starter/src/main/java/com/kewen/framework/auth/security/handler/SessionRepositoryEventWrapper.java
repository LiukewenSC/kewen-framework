package com.kewen.framework.auth.security.handler;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.session.Session;
import org.springframework.session.SessionRepository;
import org.springframework.session.events.SessionCreatedEvent;
import org.springframework.session.events.SessionDestroyedEvent;

public class SessionRepositoryEventWrapper implements SessionRepository, ApplicationEventPublisherAware {

    SessionRepository wrapper;

    public SessionRepositoryEventWrapper(SessionRepository wrapper) {
        this.wrapper = wrapper;
    }

    ApplicationEventPublisher applicationEventPublisher;


    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public Session createSession() {
        Session session = wrapper.createSession();
        applicationEventPublisher.publishEvent(new SessionCreatedEvent(session,session));
        return session;
    }

    @Override
    public void save(Session session) {
        wrapper.save(session);
        applicationEventPublisher.publishEvent(new SessionCreatedEvent(session,session));
    }

    @Override
    public Session findById(String id) {
        Session session = wrapper.findById(id);
        return session;
    }

    @Override
    public void deleteById(String id) {
        Session session = wrapper.findById(id);
        wrapper.deleteById(id);
        applicationEventPublisher.publishEvent(new SessionDestroyedEvent(session, session));
    }
}
