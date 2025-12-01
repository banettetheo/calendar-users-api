package com.calendar.users.infrastructure.models.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Setter
@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "keycloak_id", nullable = false, unique = true)
    private String keycloakId;

    private LocalDateTime joinedDate;

    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<FriendshipEntity> friendships;

    // methodes utilitaires
    public void addFriend(UserEntity friend) {
        FriendshipEntity f1 = new FriendshipEntity(this, friend);
        FriendshipEntity f2 = new FriendshipEntity(friend, this);
        this.friendships.add(f1);
        friend.friendships.add(f2);
    }

    public void removeFriend(UserEntity friend) {
        this.friendships.removeIf(f -> f.getFriend().equals(friend));
        friend.friendships.removeIf(f -> f.getFriend().equals(this));
    }

}
