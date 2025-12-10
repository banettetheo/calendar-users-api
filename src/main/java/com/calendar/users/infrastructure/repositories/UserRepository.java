package com.calendar.users.infrastructure.repositories;

import com.calendar.users.infrastructure.models.dtos.UserWithStatusDatabaseDTO;
import com.calendar.users.infrastructure.models.entities.UserEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface UserRepository extends ReactiveCrudRepository<UserEntity, Long> {

    @Query("SELECT u.* FROM app_user u " +
            "INNER JOIN user_friends f ON u.id = f.friend_id " +
            "WHERE f.user_id = :userId")
    Flux<UserEntity> findFriendByUserId(Long userId, Pageable pageable);

    Mono<UserEntity> findByKeycloakId(String keycloakId);

    @Modifying
    @Query("UPDATE app_user SET profile_pic_url = :url WHERE keycloak_id = :keycloakId")
    Mono<Integer> updateProfilePicUrlByKeycloakId(String url, String keycloakId);

    /**
     * Récupère la liste des utilisateurs avec le statut de relation calculé
     * par rapport à l'utilisateur principal.
     */
    @Query("""
        SELECT
            target_user.id,
            target_user.keycloak_id,
            target_user.profile_pic_url,
            target_user.joined_date,
            CASE
                WHEN uf.user_id IS NOT NULL THEN 'FRIENDS'
                WHEN fr_sent.sender_id IS NOT NULL THEN 'PENDING_SENT'
                WHEN fr_received.receiver_id IS NOT NULL THEN 'PENDING_RECEIVED'
                ELSE 'NOT_FRIENDS'
            END AS relationship_status
        FROM
            app_user AS target_user
        JOIN
            (SELECT id AS principal_id FROM app_user WHERE keycloak_id = :keycloakId) AS up ON TRUE
        LEFT JOIN user_friends uf
            ON uf.user_id = up.principal_id AND uf.friend_id = target_user.id
        LEFT JOIN friend_requests fr_sent
            ON fr_sent.sender_id = up.principal_id AND fr_sent.receiver_id = target_user.id
        LEFT JOIN friend_requests fr_received
            ON fr_received.sender_id = target_user.id AND fr_received.receiver_id = up.principal_id
        WHERE
            target_user.id != up.principal_id
        ORDER BY
            target_user.joined_date DESC
        LIMIT 50;
    """)
    Flux<UserWithStatusDatabaseDTO> findUsersWithStatus(String keycloakId);
}
