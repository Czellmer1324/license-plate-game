package com.czellmer1324.licenseplategame.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name="groups", schema = "public")
@NoArgsConstructor
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_id", nullable = false, unique = true)
    private Long groupId;

    @Column(name = "group_name", nullable = false)
    private String groupName;

    @ManyToMany
    private List<User> members;

    @OneToOne
    @JoinColumn(referencedColumnName = "user_id", nullable = false)
    private User groupOwner;

    @Column(name = "end_date")
    private ZonedDateTime endDate;

    public Group(String groupName, User groupOwner, List<User> members, ZonedDateTime endDate) {
        this.groupName = groupName;
        this.groupOwner = groupOwner;
        this.members = members;
        this.endDate = endDate;
    }
}
