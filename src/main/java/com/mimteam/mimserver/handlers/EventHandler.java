package com.mimteam.mimserver.handlers;

import com.google.common.eventbus.EventBus;
import com.mimteam.mimserver.events.ChatEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EventHandler {
    private final EventBus eventBus;

    private final ChatMessageBroadcaster chatMessageBroadcaster;
    private final DatabaseEntityUpdater databaseEntityUpdater;

    @Autowired
    public EventHandler(ChatMessageBroadcaster chatMessageBroadcaster, DatabaseEntityUpdater databaseEntityUpdater) {
        this.chatMessageBroadcaster = chatMessageBroadcaster;
        this.databaseEntityUpdater = databaseEntityUpdater;
        this.eventBus = new EventBus();

        registerListeners();
    }

    public void post(ChatEvent chatEvent) {
        eventBus.post(chatEvent);
    }

    private void registerListeners() {
        eventBus.register(chatMessageBroadcaster);
        eventBus.register(databaseEntityUpdater);
    }
}
