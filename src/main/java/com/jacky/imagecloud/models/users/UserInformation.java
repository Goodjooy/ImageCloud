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
    public Long totalSize;

    @Column(nullable = false)
    public Long usedSize = 0L;

    public static UserInformation defaultUserInformation(){
        UserInformation information=new UserInformation();
        information.totalSize=5368709120L;
        information.usedSize=0L;

        return information;
    }

    public Long availableSize() {
        return totalSize - usedSize;
    }

    public void AppendSize(Long size) {
        var t = usedSize + size;
        t = t < 0 ? 0 : t;
        usedSize = t;
    }

}
