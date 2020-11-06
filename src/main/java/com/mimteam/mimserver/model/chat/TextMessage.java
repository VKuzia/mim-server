package com.mimteam.mimserver.model.chat;

import java.time.LocalDate;

public class TextMessage {
    private Integer userId;
    private String content;
    private LocalDate time;

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDate getTime() {
        return time;
    }

    public void setTime(LocalDate time) {
        this.time = time;
    }
}
