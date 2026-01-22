package org.binary.scripting.chusersservice.controller;

import org.binary.scripting.chusersservice.entity.User;
import org.binary.scripting.chusersservice.controller.UserController;
import org.binary.scripting.chusersservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@WebFluxTest(UserController.class)
class UserControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private BuildProperties buildProperties;

    private User testUser;
    private UUID testId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        testUser = User.builder()
                .id(testId)
                .username("testuser")
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .mobileNumber("+1234567890")
                .createdBy("system")
                .modifiedBy("system")
                .build();

        when(buildProperties.getName()).thenReturn("ch-users-service");
        when(buildProperties.getVersion()).thenReturn("0.0.1");
        when(buildProperties.getArtifact()).thenReturn("ch-users-service");
        when(buildProperties.getGroup()).thenReturn("org.binary.scripting");
        when(buildProperties.getTime()).thenReturn(Instant.now());
    }

    @Test
    void getAll_shouldReturnUsers() {
        when(userService.findAll(0, 10))
                .thenReturn(Flux.just(testUser));

        webTestClient.get()
                .uri("/v1/users?page=0&size=10")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(User.class)
                .hasSize(1)
                .contains(testUser);
    }

    @Test
    void getAll_withDefaultPagination_shouldReturnUsers() {
        when(userService.findAll(0, 10))
                .thenReturn(Flux.just(testUser));

        webTestClient.get()
                .uri("/v1/users")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(User.class)
                .hasSize(1);
    }

    @Test
    void getById_shouldReturnUser() {
        when(userService.findById(testId))
                .thenReturn(Mono.just(testUser));

        webTestClient.get()
                .uri("/v1/users/{id}", testId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(User.class)
                .isEqualTo(testUser);
    }

    @Test
    void getById_whenNotFound_shouldReturn404() {
        when(userService.findById(testId))
                .thenReturn(Mono.empty());

        webTestClient.get()
                .uri("/v1/users/{id}", testId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void create_shouldReturnCreatedUser() {
        User newUser = User.builder()
                .username("newuser")
                .email("new@example.com")
                .firstName("Jane")
                .lastName("Smith")
                .build();

        User savedUser = User.builder()
                .id(UUID.randomUUID())
                .username("newuser")
                .email("new@example.com")
                .firstName("Jane")
                .lastName("Smith")
                .build();

        when(userService.create(any(User.class)))
                .thenReturn(Mono.just(savedUser));

        webTestClient.post()
                .uri("/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(newUser)
                .exchange()
                .expectStatus().isOk()
                .expectBody(User.class)
                .value(user -> {
                    assert user.getId() != null;
                    assert user.getUsername().equals("newuser");
                    assert user.getFirstName().equals("Jane");
                });
    }

    @Test
    void update_shouldReturnUpdatedUser() {
        User updatedUser = User.builder()
                .username("updateduser")
                .email("updated@example.com")
                .firstName("Updated")
                .lastName("User")
                .build();

        User savedUser = User.builder()
                .id(testId)
                .username("updateduser")
                .email("updated@example.com")
                .firstName("Updated")
                .lastName("User")
                .build();

        when(userService.update(eq(testId), any(User.class)))
                .thenReturn(Mono.just(savedUser));

        webTestClient.put()
                .uri("/v1/users/{id}", testId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updatedUser)
                .exchange()
                .expectStatus().isOk()
                .expectBody(User.class)
                .value(user -> {
                    assert user.getId().equals(testId);
                    assert user.getUsername().equals("updateduser");
                });
    }

    @Test
    void update_whenNotFound_shouldReturn404() {
        when(userService.update(eq(testId), any(User.class)))
                .thenReturn(Mono.empty());

        webTestClient.put()
                .uri("/v1/users/{id}", testId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(testUser)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void delete_shouldReturn200() {
        when(userService.delete(testId))
                .thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/v1/users/{id}", testId)
                .exchange()
                .expectStatus().isOk();
    }
}