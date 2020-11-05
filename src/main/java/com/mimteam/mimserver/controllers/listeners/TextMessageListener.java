package com.mimteam.mimserver.controllers.listeners;

import com.google.common.eventbus.Subscribe;
import com.mimteam.mimserver.controllers.events.SendTextMessageEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class TextMessageListener {
    private final SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    public TextMessageListener(SimpMessagingTemplate simpMessagingTemplate) {
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @Subscribe
    public void sendTextMessage(@NotNull SendTextMessageEvent event) {
        simpMessagingTemplate.convertAndSend("/chats/" + event.getChatId(),
                event.getMessage());
    }
}
