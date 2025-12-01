package com.calendar.users.domain.services;

import com.calendar.users.domain.models.BusinessUser;
import com.calendar.users.domain.ports.UserRepositoryPort;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.LocalDateTime;

public class UserService {

    private final UserRepositoryPort userRepositoryPort;

    public UserService(UserRepositoryPort userRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
    }

    public BusinessUser readProfile(Jwt jwt) {
        BusinessUser user = userRepositoryPort.getBusinessUserByKeycloakId(jwt.getSubject());
        if(user == null){
            return userRepositoryPort.save(new BusinessUser(
                    LocalDateTime.now()
                    ), jwt.getSubject());
        }
        return user;
    }
}
