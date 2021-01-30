package com.mimteam.mimserver.model.dto;

import com.mimteam.mimserver.model.entities.chat.ChatEntity;

public class ChatDTO {
    private Integer chatId;
    private String chatName;

    public ChatDTO() {}

    public ChatDTO(ChatEntity chatEntity) {
        this.chatId = chatEntity.getChatId();
        this.chatName = chatEntity.getName();
    }

    public Integer getChatId() {
        return chatId;
    }

    public void setChatId(Integer chatId) {
        this.chatId = chatId;
    }

    public String getChatName() {
        return chatName;
    }

    public void setChatName(String chatName) {
        this.chatName = chatName;
    }
}
