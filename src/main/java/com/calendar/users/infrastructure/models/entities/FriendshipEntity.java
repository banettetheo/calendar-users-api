package com.calendar.users.infrastructure.models.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table("user_friends")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FriendshipEntity {

    @Id
    private Long id;

    @Column("user_id")
    private Long userId;

    @Column("friend_id")
    private Long friendId;

    public FriendshipEntity(Long userId, Long friendId) {
        this.userId = userId;
        this.friendId = friendId;
    }
}