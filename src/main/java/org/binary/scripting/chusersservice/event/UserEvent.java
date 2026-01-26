package org.binary.scripting.chusersservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEvent {
    private UUID eventId;
    private EventType eventType;
    private Instant timestamp;
    private UserPayload payload;

    public enum EventType {
        USER_CREATED,
        USER_UPDATED,
        USER_DELETED
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserPayload {
        private UUID userId;
        private String username;
        private String email;
        private String firstName;
        private String lastName;
        private String mobileNumber;
    }
}
