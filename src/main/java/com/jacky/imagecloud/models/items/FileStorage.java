package com.jacky.imagecloud.models.items;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jacky.imagecloud.models.items.Item;

import javax.persistence.*;

@Entity
public class FileStorage {
    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Integer id;

    @JsonIgnore
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "item_id",referencedColumnName = "id")
    public Item item;

    @Column(name = "path",nullable = false)
    public String filePath;

}
