package com.czellmer1324.licenseplategame.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.ArrayList;

@Entity
@Getter
@Setter
@Table(name="Group", schema = "public")
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_id", nullable = false, unique = true)
    private Long groupId;

    @Column(name = "group_name", nullable = false)
    private String groupName;

    @ManyToMany
    private ArrayList<User> members;

    @OneToOne
    @JoinColumn(referencedColumnName = "user_id", nullable = false)
    private User groupOwner;

    @Column(name = "end_date")
    private ZonedDateTime endDate;
}
