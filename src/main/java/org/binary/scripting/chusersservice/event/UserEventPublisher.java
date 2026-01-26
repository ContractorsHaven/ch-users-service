package org.binary.scripting.chusersservice.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.binary.scripting.chusersservice.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventPublisher {

    private final KafkaTemplate<String, UserEvent> kafkaTemplate;

    @Value("${app.kafka.topics.user-events}")
    private String userEventsTopic;

    public Mono<User> publishUserCreated(User user) {
        return publishEvent(user, UserEvent.EventType.USER_CREATED);
    }

    public Mono<User> publishUserUpdated(User user) {
        return publishEvent(user, UserEvent.EventType.USER_UPDATED);
    }

    public Mono<Void> publishUserDeleted(UUID userId) {
        UserEvent event = UserEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType(UserEvent.EventType.USER_DELETED)
                .timestamp(Instant.now())
                .payload(UserEvent.UserPayload.builder()
                        .userId(userId)
                        .build())
                .build();

        return Mono.fromFuture(kafkaTemplate.send(userEventsTopic, userId.toString(), event))
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(result -> log.info("Published USER_DELETED event for userId: {}", userId))
                .doOnError(error -> log.error("Failed to publish USER_DELETED event for userId: {}", userId, error))
                .then();
    }

    private Mono<User> publishEvent(User user, UserEvent.EventType eventType) {
        UserEvent event = UserEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType(eventType)
                .timestamp(Instant.now())
                .payload(UserEvent.UserPayload.builder()
                        .userId(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .mobileNumber(user.getMobileNumber())
                        .build())
                .build();

        return Mono.fromFuture(kafkaTemplate.send(userEventsTopic, user.getId().toString(), event))
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(result -> log.info("Published {} event for userId: {}", eventType, user.getId()))
                .doOnError(error -> log.error("Failed to publish {} event for userId: {}", eventType, user.getId(), error))
                .thenReturn(user);
    }
}