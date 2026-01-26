package org.binary.scripting.chusersservice.integration;

import org.binary.scripting.chusersservice.entity.User;
import org.binary.scripting.chusersservice.event.UserEventPublisher;
import org.binary.scripting.chusersservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class UserIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private UserEventPublisher userEventPublisher;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll().block();
        when(userEventPublisher.publishUserCreated(any(User.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(userEventPublisher.publishUserUpdated(any(User.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(userEventPublisher.publishUserDeleted(any(UUID.class)))
                .thenReturn(Mono.empty());
    }

    @Test
    void shouldCreateAndRetrieveUser() {
        User newUser = User.builder()
                .username("integrationuser")
                .email("integration@example.com")
                .firstName("Integration")
                .lastName("Test")
                .mobileNumber("+1234567890")
                .build();

        // Create user
        User createdUser = webTestClient.post()
                .uri("/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(newUser)
                .exchange()
                .expectStatus().isOk()
                .expectBody(User.class)
                .returnResult()
                .getResponseBody();

        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getId()).isNotNull();
        assertThat(createdUser.getUsername()).isEqualTo("integrationuser");
        assertThat(createdUser.getFirstName()).isEqualTo("Integration");

        // Retrieve user
        webTestClient.get()
                .uri("/v1/users/{id}", createdUser.getId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(User.class)
                .value(user -> {
                    assertThat(user.getId()).isEqualTo(createdUser.getId());
                    assertThat(user.getUsername()).isEqualTo("integrationuser");
                    assertThat(user.getEmail()).isEqualTo("integration@example.com");
                    assertThat(user.getFirstName()).isEqualTo("Integration");
                    assertThat(user.getLastName()).isEqualTo("Test");
                    assertThat(user.getMobileNumber()).isEqualTo("+1234567890");
                });
    }

    @Test
    void shouldUpdateUser() {
        // Create initial user
        User initialUser = userRepository.save(
                User.builder()
                        .username("updateme")
                        .email("update@example.com")
                        .firstName("Original")
                        .lastName("Name")
                        .build()
        ).block();

        assertThat(initialUser).isNotNull();

        User updateData = User.builder()
                .username("updateduser")
                .email("updated@example.com")
                .firstName("Updated")
                .lastName("Person")
                .mobileNumber("+9876543210")
                .build();

        // Update user
        webTestClient.put()
                .uri("/v1/users/{id}", initialUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateData)
                .exchange()
                .expectStatus().isOk()
                .expectBody(User.class)
                .value(user -> {
                    assertThat(user.getId()).isEqualTo(initialUser.getId());
                    assertThat(user.getUsername()).isEqualTo("updateduser");
                    assertThat(user.getEmail()).isEqualTo("updated@example.com");
                });
    }

    @Test
    void shouldDeleteUser() {
        // Create user to delete
        User userToDelete = userRepository.save(
                User.builder()
                        .username("deleteme")
                        .email("delete@example.com")
                        .build()
        ).block();

        assertThat(userToDelete).isNotNull();

        // Delete user
        webTestClient.delete()
                .uri("/v1/users/{id}", userToDelete.getId())
                .exchange()
                .expectStatus().isOk();

        // Verify deleted
        webTestClient.get()
                .uri("/v1/users/{id}", userToDelete.getId())
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void shouldReturnPaginatedUsers() {
        // Create multiple users
        for (int i = 0; i < 15; i++) {
            userRepository.save(
                    User.builder()
                            .username("user" + i)
                            .email("user" + i + "@example.com")
                            .firstName("First" + i)
                            .lastName("Last" + i)
                            .build()
            ).block();
        }

        // Get first page
        webTestClient.get()
                .uri("/v1/users?page=0&size=10")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(User.class)
                .hasSize(10);

        // Get second page
        webTestClient.get()
                .uri("/v1/users?page=1&size=10")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(User.class)
                .hasSize(5);
    }

    @Test
    void shouldReturn404ForNonExistentUser() {
        UUID nonExistentId = UUID.randomUUID();

        webTestClient.get()
                .uri("/v1/users/{id}", nonExistentId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }
}