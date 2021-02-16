package com.jacky.imagecloud.models.items;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jacky.imagecloud.models.users.User;
import com.sun.istack.NotNull;

import javax.persistence.*;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.Set;

public class Item {

    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(name = "item_name", nullable = false, length = 128)
    private String ItemName;

    @Column(name = "item_type", nullable = false)
    private ItemType itemType;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @JsonIgnore
    @Column(name = "parent", nullable = true)
    private Integer parentID;

    // @JsonIgnore
    @Transient
    private Set<Item> SubItems;

    @OneToOne(mappedBy = "item")
    public FileStorage file;

    public Item() {
    }
    @JsonIgnore
    public Item GetTargetItem(@NotNull String path) throws FileNotFoundException {

        var pathGroup=path.split("/");

        Item temp=this;
        for (String p :
                pathGroup) {
            if(temp==null)
                throw new FileNotFoundException(String.format("path: `%s` not exist",path));
            temp=findTargetItem(temp,p);
        }
        return temp;
    }
    @JsonIgnore
    private Item findTargetItem(Item item, String path){
        for (Item subItem :
                item.SubItems) {
            if (subItem.ItemName.equals(path)){
                return subItem;
            }
        }
        return null;
    }

    public Set<Item> getSubItems() {
        return SubItems;
    }

    public void setSubItems(Set<Item> subItems) {
        this.SubItems = subItems;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getItemName() {
        return ItemName;
    }

    public void setItemName(String itemName) {
        this.ItemName = itemName;
    }

    public ItemType getItemType() {
        return itemType;
    }

    public void setItemType(ItemType itemType) {
        this.itemType = itemType;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Integer getParentID() {
        return parentID;
    }

    public void setParentID(Integer parentID) {
        this.parentID = parentID;
    }
}
