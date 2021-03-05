package com.jacky.imagecloud.models.users;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.UUID;

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

    public static UserImage nullImage(User user) {
        UserImage image = new UserImage();
        image.user = user;
        image.setHeaded = false;
        image.fileName = "unknown";
        return image;
    }

    public static UserImage generateNameImage(){
        UserImage image=new UserImage();
        image.setHeaded=false;
        image.fileName= UUID.randomUUID().toString()+".unknown";

        return image;
    }
    public UserImage combineImage(UserImage image){
        setHeaded=image.getSetHeaded();
        fileName=image.getFileName();

        return this;
    }

    public String getFileX512URL() {
        return String.format("/user/head512/%s", fileName);
    }

    public String getFileX256URL() {
        return String.format("/user/head256/%s", fileName);
    }

    public String getFileX128URL() {
        return String.format("/user/head128/%s", fileName);
    }

    public String getFileX64URL() {
        return String.format("/user/head64/%s", fileName);
    }

    public String getFileX32URL() {
        return String.format("/user/head32/%s", fileName);
    }

    public String getFileX16URL() {
        return String.format("/user/head16/%s", fileName);
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

    @Override
    public String toString() {
        return "UserImage{" +
                "user=" + user +
                ", setHeaded=" + setHeaded +
                ", fileName='" + fileName + '\'' +
                '}';
    }
}
