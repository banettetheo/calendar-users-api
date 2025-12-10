package com.calendar.users.domain.models;

import java.time.LocalDateTime;

public record UserWithStatusDTO(
        Long id,
        String profilePicUrl,
        LocalDateTime joinedDate,
        String relationshipStatus
) {
}
