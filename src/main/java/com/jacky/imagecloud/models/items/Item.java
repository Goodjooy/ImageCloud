package com.jacky.imagecloud.models.items;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jacky.imagecloud.err.RootPathNotExistException;
import com.jacky.imagecloud.models.users.User;
import com.sun.istack.NotNull;

import javax.persistence.*;
import java.io.FileNotFoundException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(name = "items")
public class Item {
    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @JsonIgnore
    private Boolean removed;

    @Column(name = "item_name", nullable = false, length = 128)
    private String itemName;

    @Column(name = "item_type", nullable = false)
    private ItemType itemType;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @JsonIgnore
    @OneToOne(mappedBy = "item",fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    private ItemTime time;

    @JsonIgnore
    @Column(name = "parent")
    private Integer parentID;

    @Transient
    private List<Item> SubItems;

    @OneToOne(mappedBy = "item", fetch = FetchType.LAZY)
    public FileStorage file;

    @Column(nullable = false)
    public Boolean hidden;

    public Item() {
    }
    public static Item NewDefaultItem(){
        Item item=DefaultItem();
        item.time=ItemTime.nowCreateTime(item);

        return item;
    }

    public static Item DefaultItem() {
        Item item = new Item();
        item.hidden = false;
        item.removed = false;
        return item;
    }

    public static Item RootItem(User user) {
        Item item = NewDefaultItem();
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
        Item item = NewDefaultItem();

        filename=URLDecoder.decode(filename, StandardCharsets.UTF_8);

        var sameNames=dir.getSameNameSubItem(filename,ItemType.FILE);
        if(sameNames.size()>0){
            var pos=filename.lastIndexOf(".");
            filename=filename.substring(0,pos)+" #" +UUID.randomUUID().toString().substring(0,8)+filename.substring(pos);
        }

        item.setItemName(filename);
        item.setItemType(ItemType.FILE);
        item.setUser(user);
        item.setParentItem(dir);
        item.hidden = hidden;

        return item;
    }

    public static Item DirItem(User user, Item parentDir, String dirName, boolean hidden) {
        Item t = Item.NewDefaultItem();
        t.setItemName(dirName);
        t.setUser(user);
        t.setItemType(ItemType.DIR);
        t.setParentItem(parentDir);
        t.hidden = hidden;

        return t;
    }
    public List<String > getSameNameSubItem(String name,ItemType type){
        return SubItems.stream()
                .filter(item -> item.itemName.equals(name)&&item.itemType==type)
                .map(item -> item.itemName).sorted().collect(Collectors.toList());

    }

    public void sortSubItems(ItemSort type,boolean reverse){
        var dir=SubItems.stream().filter(item -> item.itemType==ItemType.DIR).collect(Collectors.toSet());
        var file=SubItems.stream().filter(item -> item.itemType==ItemType.FILE).collect(Collectors.toSet());
        Comparator<Item> comparator;
        switch (type){
            case name:{
                comparator=Comparator.comparing(item -> item.itemName);
                break;
            }
            case modifyTime:{
                    comparator=Comparator.comparing(item -> item.time.modifyTime);
                    break;
            }
            case createTime:
            default:{
                comparator=Comparator.comparing(item -> item.time.createTime);
            }
        }
        if(reverse){
            comparator=comparator.reversed();
        }

        var sortedFile=file.stream().sorted(comparator).collect(Collectors.toList());
        var sortedDir=dir.stream().sorted(comparator).collect(Collectors.toList());

        var temp=new LinkedList<Item>();
        temp.addAll(sortedFile);
        temp.addAll(sortedDir);

        SubItems=temp;
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
        this.SubItems = result.collect(Collectors.toList());
    }

    public void generateSubStruct(Set<Item> items) {
        getAllSUbItemFromSet(items);
        for (Item item : SubItems) {
            item.generateSubStruct(items);
        }
    }

    public Boolean getRemoved() {
        return removed;
    }

    public void setRemoved(Boolean removed) {
        if(time!=null)
            time.deleted();
        this.removed = removed;
    }

    public void setParentItem(Item item) {
        if(time!=null)
            time.modified();
        parentID = item.getId();

    }

    public void setSameParentItem(Item sameParentItem) {
        if(time!=null)
            time.modified();
        parentID = sameParentItem.getParentID();
    }

    public List<Item> getSubItems() {
        return SubItems;
    }

    public Integer getId() {
        return id;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        if(time!=null)
            time.modified();
        this.itemName = itemName;
    }

    public ItemType getItemType() {
        return itemType;
    }

    private void setItemType(ItemType itemType) {
        this.itemType = itemType;
    }

    public User getUser() {
        return user;
    }

    private void setUser(User user) {
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
