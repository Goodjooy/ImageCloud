package com.jacky.imagecloud.models.users;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jacky.imagecloud.err.item.ItemNotFoundException;
import com.jacky.imagecloud.err.user.BadUserInformationException;
import com.jacky.imagecloud.err.user.EmailAddressNotSupportException;
import com.jacky.imagecloud.err.user.UserNotFoundException;
import com.jacky.imagecloud.models.items.Item;
import org.springframework.data.domain.Example;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

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

    public static User databaseUser(UserRepository repository) throws UserNotFoundException {
        var authentication=SecurityContextHolder.getContext().getAuthentication();
        return databaseUser(repository,authentication);
    }
    public static User databaseUser(UserRepository repository,
                                    String emailAddress) throws UserNotFoundException {
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
                                    boolean withRemoved) throws UserNotFoundException, ItemNotFoundException {
        User user = databaseUser(repository, authentication);
        user.constructItem(withHidden, withRemoved);
        return user;
    }

    public static boolean verifiedUser(UserRepository repository, String emailAddress) throws UserNotFoundException {
        User user = databaseUser(repository,emailAddress);
        return user.information.verify;
    }

    public static void userNameCheck(String name) throws BadUserInformationException {
        if (name.length() > 16 || name.length() == 0)
            throw new BadUserInformationException("User Name Length Out Of Range [1,16]");
    }
    public static void userPasswordCheck(String password) throws BadUserInformationException {
        if (password.length() < 6 || password.length() > 32)
            throw new BadUserInformationException("Password Length Out Of Range [6, 32]");
    }

    public void constructItem(boolean withHidden, boolean withRemoved) throws ItemNotFoundException {
        rootItem = (generateItemStruct(withHidden, withRemoved));
    }

    @JsonIgnore
    @Transient
    public Map<String, Item> removedItems() throws ItemNotFoundException {
        if (flatRemovedItems == null) {
            flatRemovedItems = new HashMap<>();
            constructItem(true, true);
            //find all removed branch
            flatRemovedItems = findAllRemovedBranch();
        }
        return flatRemovedItems;
    }

    private Item generateItemStruct(boolean withHidden, boolean withRemoved) throws ItemNotFoundException {
        Item rootParent = Item.RootParentItem();
        //在此处筛选。后续可不用传递数据
        Set<Item> filteredItems = seizedFiles.stream().filter(item ->
        {
            // 没有显示隐藏且为隐藏文件           没有显示移除且被移除
            return (withHidden || !item.getHidden()) && (withRemoved || !item.getRemoved());
        }).collect(Collectors.toSet());

        rootParent.getAllSUbItemFromSet(filteredItems);
        var rootOption = rootParent.getSubItems().stream().findFirst();
        if (rootOption.isPresent()) {
            var root = rootOption.get();
            root.generateSubStruct(filteredItems);
            return root;
        } else {
            throw new ItemNotFoundException("Root");
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

    public void setUserName(String name) throws BadUserInformationException {
        userNameCheck(name);
        this.name=name;
    }

    public boolean targetItemNameSupport(String name,int parentId){
        var result=seizedFiles.stream().
                filter(item -> item.getParentID()==parentId).
                filter(item -> item.getItemName().equals(name));

        return result.count() <= 0;
    }

    public  void resetItemsStatus(){
        var t=seizedFiles.stream().map(Item::resetStatus);
    }

    @Override
    public String toString() {
        return String.format("User<%s | %s>",name,emailAddress);
    }
}
