package com.jacky.imagecloud.models.items;

import com.jacky.imagecloud.err.item.ItemNotFoundException;
import com.jacky.imagecloud.models.users.User;

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

    @ManyToMany(targetEntity = Item.class, fetch = FetchType.LAZY)
    List<Item> items;

    @ManyToOne(targetEntity = User.class, fetch = FetchType.LAZY)
    User user;

    LocalDateTime createTime;

    public static Share newShare(User user,Item... items) {
        Share share = new Share();
        share.uuid = UUID.randomUUID().toString();
        share.user=user;

        share.items = List.of(items);
        share.createTime = LocalDateTime.now();

        return share;
    }

    public String getCode() {
        return uuid;
    }

    public List<Item> getItems() {
        return items;
    }

    public User getUser() {
        return user;
    }

    public void constructUser() throws ItemNotFoundException {
        user.constructItem(true,false);
    }
}
