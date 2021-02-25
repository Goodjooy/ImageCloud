package com.jacky.imagecloud.models.users;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jacky.imagecloud.err.UserNotFoundException;
import com.jacky.imagecloud.models.items.Item;
import org.springframework.data.domain.Example;
import org.springframework.security.core.Authentication;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

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

    @Column(name = "password", nullable = false, length = 64)
    public String password;

    @Transient
    public Item rootItem;

    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    public Set<Item> seizedFiles;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    public UserInformation information;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    public UserImage image;

    public static User newUser(String name,String encodedPassword,String emailAddress){
        User user=new User();
        user.name=name;
        user.emailAddress=emailAddress;
        user.password=encodedPassword;

        return user;
    }
    public static User authUser(String emailAddress){
        User user=new User();
        user.emailAddress=emailAddress;
        return user;
    }

    public static User databaseUser(UserRepository repository,
                                    Authentication authentication,
                                    boolean withHidden,
                                    boolean withRemoved) throws UserNotFoundException {
        User user=authUser(authentication.getName());
        var result=repository.findOne(Example.of(user));
        if(result.isPresent()){
            user=result.get();
            user.constructItem(withHidden,withRemoved);
            return user;
        }else {
            throw new UserNotFoundException(String.format(
                    "User<%s> not found",authentication.getName()
            ));
        }
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
        rootParent.getAllSUbItemFromSet(seizedFiles, withHidden, withRemoved);
        var rootOption = rootParent.getSubItems().stream().findFirst();
        if (rootOption.isPresent()) {
            var root = rootOption.get();
            root.generateSubStruct(seizedFiles, withHidden, withRemoved);
            return root;
        } else {
            return Item.RootItem();
        }
    }

    public void addItem(Item item) {
        if (seizedFiles==null){
            seizedFiles=new HashSet<>();
        }
        seizedFiles.add(item);
    }
}
