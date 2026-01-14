package org.binary.scripting.chusersservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.binary.scripting.chusersservice.entity.User;
import org.binary.scripting.chusersservice.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;

    @GetMapping
    public Flux<User> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Getting users - page: {}, size: {}", page, size);
        return service.findAll(page, size);
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<User>> getById(@PathVariable UUID id) {
        log.info("Getting user with id: {}", id);
        return service.findById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Mono<User> create(@RequestBody User user) {
        log.info("Creating user {}", user);
        return service.create(user);
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<User>> update(
            @PathVariable UUID id,
            @RequestBody User user) {

        return service.update(id, user)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public Mono<Void> delete(@PathVariable UUID id) {
        return service.delete(id);
    }
}

