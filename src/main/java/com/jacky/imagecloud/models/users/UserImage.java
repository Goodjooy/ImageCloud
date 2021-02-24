package com.jacky.imagecloud.models.users;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;

@Entity
public class UserImage {
    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @JsonIgnore
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    private Boolean setHeaded;

    @JsonIgnore
    @Column(nullable = false, length = 64)
    private String fileName;

    public String getFIleX512URL() {
        return String.format("/head512/%s", fileName);
    }

    public String getFIleX256URL() {
        return String.format("/head256/%s", fileName);
    }

    public String getFIleX128URL() {
        return String.format("/head128/%s", fileName);
    }

    public String getFIleX64URL() {
        return String.format("/head64/%s", fileName);
    }

    public String getFIleX32URL() {
        return String.format("/head32/%s", fileName);
    }

    public String getFIleX16URL() {
        return String.format("/head16/%s", fileName);
    }


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public Boolean getSetHeaded() {
        return setHeaded;
    }

    public void setSetHeaded(Boolean setHeaded) {
        this.setHeaded = setHeaded;
    }
}
