package com.mimteam.mimserver.controllers;

import com.mimteam.mimserver.model.entities.UserEntity;
import com.mimteam.mimserver.model.entities.chat.ChatEntity;
import com.mimteam.mimserver.model.entities.chat.UserToChatEntity;
import com.mimteam.mimserver.repositories.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class UserController {
    private final UsersRepository usersRepository;

    @Autowired
    public UserController(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    @PostMapping("/users/signup")
    @ResponseBody
    public ResponseEntity<Void> handleUserSignUp(String userName,
                                                 String login,
                                                 String password) {
        Optional<UserEntity> user = usersRepository.findByLogin(login);
        if (user.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        UserEntity userEntity = new UserEntity(userName, login, password);
        usersRepository.save(userEntity);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/users/login")
    @ResponseBody
    public ResponseEntity<Integer> handleUserLogin(String login, String password) {
        Optional<UserEntity> user = usersRepository.findByLoginAndPassword(login, password);
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        return ResponseEntity.ok(user.get().getUserId());
    }

    @GetMapping("/users/{userId}/chatlist")
    @ResponseBody
    public ResponseEntity<List<Integer>> handleUserChatList(@PathVariable Integer userId) {
        Optional<UserEntity> user = usersRepository.findById(userId);
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        List<Integer> chatIdList = user.get().getChatList().stream()
                .map(UserToChatEntity::getChatEntity)
                .map(ChatEntity::getChatId)
                .collect(Collectors.toList());

        return ResponseEntity.ok(chatIdList);
    }
}
