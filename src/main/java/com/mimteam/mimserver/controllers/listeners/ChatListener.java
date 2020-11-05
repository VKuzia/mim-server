package com.mimteam.mimserver.controllers.listeners;

import com.google.common.eventbus.Subscribe;
import com.mimteam.mimserver.controllers.events.JoinChatEvent;
import com.mimteam.mimserver.controllers.events.LeaveChatEvent;
import com.mimteam.mimserver.controllers.events.SendTextMessageEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class ChatListener {
    private final SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    public ChatListener(SimpMessagingTemplate simpMessagingTemplate) {
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @Subscribe
    public void addUserToChat(@NotNull JoinChatEvent event) {
        System.out.println(event.getUserId() + " joined " + event.getChatId());
    }

    @Subscribe
    public void removeUserFromChat(@NotNull LeaveChatEvent event) {
        System.out.println(event.getUserId() + " left " + event.getChatId());
    }
}
