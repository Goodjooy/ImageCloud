package com.jacky.imagecloud.models.users;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jacky.imagecloud.err.UserNotFoundException;
import com.jacky.imagecloud.models.items.Item;
import org.springframework.data.domain.Example;
import org.springframework.security.core.Authentication;

import javax.persistence.*;
import java.util.HashSet;
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
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    public Set<Item> seizedFiles;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    public UserInformation information;

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
                                    Authentication authentication) throws UserNotFoundException {

            User user = authUser(authentication.getName());
            var result = repository.findOne(Example.of(user));
            if (result.isPresent()) {
                user = result.get();
                return user;
            } else {
                throw new UserNotFoundException(String.format(
                        "User<%s> not found", authentication.getName()
                ));
            }
    }

    public static User databaseUser(UserRepository repository,
                                    Authentication authentication,
                                    boolean withHidden,
                                    boolean withRemoved) throws UserNotFoundException {
        User user = databaseUser(repository, authentication);
        user.constructItem(withHidden,withRemoved);
        return user;
    }

    public void constructItemWithoutRemovedFiles(boolean withHidden) {
        rootItem = (generateItemStruct(withHidden, false));
    }

    public void constructItemWithoutHiddenFiles(boolean withRemoved) {
        rootItem = (generateItemStruct(false, withRemoved));
    }

    public void constructItem(boolean withHidden, boolean withRemoved) {
        rootItem = (generateItemStruct(withHidden, withRemoved));
    }


    private Item generateItemStruct(boolean withHidden, boolean withRemoved) {
        Item rootParent = Item.RootParentItem();
        //在此处筛选。后续可不用传递数据
        Set<Item> filteredItems = seizedFiles.stream().filter(item ->
        {
            // 没有显示隐藏且为隐藏文件           没有显示移除且被移除
            return (withHidden || !item.hidden) && (withRemoved || !item.removed);
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

    public void addItem(Item item) {
        if (seizedFiles == null) {
            seizedFiles = new HashSet<>();
        }
        seizedFiles.add(item);
    }
}
