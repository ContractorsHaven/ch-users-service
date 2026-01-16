package org.binary.scripting.chusersservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table("users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    private UUID id;
    private String username;
    private String email;
    @Column("first_name")
    private String firstName;
    @Column("last_name")
    private String lastName;
    @Column("mobile_number")
    private String mobileNumber;
    @CreatedBy
    @Column("created_by")
    private String createdBy;
    @LastModifiedBy
    @Column("modified_by")
    private String modifiedBy;
}

