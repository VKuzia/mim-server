package com.mimteam.mimserver.model.entities;

import com.mimteam.mimserver.model.entities.chat.UserToChatEntity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(schema = "mim", name = "users")
public class UserEntity {

    @Id
    @GeneratedValue
    private Integer userId;

    private String name;
    private String login;
    private String password;
    private LocalDateTime lastOnline;

    private String token;

    @OneToMany(mappedBy = "userEntity")
    Set<UserToChatEntity> chatList;

    public UserEntity() {
    }

    public UserEntity(String name, String login, String password) {
        this.name = name;
        this.login = login;
        this.password = password;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public LocalDateTime getLastOnline() {
        return lastOnline;
    }

    public void setLastOnline(LocalDateTime lastOnline) {
        this.lastOnline = lastOnline;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Set<UserToChatEntity> getChatList() {
        return chatList;
    }
}
