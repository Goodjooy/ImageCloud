package com.jacky.imagecloud.models.users;

import javax.persistence.*;

@Entity
public class UserInformation {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Integer id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id",referencedColumnName = "id")
    public User user;

    @Column(nullable = false)
    public Long totalSize;

    @Column(nullable = false)
    public Long usedSize;

}
