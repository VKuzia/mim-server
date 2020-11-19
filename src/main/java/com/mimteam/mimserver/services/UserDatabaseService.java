package com.mimteam.mimserver.services;

import com.mimteam.mimserver.model.ResponseBuilder;
import com.mimteam.mimserver.model.ResponseDTO;
import com.mimteam.mimserver.model.ResponseDTO.ResponseType;
import com.mimteam.mimserver.model.entities.UserEntity;
import com.mimteam.mimserver.model.entities.chat.ChatEntity;
import com.mimteam.mimserver.model.entities.chat.UserToChatEntity;
import com.mimteam.mimserver.repositories.UsersRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserDatabaseService {
    private final UsersRepository usersRepository;

    public UserDatabaseService(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    public ResponseEntity<ResponseDTO> signUpUser(String userName,
                                                  String login,
                                                  String password) {
        Optional<UserEntity> user = usersRepository.findByLogin(login);
        if (user.isPresent()) {
            return ResponseBuilder.builder()
                    .responseType(ResponseType.USER_ALREADY_EXISTS)
                    .build();
        }

        UserEntity userEntity = new UserEntity(userName, login, password);
        usersRepository.save(userEntity);

        return ResponseBuilder.builder().ok();
    }

    public ResponseEntity<ResponseDTO> loginUser(String login, String password) {
        Optional<UserEntity> user = usersRepository.findByLogin(login);
        if (user.isEmpty()) {
            return ResponseBuilder.builder()
                    .responseType(ResponseType.USER_NOT_EXISTS)
                    .build();
        }
        if (!user.get().getPassword().equals(password)) {
            return ResponseBuilder.builder()
                    .responseType(ResponseType.INCORRECT_PASSWORD)
                    .build();
        }

        return ResponseBuilder.builder()
                .responseType(ResponseType.OK)
                .body(user.get().getUserId())
                .build();
    }

    public ResponseEntity<ResponseDTO> getUserChatList(Integer userId) {
        Optional<UserEntity> user = getUserById(userId);
        if (user.isEmpty()) {
            return ResponseBuilder.builder()
                    .responseType(ResponseType.USER_NOT_EXISTS)
                    .build();
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
