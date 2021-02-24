package com.jacky.imagecloud.models.items;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jacky.imagecloud.err.RootPathNotExistException;
import com.jacky.imagecloud.models.users.User;
import com.sun.istack.NotNull;

import javax.persistence.*;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.Set;

@Entity
@Table(name = "items")
public class Item {

    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @JsonIgnore
    public Boolean isRemoved;

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

    @Column(nullable = false)
    public  Boolean hidden;

    public Item() {
    }

    public  static Item DefaultItem(){
        Item item=new Item();
        item.hidden=false;
        item.isRemoved=false;
        return item;
    }

    @JsonIgnore
    public LinkedList<Item>getAllSubItem(){
        var items=new LinkedList<Item>();
        for (Item item :
                SubItems) {
            switch (item.itemType){
                case DIR:
                {
                    items.addAll(item.getAllSubItem());
                    break;
                }
                case FILE:
                {
                    items.add(item);
                }
            }
        }
        return items;
    }

    @JsonIgnore
    public Item GetTargetItem(@NotNull String path,boolean withHidden) throws FileNotFoundException, RootPathNotExistException {

        var pathGroup = splitPath(path);

        Item temp = this;
        for (String p :
                pathGroup) {
            temp = findTargetItem(temp, p,withHidden);
            if (temp == null) {
                throw new FileNotFoundException(String.format("path: `%s` not exist", path));
            }
        }
        return temp;
    }

    @JsonIgnore
    public String[]splitPath(String path) throws RootPathNotExistException {
        if (path.startsWith("/"))
            path=path.substring(1);
        if(path.endsWith("/"))
            path=path.substring(0,path.length()-1);

        var groups = path.split("/");

        if (!groups[0].equals("root")) throw new RootPathNotExistException();
        return groups;
    }

    @JsonIgnore
    public Item findTargetItem(Item item, String path,boolean withHidden) {
        if(item.ItemName.equals(path))
            return item;
        for (Item subItem :
                item.SubItems) {
            if (subItem.ItemName.equals(path)) {
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
