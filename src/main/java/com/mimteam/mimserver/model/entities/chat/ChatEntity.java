package com.mimteam.mimserver.model.entities.chat;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.Set;

@Entity
@Table(schema = "mim", name = "chats")
public class ChatEntity {

    @Id
    @GeneratedValue
    private Integer chatId;

    private String name;

    @OneToMany(mappedBy = "chatEntity")
    Set<UserToChatEntity> userList;

    public ChatEntity() {}

    public ChatEntity(String name) {
        this.name = name;
    }

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

    public Set<UserToChatEntity> getUserList() {
        return userList;
    }
}
