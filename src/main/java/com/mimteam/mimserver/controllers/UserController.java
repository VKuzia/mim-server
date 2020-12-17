package com.mimteam.mimserver.controllers;

import com.mimteam.mimserver.model.entities.UserEntity;
import com.mimteam.mimserver.model.responses.ResponseBuilder;
import com.mimteam.mimserver.model.responses.ResponseDTO;
import com.mimteam.mimserver.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
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

    @GetMapping("/users/chatlist")
    @ResponseBody
    public ResponseEntity<ResponseDTO> getUserChatList(Authentication authentication) {
        UserEntity user = (UserEntity) authentication.getPrincipal();
        return userService.getChatList(user.getUserId());
    }

    @GetMapping("/users/getid")
    @ResponseBody
    public ResponseEntity<ResponseDTO> getUserId(Authentication authentication) {
        UserEntity user = (UserEntity) authentication.getPrincipal();
        return ResponseBuilder.builder()
                .responseType(ResponseDTO.ResponseType.OK)
                .body(user.getUserId())
                .build();
    }
}
