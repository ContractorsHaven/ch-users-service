package org.binary.scripting.chusersservice.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "User entity representing a platform user")
public class User {
    @Id
    @Schema(description = "Unique identifier for the user", example = "550e8400-e29b-41d4-a716-446655440000", accessMode = Schema.AccessMode.READ_ONLY)
    private UUID id;

    @Schema(description = "Username for login", example = "johndoe", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @Schema(description = "User's email address", example = "john.doe@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @Column("first_name")
    @Schema(description = "User's first name", example = "John")
    private String firstName;

    @Column("last_name")
    @Schema(description = "User's last name", example = "Doe")
    private String lastName;

    @Column("mobile_number")
    @Schema(description = "User's mobile phone number", example = "+1-555-123-4567")
    private String mobileNumber;

    @CreatedBy
    @Column("created_by")
    @Schema(description = "User who created this record", accessMode = Schema.AccessMode.READ_ONLY)
    private String createdBy;

    @LastModifiedBy
    @Column("modified_by")
    @Schema(description = "User who last modified this record", accessMode = Schema.AccessMode.READ_ONLY)
    private String modifiedBy;

    @CreatedDate
    @Column("created_at")
    @Schema(description = "Timestamp when this record was created", example = "2024-01-15T10:30:00Z", accessMode = Schema.AccessMode.READ_ONLY)
    private Instant createdAt;

    @LastModifiedDate
    @Column("modified_at")
    @Schema(description = "Timestamp when this record was last modified", example = "2024-01-15T14:45:00Z", accessMode = Schema.AccessMode.READ_ONLY)
    private Instant modifiedAt;
}

