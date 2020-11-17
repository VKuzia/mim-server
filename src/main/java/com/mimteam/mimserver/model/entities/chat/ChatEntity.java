package com.mimteam.mimserver.model.entities.chat;


import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.Table;

@Entity
@Table(schema = "mim", name = "chats")
public class ChatEntity {

    @Id
    @GeneratedValue
    private Integer chatId;

    private String name;

    public Integer getChatId() {
        return chatId;
    }

    public void setChatId(Integer chatId) {
        this.chatId = chatId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
