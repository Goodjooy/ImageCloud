package com.jacky.imagecloud.models.items;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;

@Entity
public class FileStorage {
    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Integer id;

    @JsonIgnore
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "item_id", referencedColumnName = "id")
    public Item item;

    @JsonIgnore
    @Column(name = "path", nullable = false)
    public String filePath;

    public String getFileURL() {
        return String.format("/storage/%s", filePath);
    }
    public String getThumbnailURL(){
        return String.format("/thumbnail/%s",filePath);
    }

}
