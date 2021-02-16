package com.jacky.imagecloud.models.users;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;

@Entity
public class UserInformation {
    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Integer id;

    @JsonIgnore
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    public User user;


    @Column(nullable = false)
    public Long totalSize = 5368709120L;

    @Column(nullable = false)
    public Long usedSize = 0L;

}
