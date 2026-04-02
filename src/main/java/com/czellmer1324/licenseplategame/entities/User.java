package com.czellmer1324.licenseplategame.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="USERS", schema = "public")
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int userId;
    private String userName;
    private String firstName;
    private String lastName;
    private String email;
    private String password;

    public User(String userName, String firstName, String lastName, String email, String password) {
        this.userName = userName;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
    }

    public User() {}
}
