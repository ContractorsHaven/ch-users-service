package org.binary.scripting.chusersservice.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.binary.scripting.chusersservice.entity.User;
import org.binary.scripting.chusersservice.event.UserEventPublisher;
import org.binary.scripting.chusersservice.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final int DEFAULT_PAGE_SIZE = 10;

    private final UserRepository usersRepository;
    private final UserEventPublisher userEventPublisher;

    @Override
    public Flux<User> findAll(int page, int size) {
        int pageSize = size > 0 ? size : DEFAULT_PAGE_SIZE;
        int pageNumber = Math.max(page, 0);
        log.debug("Fetching users - page: {}, size: {}", pageNumber, pageSize);
        return usersRepository.findAllBy(PageRequest.of(pageNumber, pageSize));
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
                .flatMap(userEventPublisher::publishUserCreated);
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
                .flatMap(userEventPublisher::publishUserUpdated);
    }

    @Override
    public Mono<Void> delete(@NonNull UUID id) {
        log.info("Deleting user: {}", id);
        return usersRepository.deleteById(id);
    }
}
