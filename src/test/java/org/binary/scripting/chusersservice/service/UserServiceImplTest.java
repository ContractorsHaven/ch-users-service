package org.binary.scripting.chusersservice.service;

import org.binary.scripting.chusersservice.entity.User;
import org.binary.scripting.chusersservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

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
    }

    @Test
    void findAll_shouldReturnUsers() {
        when(userRepository.findAllBy(any(PageRequest.class)))
                .thenReturn(Flux.just(testUser));

        StepVerifier.create(userService.findAll(0, 10))
                .assertNext(user -> {
                    assertThat(user.getUsername()).isEqualTo("testuser");
                    assertThat(user.getEmail()).isEqualTo("test@example.com");
                    assertThat(user.getFirstName()).isEqualTo("John");
                    assertThat(user.getLastName()).isEqualTo("Doe");
                })
                .verifyComplete();

        verify(userRepository).findAllBy(PageRequest.of(0, 10));
    }

    @Test
    void findAll_withNegativePage_shouldUseZero() {
        when(userRepository.findAllBy(any(PageRequest.class)))
                .thenReturn(Flux.just(testUser));

        StepVerifier.create(userService.findAll(-1, 10))
                .expectNextCount(1)
                .verifyComplete();

        verify(userRepository).findAllBy(PageRequest.of(0, 10));
    }

    @Test
    void findAll_withZeroSize_shouldUseDefault() {
        when(userRepository.findAllBy(any(PageRequest.class)))
                .thenReturn(Flux.just(testUser));

        StepVerifier.create(userService.findAll(0, 0))
                .expectNextCount(1)
                .verifyComplete();

        verify(userRepository).findAllBy(PageRequest.of(0, 10));
    }

    @Test
    void findById_shouldReturnUser() {
        when(userRepository.findById(testId))
                .thenReturn(Mono.just(testUser));

        StepVerifier.create(userService.findById(testId))
                .assertNext(user -> {
                    assertThat(user.getId()).isEqualTo(testId);
                    assertThat(user.getUsername()).isEqualTo("testuser");
                    assertThat(user.getFirstName()).isEqualTo("John");
                })
                .verifyComplete();
    }

    @Test
    void findById_whenNotFound_shouldReturnEmpty() {
        when(userRepository.findById(testId))
                .thenReturn(Mono.empty());

        StepVerifier.create(userService.findById(testId))
                .verifyComplete();
    }

    @Test
    void create_shouldSaveAndReturnUser() {
        when(userRepository.save(any(User.class)))
                .thenReturn(Mono.just(testUser));

        User newUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .build();

        StepVerifier.create(userService.create(newUser))
                .assertNext(user -> {
                    assertThat(user.getUsername()).isEqualTo("testuser");
                    assertThat(user.getEmail()).isEqualTo("test@example.com");
                })
                .verifyComplete();

        verify(userRepository).save(newUser);
    }

    @Test
    void update_shouldUpdateAndReturnUser() {
        User updatedUser = User.builder()
                .username("updateduser")
                .email("updated@example.com")
                .firstName("Jane")
                .lastName("Smith")
                .mobileNumber("+0987654321")
                .build();

        when(userRepository.findById(testId))
                .thenReturn(Mono.just(testUser));
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(userService.update(testId, updatedUser))
                .assertNext(user -> {
                    assertThat(user.getId()).isEqualTo(testId);
                    assertThat(user.getUsername()).isEqualTo("updateduser");
                    assertThat(user.getEmail()).isEqualTo("updated@example.com");
                })
                .verifyComplete();

        verify(userRepository).findById(testId);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void update_whenNotFound_shouldReturnEmpty() {
        when(userRepository.findById(testId))
                .thenReturn(Mono.empty());

        StepVerifier.create(userService.update(testId, testUser))
                .verifyComplete();

        verify(userRepository).findById(testId);
        verify(userRepository, never()).save(any());
    }

    @Test
    void delete_shouldDeleteUser() {
        when(userRepository.deleteById(testId))
                .thenReturn(Mono.empty());

        StepVerifier.create(userService.delete(testId))
                .verifyComplete();

        verify(userRepository).deleteById(testId);
    }
}