package com.mimteam.mimserver.controllers;

import com.google.common.eventbus.EventBus;
import com.mimteam.mimserver.events.ChatEvent;
import com.mimteam.mimserver.handlers.ChatMessageBroadcaster;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EventHandler {
    private final EventBus eventBus;

    private final ChatMessageBroadcaster chatMessageBroadcaster;

    @Autowired
    public EventHandler(ChatMessageBroadcaster chatMessageBroadcaster) {
        this.chatMessageBroadcaster = chatMessageBroadcaster;

        this.eventBus = new EventBus();
        registerListeners();
    }

    public void post(ChatEvent chatEvent) {
        eventBus.post(chatEvent);
    }

    private void registerListeners() {
        eventBus.register(chatMessageBroadcaster);
    }
}
