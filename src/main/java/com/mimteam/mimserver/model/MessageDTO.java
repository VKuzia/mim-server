package com.mimteam.mimserver.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mimteam.mimserver.model.messages.ChatMembershipMessage.ChatMembershipMessageType;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageDTO {
    private MessageType messageType;

    private Integer userId;
    private Integer chatId;
    private String content;
    private LocalDateTime dateTime;

    private ChatMembershipMessageType chatMembershipMessageType;

    public enum MessageType {
        TEXT_MESSAGE, CHAT_MEMBERSHIP_MESSAGE
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getChatId() {
        return chatId;
    }

    public void setChatId(Integer chatId) {
        this.chatId = chatId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public ChatMembershipMessageType getChatMembershipMessageType() {
        return chatMembershipMessageType;
    }

    public void setChatMembershipMessageType(ChatMembershipMessageType chatMembershipMessageType) {
        this.chatMembershipMessageType = chatMembershipMessageType;
    }
}
