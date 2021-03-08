package com.jacky.imagecloud.models.items;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
public class ItemTime {
    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Integer id;

    @JsonIgnore
    @OneToOne(targetEntity = Item.class, cascade = CascadeType.ALL)
    @JoinColumn(name = "item_id", referencedColumnName = "id", nullable = false)
    public Item item;

    @Column(nullable = false)
    public LocalDateTime createTime;
    @Column(nullable = false)
    public LocalDateTime modifyTime;
    public LocalDateTime deleteTime;

    public static ItemTime nowCreateTime(Item item) {
        ItemTime time = new ItemTime();
        time.createTime = LocalDateTime.now();
        time.modifyTime = LocalDateTime.now();
        time.deleteTime = null;

        time.item = item;
        return time;
    }

    public void modified() {
        modifyTime = LocalDateTime.now();
    }

    public void deleted() {
        deleteTime = LocalDateTime.now();
    }

    public String getCreateTime() {
        return createTime.toString();
    }

    public String getModifyTime() {
        return modifyTime.toString();
    }

    public String getDeleteTime() {
        return deleteTime == null ? null : deleteTime.toString();
    }
}
