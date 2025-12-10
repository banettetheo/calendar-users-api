package com.calendar.users.infrastructure.models.dtos;

import java.time.LocalDateTime;

public record UserWithStatusDatabaseDTO(
        Long id,
        String keycloakId,
        String profilePicUrl,
        LocalDateTime joinedDate,
        String relationshipStatus) {
}
