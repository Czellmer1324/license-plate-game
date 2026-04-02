package com.czellmer1324.licenseplategame.repository.entities;

import jakarta.persistence.*;

@Entity
@Table(name="GAME", schema = "public")
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int game_id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
}
