package com.jacky.imagecloud.models.users;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jacky.imagecloud.models.items.Item;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
public class User {
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
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
    private Set<Item> masterFiles;

    @JsonIgnore
    @OneToOne(mappedBy = "user")
    public UserInformation information;

    @Transient
    private Item rootItem;

    public void constructItem(){
        setRootItem(generateItemStruct(getAllItems()));
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

    @JsonIgnore
    public Set<Item> getAllItems() {
        return masterFiles;
    }

    public void addItems(Iterable<Item> items) {
        for (Item item : items) {
            masterFiles.add(item);
        }
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    private Item generateItemStruct(Set<Item> items) {
        Item root;
        var rootItems = findAllSUbItem(items, 0);
        if (!rootItems.isEmpty()) {
            root = (Item) rootItems.toArray()[0];

            root.setSubItems(generateSubStruct(items, root));
            return root;
        } else {
            root = new Item();
            return root;
        }
    }

    private Set<Item> generateSubStruct(Set<Item> items, Item parentItem) {
        var SubItems = findAllSUbItem(items, parentItem.getId());
        for (Item item : SubItems) {
            item.setSubItems(generateSubStruct(items, item));
        }
        return SubItems;
    }

    private Set<Item> findAllSUbItem(Set<Item> items, int parent) {
        Set<Item> target = new HashSet<>();
        for (Item item : items) {
            if (item.getParentID() == parent) {
                target.add(item);
            }
        }
        return target;
    }

    public void setMasterFiles(Set<Item> masterFiles) {
        this.masterFiles = masterFiles;
    }
}
