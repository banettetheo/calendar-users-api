package com.calendar.users.infrastructure.mappers;

import com.calendar.users.domain.models.BusinessUser;
import com.calendar.users.infrastructure.models.entities.UserEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserEntityMapper {

    BusinessUser toBusinessUser(UserEntity userEntity);

    UserEntity toUserEntity(BusinessUser businessUser);
}
