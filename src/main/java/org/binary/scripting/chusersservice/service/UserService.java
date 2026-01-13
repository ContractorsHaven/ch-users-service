package org.binary.scripting.chusersservice.service;

import org.binary.scripting.chusersservice.entity.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UserService {
    Flux<User> findAll();
    Mono<User> findById(UUID id);
    Mono<User> create(User user);
    Mono<User> update(UUID id, User user);
    Mono<Void> delete(UUID id);
}
