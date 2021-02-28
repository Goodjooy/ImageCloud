package com.jacky.imagecloud.models.items;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.time.LocalDateTime;


public class ItemTime {
    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Integer id;

    @JsonIgnore
    @OneToOne(targetEntity = Item.class,cascade =CascadeType.ALL)
    @JoinColumn(name = "item_id",referencedColumnName = "id")
    public Item item;

    @Column(nullable = false)
    public LocalDateTime create;

    @Column(nullable = false)
    public LocalDateTime modify;

    public LocalDateTime delete;
}
