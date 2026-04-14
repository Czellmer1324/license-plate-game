package com.czellmer1324.licenseplategame.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "invite")
@Getter
@Setter
@NoArgsConstructor
public class Invite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invite_id", nullable = false, unique = true)
    private Long inviteId;

   @ManyToOne
   private Group group;

    @ManyToOne
    private User user;

    public Invite(Group group, User user) {
        this.user = user;
        this.group = group;
    }
}
