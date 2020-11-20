package com.mimteam.mimserver.services;

import com.mimteam.mimserver.model.responses.ErrorResponseCreator;
import com.mimteam.mimserver.model.responses.ResponseBuilder;
import com.mimteam.mimserver.model.responses.ResponseDTO;
import com.mimteam.mimserver.model.responses.ResponseDTO.ResponseType;
import com.mimteam.mimserver.model.entities.UserEntity;
import com.mimteam.mimserver.model.entities.chat.ChatEntity;
import com.mimteam.mimserver.model.entities.chat.UserToChatEntity;
import com.mimteam.mimserver.repositories.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UsersRepository usersRepository;

    @Autowired
    public UserService(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    public ResponseEntity<ResponseDTO> signUpUser(String userName,
                                                  String login,
                                                  String password) {
        Optional<UserEntity> user = usersRepository.findByLogin(login);
        if (user.isPresent()) {
            return ErrorResponseCreator.createResponse(ResponseType.USER_ALREADY_EXISTS);
        }

        UserEntity userEntity = new UserEntity(userName, login, password);
        usersRepository.save(userEntity);
        return ResponseBuilder.builder().ok();
    }

    public ResponseEntity<ResponseDTO> loginUser(String login, String password) {
        Optional<UserEntity> user = usersRepository.findByLogin(login);
        if (user.isEmpty()) {
            return ErrorResponseCreator.createResponse(ResponseType.USER_NOT_EXISTS);
        }
        if (!user.get().getPassword().equals(password)) {
            return ErrorResponseCreator.createResponse(ResponseType.INCORRECT_PASSWORD);
        }

        return ResponseBuilder.builder()
                .responseType(ResponseType.OK)
                .body(user.get().getUserId())
                .build();
    }

    public ResponseEntity<ResponseDTO> getChatIdList(Integer userId) {
        Optional<UserEntity> user = getUserById(userId);
        if (user.isEmpty()) {
            return ErrorResponseCreator.createResponse(ResponseType.USER_NOT_EXISTS);
        }

        List<Integer> chatIdList = user.get().getChatList().stream()
                .map(UserToChatEntity::getChatEntity)
                .map(ChatEntity::getChatId)
                .collect(Collectors.toList());

        return ResponseBuilder.builder()
                .responseType(ResponseType.OK)
                .body(chatIdList)
                .build();
    }

    public Optional<UserEntity> getUserById(Integer userId) {
        return usersRepository.findById(userId);
    }
}