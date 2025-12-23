package com.calendar.users.domain.services;

import com.calendar.users.domain.models.BusinessUser;
import com.calendar.users.domain.ports.AwsPort;
import com.calendar.users.domain.ports.KeycloakPort;
import com.calendar.users.domain.ports.UserEventPublisher;
import com.calendar.users.domain.ports.UserRepositoryPort;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

public class UserService {

    private final UserRepositoryPort userRepositoryPort;
    private final AwsPort awsPort;
    private final KeycloakPort keycloakPort;
    private final UserEventPublisher userEventPublisher;

    public UserService(UserRepositoryPort userRepositoryPort, AwsPort awsPort, KeycloakPort keycloakPort, UserEventPublisher userEventPublisher) {
        this.userRepositoryPort = userRepositoryPort;
        this.awsPort = awsPort;
        this.keycloakPort = keycloakPort;
        this.userEventPublisher = userEventPublisher;
    }

    public Mono<BusinessUser> readProfile(Long userId) {
        return userRepositoryPort.getBusinessUserByUserId(userId)
                .onErrorResume(e -> Mono.empty());
    }

    @Transactional
    public Mono<Long> resolveInternalUserId(String keycloakId) {
        return userRepositoryPort.findIdByKeycloakId(keycloakId)
                        .switchIfEmpty(
                                keycloakPort.getUser(keycloakId)
                                                .flatMap(keycloakUserResponse -> userRepositoryPort.save(new BusinessUser(
                                                        null,
                                                        keycloakUserResponse.username(),
                                                        keycloakUserResponse.firstName(),
                                                        keycloakUserResponse.lastName(),
                                                        null,
                                                        LocalDateTime.now()), keycloakId)
                                                        .flatMap(userEventPublisher::publishUserCreatedEvent).onErrorMap(e -> new RuntimeException(e.getMessage(), e))))
                                                .switchIfEmpty(Mono.error(new RuntimeException("Keycloak User not found")));
    }

    public Mono<String> updateProfilePicture(Long userId, Mono<FilePart> filePartMono) {
        return filePartMono.flatMap(filePart ->
                    awsPort.storeObject(filePart, userId.toString())
                        .flatMap(profilePicUrl ->
                                userRepositoryPort.updateProfilePicUrl(profilePicUrl, userId)
                                        .flatMap(update ->
                                            update > 0 ? Mono.just(profilePicUrl) :  Mono.error(new Exception())
                                        )
                        ));
    }
}
