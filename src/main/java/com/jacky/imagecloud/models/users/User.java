package com.jacky.imagecloud.models.users;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jacky.imagecloud.models.items.Item;

import javax.persistence.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
public class User {
    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(nullable = false, length = 128, unique = true)
    private String emailAddress;

    @Column(nullable = false, length = 16)
    private String name;

    @Column(name = "password", nullable = false, length = 64)
    private String passWdHash;

    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL)
    private Set<Item> masterFiles;

    //@JsonIgnore
    @OneToOne(mappedBy = "user",fetch = FetchType.LAZY)
    public UserInformation information;

    @OneToOne(mappedBy = "user",fetch = FetchType.LAZY)
    public UserImage image;

    @Transient
    private Item rootItem;

    public  void constructItem(boolean withHidden){
        setRootItem(generateItemStruct(getAllItems(), withHidden,false));
    }
    public void constructItem(boolean withHidden,boolean withRemoved) {
        setRootItem(generateItemStruct(getAllItems(), withHidden,withRemoved));
    }

    public Integer getID() {
        return id;
    }

    public Item getRootItem() {
        return rootItem;
    }

    public void setRootItem(Item rootItem) {
        this.rootItem = rootItem;
    }

    public String getName() {
        return name;
    }

    @JsonIgnore
    public String getPasswordHash() {
        return passWdHash;
    }

    public void setID(Integer id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRawPassword(String password) {
        setPassword(password);
    }

    public void setPassword(String password) {
        this.passWdHash = password;
    }

    public void addItem(Item... items) {
        addItems(Arrays.asList(items));
    }

    @JsonIgnore
    public Set<Item> getAllItems() {
        return masterFiles;
    }

    public void addItems(Iterable<Item> items) {
        if (masterFiles == null) {
            masterFiles = new HashSet<>();
        }
        for (Item item : items) {
            if (!item.isRemoved)
                masterFiles.add(item);
        }
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    private Item generateItemStruct(Set<Item> items, boolean withHidden,boolean withRemoved) {
        Item root;
        var rootItems = findAllSUbItem(items, -1, withHidden,withRemoved);
        if (!rootItems.isEmpty()) {
            root = (Item) rootItems.toArray()[0];

            root.setSubItems(generateSubStruct(items, root, withHidden,withRemoved));
        } else {
            root = new Item();
        }
        return root;
    }

    private Set<Item> generateSubStruct(Set<Item> items, Item parentItem, boolean withHidden,boolean withRemoved) {
        var SubItems = findAllSUbItem(items, parentItem.getId(), withHidden,withRemoved);
        for (Item item : SubItems) {
            item.setSubItems(generateSubStruct(items, item, withHidden,withRemoved));
        }
        return SubItems;
    }

    private Set<Item> findAllSUbItem(Set<Item> items, int parent){
        return findAllSUbItem(items,parent,true,false);
    }
    private Set<Item> findAllSUbItem(Set<Item> items, int parent, boolean withHidden,boolean withRemoved) {
        Set<Item> target = new HashSet<>();
        for (Item item : items) {
            if (item.getParentID() == parent) {
                if(!withHidden&&item.hidden)
                    continue;
                if(!withRemoved&&item.isRemoved)
                    continue;
                target.add(item);
            }
        }
        return target;
    }

    public void setMasterFiles(Set<Item> masterFiles) {
        this.masterFiles = masterFiles;
    }
}
