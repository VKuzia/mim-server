package com.mimteam.mimserver.controllers;

import com.mimteam.mimserver.model.ResponseDTO;
import com.mimteam.mimserver.services.UserDatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class UserController {
    private final UserDatabaseService userDatabaseService;

    @Autowired
    public UserController(UserDatabaseService userDatabaseService) {
        this.userDatabaseService = userDatabaseService;
    }

    @PostMapping("/users/signup")
    @ResponseBody
    public ResponseEntity<ResponseDTO> handleUserSignUp(String userName,
                                                        String login,
                                                        String password) {
        return userDatabaseService.signUpUser(userName, login, password);
    }

    @PostMapping("/users/login")
    @ResponseBody
    public ResponseEntity<ResponseDTO> handleUserLogin(String login, String password) {
        return userDatabaseService.loginUser(login, password);
    }

    @GetMapping("/users/{userId}/chatlist")
    @ResponseBody
    public ResponseEntity<ResponseDTO> handleUserChatList(@PathVariable Integer userId) {
        return userDatabaseService.getUserChatList(userId);
    }
}
