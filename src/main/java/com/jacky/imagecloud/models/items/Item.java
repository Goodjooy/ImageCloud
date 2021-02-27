package com.jacky.imagecloud.models.items;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jacky.imagecloud.err.RootPathNotExistException;
import com.jacky.imagecloud.models.users.User;
import com.sun.istack.NotNull;

import javax.persistence.*;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "items")
public class Item {
    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @JsonIgnore
    public Boolean removed;

    @Column(name = "item_name", nullable = false, length = 128)
    private String itemName;

    @Column(name = "item_type", nullable = false)
    private ItemType itemType;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @JsonIgnore
    @Column(name = "parent")
    private Integer parentID;

    // @JsonIgnore
    @Transient
    private Set<Item> SubItems;

    @OneToOne(mappedBy = "item", fetch = FetchType.LAZY)
    public FileStorage file;

    @Column(nullable = false)
    public Boolean hidden;

    public Item() {
    }

    public static Item DefaultItem() {
        Item item = new Item();
        item.hidden = false;
        item.removed = false;
        return item;
    }

    public static Item RootItem(User user) {
        Item item = DefaultItem();
        item.user = user;
        item.itemName = "root";
        item.itemType = ItemType.DIR;
        item.parentID = -1;
        return item;
    }

    public static Item RootParentItem() {
        Item item = DefaultItem();
        item.id = -1;
        return item;
    }

    public static Item FileItem(User user, Item dir, String filename, boolean hidden) {
        Item item = DefaultItem();
        item.setItemName(filename);
        item.setItemType(ItemType.FILE);
        item.setUser(user);
        item.setParentItem(dir);
        item.hidden = hidden;

        return item;
    }

    public static Item DirItem(User user, Item parentDir, String dirName, boolean hidden) {
        Item t = Item.DefaultItem();
        t.setItemName(dirName);
        t.setUser(user);
        t.setItemType(ItemType.DIR);
        t.setParentItem(parentDir);
        t.hidden = hidden;

        return t;
    }

    public HashMap<String, Item> findAllRemoveSubItem() {
        HashMap<String, Item> result = new HashMap<>();
        var items = SubItems.stream().filter(item -> item.removed
        ).map((item) -> result.put(String.format("%s/%s",itemName,item.itemName), item)).collect(Collectors.toSet());
        var re = SubItems.stream().filter(item -> !item.removed).map(item -> {
            var temps = item.findAllRemoveSubItem();
            for (String key : temps.keySet()) {
                result.put(String.format("%s/%s", itemName, key), temps.get(key));
            }
            return temps.values();
        }).collect(Collectors.toSet());
        return result;
    }

    @JsonIgnore
    public LinkedList<Item> transformSubItemsToList() {
        var items = new LinkedList<Item>();
        for (Item item :
                SubItems) {
            switch (item.itemType) {
                case DIR: {
                    items.add(item);
                    items.addAll(item.transformSubItemsToList());

                    break;
                }
                case FILE: {
                    items.add(item);
                }
            }
        }
        return items;
    }

    @JsonIgnore
    public Item getTargetItem(@NotNull String path, boolean withHidden) throws FileNotFoundException, RootPathNotExistException {

        var pathGroup = splitPath(path);

        Item temp = this;
        for (String p :
                pathGroup) {
            temp = temp.findTargetItem(p, withHidden);
            if (temp == null) {
                throw new FileNotFoundException(String.format("path: `%s` not exist", path));
            }
        }
        return temp;
    }

    @JsonIgnore
    public String[] splitPath(String path) throws RootPathNotExistException {
        if (path.startsWith("/"))
            path = path.substring(1);
        if (path.endsWith("/"))
            path = path.substring(0, path.length() - 1);

        var groups = path.split("/");

        if (!groups[0].equals("root")) throw new RootPathNotExistException();
        return groups;
    }

    @JsonIgnore
    public Item findTargetItem(String path, boolean withHidden) {
        Item targetItem = null;

        if (itemName.equals(path))
            targetItem = this;

        for (Item subItem :
                SubItems) {
            if (subItem.itemName.equals(path)) {
                targetItem = subItem;
                break;
            }
        }
        if (!withHidden && targetItem != null && targetItem.hidden) {
            targetItem = null;
        }

        return targetItem;
    }

    public void getAllSUbItemFromSet(Set<Item> filteredItems) {
        var result = filteredItems.stream().filter(item -> item.getParentID() == (id));
        this.SubItems = result.collect(Collectors.toSet());
    }

    public void generateSubStruct(Set<Item> items) {
        getAllSUbItemFromSet(items);
        for (Item item : SubItems) {
            item.generateSubStruct(items);
        }
    }

    public void setParentItem(Item item) {
        parentID = item.getId();
    }

    public void setSameParentItem(Item sameParentItem) {
        parentID = sameParentItem.getParentID();
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
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
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

    public int getParentID() {
        return parentID == null ? -2 : parentID;
    }

    @Override
    public String toString() {
        return "Item{" +
                "removed=" + removed +
                ", itemName='" + itemName + '\'' +
                ", itemType=" + itemType +
                ", parentID=" + parentID +
                ", hidden=" + hidden +
                '}';
    }
}
