package com.mimteam.mimserver.model.entities.chat;

import com.mimteam.mimserver.model.messages.TextMessage;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(schema = "mim", name = "messages")
public class ChatMessageEntity {

    @Id
    @GeneratedValue
    private Integer id;

    private Integer chatId;
    private Integer senderId;
    private Date dateTime;
    private String content;

    public ChatMessageEntity() {}

    public ChatMessageEntity(TextMessage event) {
        this.chatId = event.getChatId();
        this.senderId = event.getUserId();
        this.dateTime = event.getDateTime();
        this.content = event.getContent();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getChatId() {
        return chatId;
    }

    public void setChatId(Integer chatId) {
        this.chatId = chatId;
    }

    public Integer getSenderId() {
        return senderId;
    }

    public void setSenderId(Integer senderId) {
        this.senderId = senderId;
    }

    public Date getDateTime() {
        return dateTime;
    }

    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
