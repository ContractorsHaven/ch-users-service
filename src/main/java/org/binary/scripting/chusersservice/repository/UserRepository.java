package org.binary.scripting.chusersservice.repository;

import org.binary.scripting.chusersservice.entity.User;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.util.UUID;

public interface UserRepository extends ReactiveCrudRepository<User, UUID> {
}
