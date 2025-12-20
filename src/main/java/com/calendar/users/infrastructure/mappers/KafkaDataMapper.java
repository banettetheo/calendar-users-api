package com.calendar.users.infrastructure.mappers;

import com.calendar.users.domain.models.BusinessUser;
import com.calendar.users.infrastructure.models.dtos.UserCreatedEventDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface KafkaDataMapper {

    @Mapping(target = "userId", source = "id")
    UserCreatedEventDTO toUserCreatedEventDTO(BusinessUser businessUser);
}
