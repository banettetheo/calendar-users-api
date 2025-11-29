package com.calendar.users.infrastructure.models.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Setter
@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class UserEntity {

    @Id
    @GeneratedValue
    private Long id;

    private String username;
    private String firstName;
    private String lastName;
    private String email;

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
