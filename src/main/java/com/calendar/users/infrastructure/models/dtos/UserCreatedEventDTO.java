package com.calendar.users.infrastructure.models.dtos;

public record UserCreatedEventDTO(
        Long userId,
        String userName,
        Integer hashtag,
        String profilePicUrl
) {
}
