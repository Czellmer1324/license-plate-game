package com.czellmer1324.licenseplategame.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="SpottedStates", schema = "public")
@Getter
@Setter
public class SpottedStates {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "spotted_id", nullable = false, unique = true)
    private Long spottedId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name="state_code", length = 3, nullable = false)
    private String stateCode;

    public SpottedStates(User user, String stateCode) {
        this.user = user;
        this.stateCode = stateCode;
    }

    public SpottedStates() {}
}
