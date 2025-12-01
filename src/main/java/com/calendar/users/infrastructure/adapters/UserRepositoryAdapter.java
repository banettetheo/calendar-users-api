package com.calendar.users.infrastructure.adapters;

import com.calendar.users.domain.models.BusinessUser;
import com.calendar.users.domain.ports.UserRepositoryPort;
import com.calendar.users.infrastructure.mappers.UserEntityMapper;
import com.calendar.users.infrastructure.models.entities.UserEntity;
import com.calendar.users.infrastructure.repositories.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final UserRepository userRepository;
    private final UserEntityMapper userEntityMapper;

    public UserRepositoryAdapter(UserRepository userRepository, UserEntityMapper userEntityMapper) {
        this.userRepository = userRepository;
        this.userEntityMapper = userEntityMapper;
    }

    public BusinessUser save(BusinessUser businessUser, String keycloakId) {
        try {
            UserEntity userEntity = userEntityMapper.toUserEntity(businessUser);
            userEntity.setKeycloakId(keycloakId);
            return userEntityMapper.toBusinessUser(
                    userRepository.save(userEntity));
        } catch (DataIntegrityViolationException e) {
            throw e;
        } catch (Exception e) {
            throw e;
        }
    }

    public BusinessUser getBusinessUserByKeycloakId(String keycloakId) {
        return userEntityMapper.toBusinessUser(userRepository.findByKeycloakId(keycloakId));
    }
}
