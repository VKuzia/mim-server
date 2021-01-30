package com.mimteam.mimserver.model.dto;

import com.mimteam.mimserver.model.entities.UserEntity;

public class UserDTO {
    private Integer userId;
    private String userName;

    public UserDTO() {}

    public UserDTO(UserEntity userEntity) {
        this.userId = userEntity.getUserId();
        this.userName = userEntity.getName();
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
