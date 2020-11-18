package com.mimteam.mimserver.handlers;

import com.google.common.eventbus.EventBus;
import com.mimteam.mimserver.events.ChatEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EventHandler {
    private final EventBus eventBus;

    private final ChatMessageBroadcaster chatMessageBroadcaster;
    private final DatabaseManager databaseManager;

    @Autowired
    public EventHandler(ChatMessageBroadcaster chatMessageBroadcaster, DatabaseManager databaseManager) {
        this.chatMessageBroadcaster = chatMessageBroadcaster;
        this.databaseManager = databaseManager;
        this.eventBus = new EventBus();

        registerListeners();
    }

    public void post(ChatEvent chatEvent) {
        eventBus.post(chatEvent);
    }

    private void registerListeners() {
        eventBus.register(chatMessageBroadcaster);
        eventBus.register(databaseManager);
    }
}
