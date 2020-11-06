package com.mimteam.mimserver.handlers;

import com.google.common.eventbus.Subscribe;
import com.mimteam.mimserver.events.JoinChatEvent;
import com.mimteam.mimserver.events.LeaveChatEvent;
import com.mimteam.mimserver.events.SendTextMessageEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class ChatMessageBroadcaster {
    private final SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    public ChatMessageBroadcaster(SimpMessagingTemplate simpMessagingTemplate) {
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

    @Subscribe
    public void sendTextMessage(@NotNull SendTextMessageEvent event) {
        simpMessagingTemplate.convertAndSend("/chats/" + event.getChatId(),
                event.getMessage());
    }
}
