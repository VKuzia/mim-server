package com.mimteam.mimserver.repositories;

import com.mimteam.mimserver.model.entities.UserEntity;
import org.springframework.data.repository.CrudRepository;

public interface UsersRepository extends CrudRepository<UserEntity, Integer> {
}
