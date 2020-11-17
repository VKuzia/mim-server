package com.mimteam.mimserver.handlers;

import com.google.common.eventbus.Subscribe;
import com.mimteam.mimserver.events.SendTextMessageEvent;
import com.mimteam.mimserver.repositories.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.mimteam.mimserver.model.entities.ChatMessageEntity;

@Component
public class DatabaseManager {
    private final ChatMessageRepository chatMessageRepository;

    @Autowired
    public DatabaseManager(ChatMessageRepository chatMessageRepository) {
        this.chatMessageRepository = chatMessageRepository;
    }

    @Subscribe
    public void saveChatMessage(SendTextMessageEvent event) {
        ChatMessageEntity entity = new ChatMessageEntity();
        entity.setChatId(event.getChatId());
        entity.setSenderId(event.getUserId());
        entity.setContent(event.getContent());
        entity.setDateTime(event.getDateTime());
        chatMessageRepository.save(entity);
    }
}
