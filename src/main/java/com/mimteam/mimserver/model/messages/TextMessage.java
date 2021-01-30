package com.mimteam.mimserver.model.messages;

import com.mimteam.mimserver.model.dto.MessageDTO;
import com.mimteam.mimserver.model.Transferable;
import com.mimteam.mimserver.model.entities.chat.ChatMessageEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Date;

public class TextMessage implements Transferable {
    private Integer userId;
    private Integer chatId;
    private String content;
    private Date dateTime;

    public TextMessage(ChatMessageEntity messageEntity) {
        this.userId = messageEntity.getSenderId();
        this.chatId = messageEntity.getChatId();
        this.content = messageEntity.getContent();
        this.dateTime = messageEntity.getDateTime();
    }

    public TextMessage(MessageDTO messageDto) {
        fromDataTransferObject(messageDto);
    }

    @Override
    public MessageDTO toDataTransferObject() {
        MessageDTO dto = new MessageDTO();
        dto.setMessageType(MessageDTO.MessageType.TEXT_MESSAGE);
        dto.setUserId(userId);
        dto.setChatId(chatId);
        dto.setContent(content);
        dto.setDateTime(dateTime);
        return dto;
    }

    @Override
    public void fromDataTransferObject(@NotNull MessageDTO dto) {
        this.userId = dto.getUserId();
        this.chatId = dto.getChatId();
        this.content = dto.getContent();
        this.dateTime = dto.getDateTime();
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

    public Date getDateTime() {
        return dateTime;
    }

    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }
}
