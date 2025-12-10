package com.calendar.users.domain.ports;

import com.calendar.users.domain.models.BusinessUser;
import com.calendar.users.domain.models.UserWithStatusDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserRepositoryPort {

    Mono<BusinessUser> save(BusinessUser businessUser, String keycloakId);

    Mono<BusinessUser> getBusinessUserByKeycloakId(String keycloakId);

    Mono<Integer> updateProfilePicUrl(String profilePicUrl, String keycloakId);

    Flux<UserWithStatusDTO> getUsersWithRelationshipStatus(String keycloakId);
}
