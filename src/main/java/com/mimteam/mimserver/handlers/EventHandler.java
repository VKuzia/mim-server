package com.mimteam.mimserver.handlers;

import com.google.common.eventbus.EventBus;
import com.mimteam.mimserver.events.ChatEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EventHandler {
    private final EventBus eventBus;

    private final ChatMessageBroadcaster chatMessageBroadcaster;

    @Autowired
    public EventHandler(ChatMessageBroadcaster chatMessageBroadcaster) {
        this.eventBus = new EventBus();
        this.chatMessageBroadcaster = chatMessageBroadcaster;

        registerListeners();
    }

    public void post(ChatEvent chatEvent) {
        eventBus.post(chatEvent);
    }

    private void registerListeners() {
        eventBus.register(chatMessageBroadcaster);
    }
}
