package com.jacky.imagecloud.models.items;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
public class Share {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Integer id;

    @Column(unique = true, updatable = false, nullable = false, length = 64)
    String uuid;

    @OneToMany(targetEntity = Item.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    List<Item> items;

    LocalDateTime createTime;

    public static Share newShare(Item...items) {
        Share share = new Share();
        share.uuid = UUID.randomUUID().toString();

        share.items=List.of(items);
        share.createTime=LocalDateTime.now();

        return share;
    }

}
