package com.mimteam.mimserver.controllers;

import com.mimteam.mimserver.model.responses.ResponseDTO;
import com.mimteam.mimserver.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/users/signup")
    @ResponseBody
    public ResponseEntity<ResponseDTO> handleUserSignUp(String userName,
                                                        String login,
                                                        String password) {
        return userService.signUpUser(userName, login, password);
    }

    @PostMapping("/users/login")
    @ResponseBody
    public ResponseEntity<ResponseDTO> handleUserLogin(String login, String password) {
        return userService.loginUser(login, password);
    }

    @GetMapping("/users/{userId}/chatlist")
    @ResponseBody
    public ResponseEntity<ResponseDTO> getUserChatList(@PathVariable Integer userId) {
        return userService.getChatIdList(userId);
    }
}
