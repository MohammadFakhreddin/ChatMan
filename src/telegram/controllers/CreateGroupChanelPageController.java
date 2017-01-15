package telegram.controllers;

import com.mongodb.*;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.bson.types.ObjectId;
import telegram.Constants;
import telegram.models.Chat;
import telegram.models.User;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by M.Fakhreddin on 6/28/2016.
 */
public class CreateGroupChanelPageController implements Initializable {
    @FXML
    private ListView membersList;
    @FXML
    private ListView contactList;
    @FXML
    private Button createBtn;
    @FXML
    private TextField groupNameField;
    @FXML
    private Label chanelLabel;
    @FXML
    private Label groupLabel;
    private Stage stage;
    private boolean isChanel;
    private User owner;
    private List<User> allUsers;
    private ListProperty<String> contactListProperty;
    private List<String> contactItems;
    private List<Integer> contactId;
    private ListProperty<String> membersListProperty;
    private List<Integer> memberId;
    private List<String> membersItem;
    private DB rootDB;
    private boolean isContactSelected;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        contactListProperty = new SimpleListProperty<>();
        membersListProperty = new SimpleListProperty<>();
        contactList.itemsProperty().bind(contactListProperty);
        membersList.itemsProperty().bind(membersListProperty);
        contactItems = new ArrayList<>();
        contactId = new ArrayList<>();
        memberId = new ArrayList<>();
        membersItem = new ArrayList<>();
        rootDB = Constants.getRootDB();

    }

    public void start(Stage stage, boolean isChanel, User owner, List<User> allUsers) {
        this.stage = stage;
        this.isChanel = isChanel;
        this.owner = owner;
        this.allUsers = allUsers;
        if (isChanel) {
            chanelLabel.setVisible(true);
            groupLabel.setVisible(false);
        } else {
            chanelLabel.setVisible(false);
            groupLabel.setVisible(true);
        }
        for (int i = 0; i < allUsers.size(); i++) {
            contactItems.add(allUsers.get(i).name);
            contactId.add(allUsers.get(i).id);
        }
        contactListProperty.set(FXCollections.observableArrayList(contactItems));
        contactList.getSelectionModel().selectedItemProperty().addListener(contactChangeListener);
        contactList.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                isContactSelected = true;
                contactChangeListener.changed(null, contactLastVal, contactLastVal);
            }
        });
        membersListProperty.set(FXCollections.observableArrayList(membersItem));
        membersList.getSelectionModel().selectedItemProperty().addListener(memberChangeListener);
        membersList.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                isContactSelected = false;
                memberChangeListener.changed(null, memberLastVal, memberLastVal);
            }
        });

    }

    private String contactLastVal = "";
    private ChangeListener contactChangeListener = new ChangeListener<String>() {
        @Override
        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
            contactLastVal = newValue;
            if (isContactSelected) {
                if (newValue != null) {
                    int index = contactItems.indexOf(newValue);
                    membersItem.add(contactItems.remove(index));
                    memberId.add(contactId.remove(index));
                    contactListProperty.set(FXCollections.observableArrayList(contactItems));
                    membersListProperty.set(FXCollections.observableArrayList(membersItem));
                }
            }
        }
    };

    private String memberLastVal = "";
    private ChangeListener memberChangeListener = new ChangeListener<String>() {
        @Override
        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
            memberLastVal = newValue;
            if (!isContactSelected) {
                if (newValue != null) {
                    int index = membersItem.indexOf(newValue);
                    contactItems.add(membersItem.remove(index));
                    contactId.add(memberId.remove(index));
                    contactListProperty.set(FXCollections.observableArrayList(contactItems));
                    membersListProperty.set(FXCollections.observableArrayList(membersItem));
                }
            }

        }
    };

    public void OnAction(ActionEvent event) {
        if (event.getSource() == createBtn) {
            if (groupNameField.getText() != "") {
                DBCollection chatCol = rootDB.getCollection(Constants.CHAT_COL);
                String groupName = groupNameField.getText();
                BasicDBObject query = new BasicDBObject();
                query.append(Constants.CHAT.CHAT_NAME, groupName);
                DBCursor c = chatCol.find(query);
                if (c == null || c.size() == 0) {
                    DBCollection userCol = rootDB.getCollection(Constants.USER_COL_NAME);
                    query = new BasicDBObject();
                    query.append(Constants.USER.NAME, groupName);
                    c = userCol.find(query);
                    if (c == null || c.size() == 0) {
                        Chat chat = new Chat();
                        chat.talkList = new ArrayList<>();
                        chat.chatId = ObjectId.get().toString();
                        chat.chatName = groupName;
                        chat.adminId = owner.id;
                        chat.members = new ArrayList<>();
                        chat.isChanel = isChanel;
                        memberId.add(owner.id);
                        chat.members = memberId;
                        BasicDBObject rawChat = Constants.generateRawChat(chat);
                        chatCol.insert(rawChat);
                        for (int i = 0; i < chat.members.size(); i++) {
                            query = new BasicDBObject();
                            query.append(Constants.USER.ID, chat.members.get(i));
                            BasicDBObject curUser = (BasicDBObject) userCol.findOne(query);
                            BasicDBList groupList = (BasicDBList) curUser.get(Constants.USER.GROUP_LIST);
                            if (groupList == null) {
                                groupList = new BasicDBList();
                            }
                            groupList.add(chat.chatId);
                            curUser.put(Constants.USER.GROUP_LIST, groupList);
                            userCol.update(query, curUser);
                        }
                        System.out.println("Created successfully");
                        stage.close();
                    }
                }
                System.out.println("Cannot create group/chanel beacuase it exist");
            }
        }
    }
}
