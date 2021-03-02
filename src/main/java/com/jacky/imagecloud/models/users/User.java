package com.jacky.imagecloud.models.users;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jacky.imagecloud.err.UserNotFoundException;
import com.jacky.imagecloud.models.items.Item;
import org.springframework.data.domain.Example;
import org.springframework.security.core.Authentication;

import javax.persistence.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "users")
public class User {
    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Integer id;

    @Column(nullable = false, length = 128, unique = true)
    public String emailAddress;

    @Column(nullable = false, length = 16)
    public String name;

    @JsonIgnore
    @Column(name = "password", nullable = false, length = 64)
    public String password;

    @JsonIgnore
    @Transient
    public Item rootItem;

    @JsonIgnore
    @Transient
    private Map<String, Item> flatRemovedItems;

    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    public Set<Item> seizedFiles;

    @JsonIgnore
    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    public UserInformation information;

    @JsonIgnore
    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    public UserImage image;

    public static User newUser(String name, String encodedPassword, String emailAddress) {
        User user = new User();
        user.name = name;
        user.emailAddress = emailAddress;
        user.password = encodedPassword;

        user.addItem(Item.RootItem(user));
        user.information = UserInformation.defaultUserInformation(user);
        user.image = UserImage.nullImage(user);

        return user;
    }

    public static User authUser(String emailAddress) {
        User user = new User();
        user.emailAddress = emailAddress;
        return user;
    }

    public static User databaseUser(UserRepository repository,
                                    String emailAddress) {
        User user = authUser(emailAddress);
        var result = repository.findOne(Example.of(user));
        if (result.isPresent()) {
            user = result.get();
            return user;
        } else {
            throw new UserNotFoundException(String.format(
                    "User<%s> not found", emailAddress
            ));
        }
    }

    public static User databaseUser(UserRepository repository,
                                    Authentication authentication) throws UserNotFoundException {
        return databaseUser(repository,authentication.getName());

    }

    public static User databaseUser(UserRepository repository,
                                    Authentication authentication,
                                    boolean withHidden,
                                    boolean withRemoved) throws UserNotFoundException {
        User user = databaseUser(repository, authentication);
        user.constructItem(withHidden, withRemoved);
        return user;
    }

    public static boolean verifiedUser(UserRepository repository, String emailAddress) {
        User user = databaseUser(repository,emailAddress);
        return user.information.verify;
    }

    public void constructItem(boolean withHidden, boolean withRemoved) {
        rootItem = (generateItemStruct(withHidden, withRemoved));
    }

    @JsonIgnore
    @Transient
    public Map<String, Item> removedItems() {
        if (flatRemovedItems == null) {
            flatRemovedItems = new HashMap<>();
            constructItem(true, true);
            //find all removed branch
            flatRemovedItems = findAllRemovedBranch();
        }
        return flatRemovedItems;
    }

    private Item generateItemStruct(boolean withHidden, boolean withRemoved) {
        Item rootParent = Item.RootParentItem();
        //在此处筛选。后续可不用传递数据
        Set<Item> filteredItems = seizedFiles.stream().filter(item ->
        {
            // 没有显示隐藏且为隐藏文件           没有显示移除且被移除
            return (withHidden || !item.hidden) && (withRemoved || !item.getRemoved());
        }).collect(Collectors.toSet());

        rootParent.getAllSUbItemFromSet(filteredItems);
        var rootOption = rootParent.getSubItems().stream().findFirst();
        if (rootOption.isPresent()) {
            var root = rootOption.get();
            root.generateSubStruct(filteredItems);
            return root;
        } else {
            return Item.RootItem(null);
        }
    }

    private HashMap<String, Item> findAllRemovedBranch() {
        return rootItem.findAllRemoveSubItem();
    }

    public void addItem(Item item) {
        if (seizedFiles == null) {
            seizedFiles = new HashSet<>();
        }
        seizedFiles.add(item);
    }

    @Override
    public String toString() {
        return String.format("User<%s | %s>",name,emailAddress);
    }
}
