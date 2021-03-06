package com.jacky.imagecloud.models.items;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jacky.imagecloud.FileStorage.FileService.FileSystemStorageService;
import com.jacky.imagecloud.data.Result;
import com.jacky.imagecloud.err.item.*;
import com.jacky.imagecloud.models.users.User;
import com.sun.istack.NotNull;

import javax.persistence.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
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
    @Column(nullable = false)
    private Boolean removed;

    @Column(name = "item_name", nullable = false, length = 128)
    private String itemName;

    @Column(name = "item_type", nullable = false)
    private ItemType itemType;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_id",nullable = false)
    private User user;

    @OneToOne(mappedBy = "item", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private ItemTime time;

    @JsonIgnore
    @Column(name = "parent",nullable = false)
    private Integer parentID;

    @Transient
    private List<Item> subItems;

    @OneToOne(mappedBy = "item", fetch = FetchType.LAZY)
    public FileStorage file;

    @Column(nullable = false)
    private Boolean hidden;

    @JsonIgnore
    @Transient
    private Boolean used = false;

    public Item() {
        subItems = new LinkedList<>();
    }

    public static Item NewDefaultItem() {
        Item item = DefaultItem();
        item.time = ItemTime.nowCreateTime(item);

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

        filename = URLDecoder.decode(filename, StandardCharsets.UTF_8);

        var sameNames = dir.getSameNameSubItem(filename, ItemType.FILE);
        if (sameNames.size() > 0) {
            var pos = filename.lastIndexOf(".");
            filename = filename.substring(0, pos) + " #" + UUID.randomUUID().toString().substring(0, 8) + filename.substring(pos);
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

    public static Item cloneItem(User user,
                                 Item rawItem,
                                 Item parentDir,
                                 ItemRepository repository,
                                 FileStorageRepository storageRepository,
                                 FileSystemStorageService storageService,
                                 boolean sameNameOK) throws ItemExistException {
        if (!sameNameOK)
            parentDir.sameNameWarn(rawItem.itemName, rawItem.itemType);

        Item newItem = NewDefaultItem();
        //copy value
        newItem.user = user;
        newItem.itemName = parentDir.sameNameAvoid(rawItem.itemName, rawItem.itemType);
        newItem.itemType = rawItem.itemType;
        newItem.setParentItem(parentDir);
        //add to user
        user.addItem(newItem);
        //save item
        repository.save(newItem);
        //clone sub items
        if (rawItem.itemType == ItemType.DIR) {
            var newSubItems = rawItem.cloneSubItems(newItem,
                    user,
                    repository,
                    storageRepository,
                    storageService,
                    sameNameOK);
            user.addItem(newSubItems.toArray(new Item[0]));
        } else {
            FileStorage storage = storageService.clone(rawItem.file.filePath);
            storage.item = newItem;
            newItem.file = storage;
            storageRepository.save(storage);
        }
        //save item
        repository.save(newItem);
        return newItem;
    }

    public void sameNameWarn(String filename, ItemType type) throws ItemExistException {
        var sameNames = getSameNameSubItem(filename, type);
        if (sameNames.size() > 0)
            throw new ItemExistException(itemName, filename);
    }

    public String sameNameAvoid(String filename, ItemType type) {
        var sameNames = getSameNameSubItem(filename, type);
        if (sameNames.size() > 0) {
            var pos = filename.contains(".") ? filename.lastIndexOf(".") : filename.length();
            filename = filename.substring(0, pos) + " #" + UUID.randomUUID().toString().substring(0, 8) + filename.substring(pos);
        }
        return filename;
    }

    public boolean resetStatus() {
        this.used = false;
        return false;
    }

    public List<String> getSameNameSubItem(String name, ItemType type) {
        return subItems.stream()
                .filter(item -> item.itemName.equals(name) && item.itemType == type)
                .map(item -> item.itemName).sorted().collect(Collectors.toList());

    }

    public void sortSubItems(ItemSort type, boolean reverse) {
        if (type == ItemSort.off) return;

        var dir = subItems.stream().filter(item -> item.itemType == ItemType.DIR).collect(Collectors.toList());
        var file = subItems.stream().filter(item -> item.itemType == ItemType.FILE).collect(Collectors.toList());
        Comparator<Item> comparator;
        switch (type) {
            case name: {
                comparator = Comparator.comparing(item -> item.itemName);
                break;
            }
            case modifyTime: {
                comparator = Comparator.comparing(item -> item.time.modifyTime);
                break;
            }
            case createTime:
            default: {
                comparator = Comparator.comparing(item -> item.time.createTime);
            }
        }
        if (reverse) {
            comparator = comparator.reversed();
        }

        var sortedFile = file.stream().sorted(comparator).collect(Collectors.toList());
        var sortedDir = dir.stream().sorted(comparator).collect(Collectors.toList());

        var temp = new LinkedList<Item>();
        temp.addAll(sortedFile);
        temp.addAll(sortedDir);

        subItems = temp;
    }

    public HashMap<String, Item> findAllRemoveSubItem() {
        HashMap<String, Item> result = new HashMap<>();
        var items = subItems.stream().filter(item -> item.removed
        ).map((item) -> result.put(String.format("%s/%s", itemName, item.itemName), item)).collect(Collectors.toSet());

        var re = subItems.stream().filter(item -> !item.removed).map(item -> {
            var temps = item.findAllRemoveSubItem();
            for (String key : temps.keySet()) {
                result.put(String.format("%s/%s", itemName, key), temps.get(key));
            }
            return temps.values();
        }).collect(Collectors.toSet());
        return result;
    }

    @JsonIgnore
    public LinkedList<Item> transformSubItemsToList() throws UnknownItemTypeException {
        var items = new LinkedList<Item>();
        for (Item item :
                subItems) {
            if (item.itemType == ItemType.DIR) {
                items.add(item);
                items.addAll(item.transformSubItemsToList());
            } else if (item.itemType == ItemType.FILE) {
                items.add(item);
            } else {
                throw new UnknownItemTypeException(String.format("Item Type<%s> not Support", item.itemType));
            }
        }
        return items;
    }

    @JsonIgnore
    public Item getTargetItem(@NotNull String path,
                              boolean withHidden) throws RootPathNotExistException, ItemNotFoundException {

        var pathGroup = splitPath(path);

        Item temp = this;
        for (String p :
                pathGroup) {
            temp = temp.findInSubItems(p, withHidden);
            if (temp == null) {
                throw new ItemNotFoundException(path);
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
    public Item findInSubItems(String path, boolean withHidden) {
        Item targetItem = null;

        if (itemName.equals(path) && !used) {
            used = true;
            targetItem = this;
            return targetItem;
        }

        if (subItems == null)
            subItems = new LinkedList<>();

        for (Item subItem :
                subItems) {
            if (subItem.itemName.equals(path) && !subItem.used) {
                targetItem = subItem;
                subItem.used = true;
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
        this.subItems = result.collect(Collectors.toList());
    }

    public void generateSubStruct(Set<Item> items) {
        getAllSUbItemFromSet(items);
        for (Item item : subItems) {
            item.generateSubStruct(items);
        }
    }

    public Boolean getRemoved() {
        return removed;
    }

    public void setRemoved(Boolean removed) {
        if (time != null)
            time.deleted();
        this.removed = removed;
    }

    public void setParentItem(Item item) {
        if (time != null)
            time.modified();
        parentID = item.getId();

    }

    public boolean isRootItem() {
        return parentID < 0 && itemName.equalsIgnoreCase("root");
    }

    public void setSameParentItem(Item sameParentItem) {
        if (time != null)
            time.modified();
        parentID = sameParentItem.getParentID();
    }

    public List<Item> getSubItems() {
        return subItems;
    }

    public Integer getId() {
        return id;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        if (time != null)
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

    public Boolean getHidden() {
        return hidden;
    }

    public void reverseHidden() throws RootPathCanNotBeHiddenException {
        if (isRootItem() && !hidden)
            throw new RootPathCanNotBeHiddenException();
        this.time.modifyTime = LocalDateTime.now();
        hidden = !hidden;
    }

    public List<Result<Boolean>> moveItemsInto(boolean sameNameItemOK, Iterable<Result<Item>> targetItems) throws ItemExistException {
        LinkedList<Result<Boolean>> results = new LinkedList<>();

        for (Result<Item> RItem :
                targetItems) {
            if (RItem.err)
                results.add(Result.failureResult(RItem.e));
            else {
                var item = RItem.data;
                item.setParentItem(this);
                if (!sameNameItemOK)
                    this.sameNameWarn(item.itemName, item.itemType);
                else
                    item.itemName = sameNameAvoid(item.itemName, item.itemType);

                results.add(Result.okResult(true));
            }
        }
        return results;
    }

    public void cloneTargetItems(boolean sameNameOK, FileSystemStorageService storageService,
                                 ItemRepository repository, FileStorageRepository storageRepository,
                                 Iterable<Item> items) throws ItemExistException {
        User user = this.user;

        for (Item item :
                items) {
            Item newItem = Item.cloneItem(user, item, this, repository,
                    storageRepository, storageService, sameNameOK);
            user.addItem(newItem);
        }
    }

    public List<Item> cloneSubItems(Item parent,
                                    User targetUser,
                                    ItemRepository repository,
                                    FileStorageRepository storageRepository,
                                    FileSystemStorageService storageService,
                                    boolean sameNameOK) throws ItemExistException {
        List<Item> items = new LinkedList<>();
        for (Item item : subItems) {
            items.add(cloneItem(targetUser,
                    item,
                    parent,
                    repository,
                    storageRepository,
                    storageService,
                    sameNameOK));
        }
        return items;
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

    public ItemTime getTime() {
            return time;
    }
}
