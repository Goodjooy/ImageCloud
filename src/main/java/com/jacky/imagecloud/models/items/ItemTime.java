package com.jacky.imagecloud.models.items;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.time.LocalDateTime;


public class ItemTime {
    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Integer id;

    @Column(nullable = false)
    LocalDateTime create;
}
