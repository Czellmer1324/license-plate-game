package com.czellmer1324.licenseplategame.entities;

import jakarta.persistence.*;

@Entity
@Table(name="SpottedStates", schema = "public")
public class SpottedStates {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long spotted_id;

    @ManyToOne
    @JoinColumn(name = "game_id")
    private Game game;

    private String state_code;
}
