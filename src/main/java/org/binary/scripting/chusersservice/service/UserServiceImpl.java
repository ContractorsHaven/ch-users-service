package org.binary.scripting.chusersservice.service;

import lombok.AllArgsConstructor;
import org.binary.scripting.chusersservice.entity.User;
import org.binary.scripting.chusersservice.repository.UserRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


import java.util.UUID;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository usersRepository;

    @Override
    public Flux<User> findAll() {
        return null;
    }

    @Override
    public Mono<User> findById(UUID id) {
        return null;
    }

    @Override
    public Mono<User> create(User user) {
        return null;
    }

    @Override
    public Mono<User> update(UUID id, User user) {
        return null;
    }

    @Override
    public Mono<Void> delete(UUID id) {
        return null;
    }
}
