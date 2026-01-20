package org.binary.scripting.chusersservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.binary.scripting.chusersservice.entity.User;
import org.binary.scripting.chusersservice.service.UserService;
import org.springframework.boot.info.BuildProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management API endpoints")
public class UserController {

    private final UserService service;
    private final BuildProperties buildProperties;

    @GetMapping
    @Operation(summary = "Get all users", description = "Retrieve a paginated list of all users")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved users",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class)))
    })
    public Flux<User> getAll(
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of users per page", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        log.info("Getting users - page: {}, size: {}", page, size);
        return service.findAll(page, size);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Retrieve a specific user by their UUID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    })
    public Mono<ResponseEntity<User>> getById(
            @Parameter(description = "User UUID", required = true)
            @PathVariable UUID id) {
        log.info("Getting user with id: {}", id);
        return service.findById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Create user", description = "Create a new user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "400", description = "Invalid user data", content = @Content)
    })
    public Mono<User> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User object to create",
                    required = true,
                    content = @Content(schema = @Schema(implementation = User.class)))
            @RequestBody User user) {
        log.info("Creating user {}", user);
        return service.create(user);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user", description = "Update an existing user by their UUID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid user data", content = @Content)
    })
    public Mono<ResponseEntity<User>> update(
            @Parameter(description = "User UUID", required = true)
            @PathVariable UUID id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Updated user object",
                    required = true,
                    content = @Content(schema = @Schema(implementation = User.class)))
            @RequestBody User user) {
        return service.update(id, user)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user", description = "Delete a user by their UUID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User deleted successfully"),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    })
    public Mono<Void> delete(
            @Parameter(description = "User UUID", required = true)
            @PathVariable UUID id) {
        return service.delete(id);
    }
}

