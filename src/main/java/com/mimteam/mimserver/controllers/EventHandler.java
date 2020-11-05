package com.mimteam.mimserver.controllers;

import com.google.common.eventbus.EventBus;
import com.mimteam.mimserver.controllers.events.Event;
import com.mimteam.mimserver.controllers.listeners.ChatListener;
import com.mimteam.mimserver.controllers.listeners.TextMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EventHandler {

    private EventBus eventBus;

    private final TextMessageListener textMessageListener;
    private final ChatListener chatListener;

    @Autowired
    public EventHandler(TextMessageListener textMessageListener, ChatListener chatListener) {
        this.textMessageListener = textMessageListener;
        this.chatListener = chatListener;

        this.eventBus = new EventBus();
        registerListeners();
    }

    public void post(Event event) {
        eventBus.post(event);
    }

    private void registerListeners() {
        eventBus.register(textMessageListener);
        eventBus.register(chatListener);
    }
}
