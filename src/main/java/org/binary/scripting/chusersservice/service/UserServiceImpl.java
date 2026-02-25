package org.binary.scripting.chusersservice.service;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.binary.scripting.chusersservice.dto.PagedResponse;
import org.binary.scripting.chusersservice.entity.User;
import org.binary.scripting.chusersservice.event.UserEventPublisher;
import org.binary.scripting.chusersservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private static final int DEFAULT_PAGE_SIZE = 10;

    private final UserRepository usersRepository;
    private final Optional<UserEventPublisher> userEventPublisher;

    @Autowired
    public UserServiceImpl(UserRepository usersRepository, Optional<UserEventPublisher> userEventPublisher) {
        this.usersRepository = usersRepository;
        this.userEventPublisher = userEventPublisher;
    }

    @Override
    public Mono<PagedResponse<User>> findAll(int page, int size) {
        int pageSize = size > 0 ? size : DEFAULT_PAGE_SIZE;
        int pageNumber = page <= 1 ? 0 : page - 1;
        log.debug("Fetching users - page: {}, size: {}", pageNumber, pageSize);

        return Mono.zip(
                usersRepository.findAllBy(PageRequest.of(pageNumber, pageSize)).collectList(),
                usersRepository.count()
        ).map(tuple -> {
            var users = tuple.getT1();
            var totalElements = tuple.getT2();
            int totalPages = (int) Math.ceil((double) totalElements / pageSize);

            return PagedResponse.<User>builder()
                    .content(users)
                    .page(pageNumber)
                    .size(pageSize)
                    .totalElements(totalElements)
                    .totalPages(totalPages)
                    .first(pageNumber == 0)
                    .last(pageNumber >= totalPages - 1)
                    .build();
        });
    }

    @Override
    public Mono<User> findById(@NonNull UUID id) {
        log.debug("Fetching user by id: {}", id);
        return usersRepository.findById(id);
    }

    @Override
    public Mono<User> create(@NonNull User user) {
        log.info("Creating user: {}", user.getUsername());
        return usersRepository.save(user)
                .flatMap(savedUser -> userEventPublisher
                        .map(publisher -> publisher.publishUserCreated(savedUser))
                        .orElse(Mono.just(savedUser)));
    }

    @Override
    public Mono<User> update(@NonNull UUID id, @NonNull User user) {
        log.info("Updating user: {}", id);
        return usersRepository.findById(id)
                .flatMap(existingUser -> {
                    existingUser.setUsername(user.getUsername());
                    existingUser.setEmail(user.getEmail());
                    existingUser.setFirstName(user.getFirstName());
                    existingUser.setLastName(user.getLastName());
                    existingUser.setMobileNumber(user.getMobileNumber());
                    existingUser.setModifiedBy(user.getModifiedBy());
                    return usersRepository.save(existingUser);
                })
                .flatMap(savedUser -> userEventPublisher
                        .map(publisher -> publisher.publishUserUpdated(savedUser))
                        .orElse(Mono.just(savedUser)));
    }

    @Override
    public Mono<Void> delete(@NonNull UUID id) {
        log.info("Deleting user: {}", id);
        return usersRepository.deleteById(id);
    }
}
