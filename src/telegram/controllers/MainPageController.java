package telegram.controllers;

import com.mongodb.*;
import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.bson.types.ObjectId;
import telegram.Constants;
import telegram.models.Chat;
import telegram.models.PrivateTalk;
import telegram.models.Talk;
import telegram.models.User;

import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * Created by M.Fakhreddin on 6/24/2016.
 */
public class MainPageController implements Initializable {
    @FXML
    private Button sendBtn;
    @FXML
    private TextField inputField;
    @FXML
    private TextArea chatField;
    @FXML
    private ListView contactList;
    @FXML
    private Button refreshBtn;
    @FXML
    private Button moreMessages;
    @FXML
    private Button privateChat;
    @FXML
    private Button myProfile;
    @FXML
    private Button targetUserProfile;
    @FXML
    private Label privateChatMod;
    @FXML
    private ListView friendAndGroupListView;
    @FXML
    private Button searchBtn;
    @FXML
    private Button cancelBtn;
    @FXML
    private TextField searchField;
    private DB rootDB;
    private Stage stage;
    private User owner;
    private DBCollection talkCol;
    private List<User> allUser;//Except owner
    private ListProperty<String> contactsListProperty;
    private ListProperty<String> friendsListProperty;
    private List<String> contactItems;
    //    private List<Integer> contactID;
    private List<String> groupsAndFriendItems;
    //    private List<Integer> groupAndFriendID;
    private int userId;
    private Chat currentChat;
    private boolean endOfProgram;//The fetching thread for updating
    private boolean isPrivateChat = false;
    private int targetUserId = -1;
    private boolean isFriendListSelected;
    private boolean isGroupChat;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        try {
            // Now connect to your databases
            this.rootDB = Constants.getRootDB();
            contactsListProperty = new SimpleListProperty<>();
            contactList.itemsProperty().bind(contactsListProperty);
            friendsListProperty = new SimpleListProperty<>();
            friendAndGroupListView.itemsProperty().bind(friendsListProperty);
            isPrivateChat = false;
            privateChatMod.setVisible(false);
            cancelBtn.setVisible(false);
            System.out.println("Connect to database successfully");

//            DBCollection userCol = rootDB.getCollection(Constants.USER_COL_NAME);
//            for (int i = 0; i < 10; i++) {
//                BasicDBObject query = new BasicDBObject();
//                query.append(Constants.USER.NAME, "a" + i);
//                query.append(Constants.USER.PASSWORD, i + "");
//                DBCursor cC = userCol.find();M
//                int userId = i + 6;//cC.size() + 1;
//                query.append(Constants.USER.ID, userId);
//                query.append(Constants.USER.IS_REPORTED, false);
//                query.append(Constants.USER.REPORT_COUNT, 0);
//                System.out.println(query);
//                BasicDBObject delQuery = new BasicDBObject();
//                delQuery.append(Constants.USER.NAME, "a" + i);
//                //MuserCol.remove(delQuery);
//                userCol.insert(query);
//            }
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
    }

    public void setCurrUser(int userId) {
        this.userId = userId;
        refresh();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void OnAction(ActionEvent event) {
        if (event.getSource() == sendBtn) {
            if (!inputField.getText().trim().equals("")) {
                send(inputField.getText().trim());
                inputField.setText("");
            }
        } else if (event.getSource() == refreshBtn) {
            refresh();
        } else if (event.getSource() == moreMessages) {
            loadMoreMessage();
        } else if (event.getSource() == privateChat) {
            if (!isGroupChat) {
                if (isPrivateChat) {
                    cancelRefresher();
                    currentChat.talkList = new ArrayList<>();
                    System.out.println("Starting private chat");
                    isPrivateChat = false;
                    privateChatMod.setVisible(false);
                    refresh();
                } else {
                    cancelRefresher();
                    currentChat.talkList = new ArrayList<>();
                    System.out.println("Starting normal chat");
                    isPrivateChat = true;
                    privateChatMod.setVisible(true);
                    refresh();
                }
            } else {
                privateChatMod.setVisible(false);
                isPrivateChat = false;
            }
        } else if (event.getSource() == myProfile) {
            Stage mainPage = new Stage();
//        mainPage.initStyle(StageStyle.UNDECORATED);
            mainPage.initModality(Modality.WINDOW_MODAL);
            mainPage.initOwner(((Node) event.getSource()).getScene().getWindow());
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("../views/UserPageView.fxml"));
            Parent content = null;
            try {
                content = loader.load();
                mainPage.setScene(new Scene(content));
                mainPage.setResizable(false);
                mainPage.show();
                UserPageController controller = loader.getController();
                controller.start(mainPage, owner, allUser);
                mainPage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                    public void handle(WindowEvent we) {
                        refresh();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (event.getSource() == targetUserProfile) {
            User targetUser = findUser(targetUserId);
            if (targetUser != null) {
                Stage mainPage = new Stage();
//        mainPage.initStyle(StageStyle.UNDECORATED);
                mainPage.initModality(Modality.WINDOW_MODAL);
                mainPage.initOwner(((Node) event.getSource()).getScene().getWindow());
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(getClass().getResource("../views/UserPageView.fxml"));
                Parent content = null;
                try {
                    content = loader.load();
                    mainPage.setScene(new Scene(content));
                    mainPage.setResizable(false);
                    mainPage.show();
                    UserPageController controller = loader.getController();
                    boolean isFriend = false;
                    if (owner.friendList.contains(targetUserId)) {
                        isFriend = true;
                    }
                    boolean isBlocked = false;
                    if (owner.blockedList.contains(targetUserId)) {
                        isBlocked = true;
                    }
                    controller.start(mainPage, targetUser, owner, isFriend, isBlocked, allUser);
                    mainPage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                        public void handle(WindowEvent we) {
                            refresh();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else if (event.getSource() == searchBtn) {
            if (!isPrivateChat) {
                if (!searchField.getText().trim().equals("")) {
                    cancelBtn.setVisible(true);
                    cancelRefresher();
                    clearChatField();
                    startSearch(searchField.getText().trim());
                }
            }
        } else if (event.getSource() == cancelBtn) {
            if (!isPrivateChat) {
                refresh();
                cancelBtn.setVisible(false);
            }
        }
    }

    private void startSearch(String searchText) {
        talkCol = rootDB.getCollection(Constants.TALK_NAME);
        List<Talk> talkList = new ArrayList<>();
        BasicDBObject query = new BasicDBObject();
        query.append(Constants.TALK.PARENT_CHAT_ID, currentChat.chatId);
        DBCursor c = talkCol.find(query);
        if (c == null || c.size() == 0) {
            chatField.setText("No results found\n");
        } else {
            while (c.hasNext()) {
                BasicDBObject rawTalk = (BasicDBObject) c.next();
                Talk talk = Constants.generateTalkObj(rawTalk);
                if (talk.name.contains(searchText)) {
                    talkList.add(talk);
                } else if (talk.text.contains(searchText)) {
                    talkList.add(talk);
                }
            }
            chatField.appendText("\nResults found:" + talkList.size() + "\n");
            for (int i = 0; i < talkList.size(); i++) {
                showTextToUser(talkList.get(i));
            }
        }
    }

    private User findUser(int userId) {
        for (int i = 0; i < allUser.size(); i++) {
            if (allUser.get(i).id == userId) {
                return allUser.get(i);
            }
        }
        return null;
    }

    private void loadMoreMessage() {
        if (currentChat != null) {
            talkCol = rootDB.getCollection(Constants.TALK_NAME);
            BasicDBObject query = new BasicDBObject();
            BasicDBObject subQuery = new BasicDBObject();
            subQuery.append("$lt", currentChat.talkList.get(0).date);
            query.append(Constants.TALK.PARENT_CHAT_ID, currentChat.chatId);
            query.append(Constants.TALK.DATE, subQuery);
            BasicDBObject sortQuery = new BasicDBObject();
            sortQuery.append(Constants.TALK.DATE, -1);
            DBCursor c = talkCol.find(query).sort(sortQuery).limit(Constants.LOAD_MESSAGE_MAX);
            List<Talk> newTalks = new ArrayList<>();
            while (c.hasNext()) {
                BasicDBObject rawTalk = (BasicDBObject) c.next();
                Talk talk = Constants.generateTalkObj(rawTalk);
//                if (!currentChat.talkList.contains(talk)) {
                newTalks.add(talk);
//                }
            }
            newTalks.sort(new Comparator<Talk>() {
                @Override
                public int compare(Talk talk1, Talk talk2) {
                    return talk1.date.compareTo(talk2.date);
                }
            });
            currentChat.talkList.addAll(0, newTalks);
            clearChatField();
            for (int i = 0; i < currentChat.talkList.size(); i++) {
                showTextToUser(currentChat.talkList.get(i));
            }
        }
    }

    private void refresh() {
        System.out.println("Refreshing");
        cancelRefresher();
        clearChatField();
        DBCollection userCol = rootDB.getCollection(Constants.USER_COL_NAME);
        DBCursor uCursor = userCol.find();
        if (uCursor.size() >= 1)//Means we have more than one user
        {
            contactItems = new ArrayList<>();
            List<Integer> contactID = new ArrayList<>();
            allUser = new ArrayList<>();
            groupsAndFriendItems = new ArrayList<>();
//            groupAndFriendID = new ArrayList<>();
            while (uCursor.hasNext()) {
                BasicDBObject currUser = (BasicDBObject) uCursor.next();
                User newUser = new User();
                newUser.id = currUser.getInt(Constants.USER.ID);
                newUser.name = currUser.getString(Constants.USER.NAME);
                newUser.password = currUser.getString(Constants.USER.PASSWORD);
                newUser.isReported = currUser.getBoolean(Constants.USER.IS_REPORTED);
                newUser.totalReportCount = currUser.getInt(Constants.USER.REPORT_COUNT);
                newUser.reportExpireTime = currUser.getDate(Constants.USER.REPORT_EXPIRE_TIME);
                newUser.phoneNumber = currUser.getString(Constants.USER.PHONE_NUMBER);
                newUser.moreInfo = currUser.getString(Constants.USER.MORE_INFO);
                newUser.realName = currUser.getString(Constants.USER.NAME_AND_FAMILY_NAME);
                BasicDBList friendRawData = (BasicDBList) currUser.get(Constants.USER.FRIEND_LIST);
                newUser.friendList = new ArrayList<>();
                if (friendRawData != null) {
                    for (int i = 0; i < friendRawData.size(); i++) {
                        newUser.friendList.add((Integer) friendRawData.get(i));
                    }
                }
                BasicDBList blockedRawData = (BasicDBList) currUser.get(Constants.USER.BLOCKED_LIST);
                newUser.blockedList = new ArrayList<>();
                if (blockedRawData != null) {
                    for (int i = 0; i < blockedRawData.size(); i++) {
                        newUser.blockedList.add((Integer) blockedRawData.get(i));
                    }
                }
                BasicDBList groupRawData = (BasicDBList) currUser.get(Constants.USER.GROUP_LIST);
                newUser.groupAndChanelList = new ArrayList<>();
                if (groupRawData != null) {
                    for (int i = 0; i < groupRawData.size(); i++) {
                        newUser.groupAndChanelList.add((String) groupRawData.get(i));
                    }
                }
                if (newUser.id != userId) {//Means it's not owner
                    allUser.add(newUser);
                    contactItems.add(newUser.name);
                    contactID.add(newUser.id);
                } else {
                    this.owner = newUser;
                    this.owner.mentionedGroupNameList = new ArrayList<>();
                }
            }
            if (owner.isReported) {
                if (owner.reportExpireTime.compareTo(new Date(System.currentTimeMillis())) < 0) {
                    owner.isReported = false;
                    Constants.updateUser(owner);
                    sendBtn.setVisible(true);
                } else {
                    chatField.setText("Sorry but you are reported for amount of Time\n" + owner.reportExpireTime.getDay() + ":"
                            + owner.reportExpireTime.getHours() + ":" + owner.reportExpireTime.getMinutes()
                            + "\nD:\n");
                    sendBtn.setVisible(false);
                    return;
                }
            }
            DBCollection chatCol = rootDB.getCollection(Constants.CHAT_COL);
            for (int i = 0; i < owner.groupAndChanelList.size(); i++) {
                BasicDBObject query = new BasicDBObject();
                query.append(Constants.CHAT.CHAT_ID, owner.groupAndChanelList.get(i));
                BasicDBObject rawGroup = (BasicDBObject) chatCol.findOne(query);
                String name = rawGroup.getString(Constants.CHAT.CHAT_NAME);
                BasicDBList mentionedMembers = (BasicDBList) rawGroup.get(Constants.CHAT.MENTIONED_MEMBERS);
                if (mentionedMembers != null && mentionedMembers.contains(owner.id)) {
                    owner.mentionedGroupNameList.add(name);
                }
                groupsAndFriendItems.add(name);
//                groupAndFriendID.add(owner.groupAndChanelList.get(i));
            }
            for (int i = contactID.size() - 1; i >= 0; i--) {
                if (owner.friendList.contains(contactID.get(i))) {
                    groupsAndFriendItems.add(contactItems.remove(i));
//                    groupAndFriendID.add(contactID.remove(i));
                }
            }
            for (int i = 0; i < groupsAndFriendItems.size(); i++) {
                if (owner.mentionedGroupNameList.contains(groupsAndFriendItems.get(i))) {
                    groupsAndFriendItems.set(i, "@" + groupsAndFriendItems.get(i));
                }
            }
            contactsListProperty.set(FXCollections.observableArrayList(contactItems));
            contactList.getSelectionModel().selectedItemProperty().addListener(contactChangeListener);
            contactList.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    isFriendListSelected = false;
                    contactChangeListener.changed(null, contactLastVal, contactLastVal);
                }
            });
            friendsListProperty.set(FXCollections.observableArrayList(groupsAndFriendItems));
            friendAndGroupListView.getSelectionModel().selectedItemProperty().addListener(friendChangeListener);
//            friendAndGroupListView.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
//                @Override
//                public ListCell<String> call(ListView<String> list) {
//                    return new ColorRectCell();
//                }
//            });
            friendAndGroupListView.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    isFriendListSelected = true;
                    friendChangeListener.changed(null, lastSelectedFreindGroup, lastSelectedFreindGroup);
                }
            });

        }

    }

    private String contactLastVal;
    private ChangeListener<String> contactChangeListener = new ChangeListener<String>() {
        @Override
        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
            contactLastVal = newValue;
            if (!isFriendListSelected) {
                cancelRefresher();
                clearChatField();
                BasicDBObject query = new BasicDBObject();
                query.append(Constants.USER.NAME, newValue);
                DBCollection userCol = rootDB.getCollection(Constants.USER_COL_NAME);
                BasicDBObject targetUser = (BasicDBObject) userCol.findOne(query);
                if (targetUser != null) {
                    MainPageController.this.targetUserId = targetUser.getInt(Constants.USER.ID);
                    BasicDBList rawBlockList = (BasicDBList) targetUser.get(Constants.USER.BLOCKED_LIST);
                    List<Integer> blockList = new ArrayList<Integer>();
                    if (rawBlockList != null) {
                        for (int i = 0; i < rawBlockList.size(); i++) {
                            blockList.add((Integer) rawBlockList.get(i));
                        }
                    }
                    if (checkForBlock(owner.blockedList, blockList, owner.id, targetUserId)) {
                        sendBtn.setVisible(false);
                        return;
                    } else {
                        sendBtn.setVisible(true);
                    }
                    if (!isPrivateChat) {
                        startNormalChat(targetUserId);
                    } else {
                        startPrivateChat(targetUserId);
                    }
                }
            }
        }
    };

    private String lastSelectedFreindGroup;
    private ChangeListener<String> friendChangeListener = new ChangeListener<String>() {
        @Override
        public synchronized void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
            lastSelectedFreindGroup = newValue;
            if (isFriendListSelected) {
                cancelRefresher();
                clearChatField();
                BasicDBObject query = new BasicDBObject();
                query.append(Constants.USER.NAME, newValue);
                DBCollection userCol = rootDB.getCollection(Constants.USER_COL_NAME);
                BasicDBObject targetUser = (BasicDBObject) userCol.findOne(query);
                if (targetUser != null) {
                    MainPageController.this.targetUserId = targetUser.getInt(Constants.USER.ID);
                    BasicDBList rawBlockList = (BasicDBList) targetUser.get(Constants.USER.BLOCKED_LIST);
                    List<Integer> blockList = new ArrayList<Integer>();
                    if (rawBlockList != null) {
                        for (int i = 0; i < rawBlockList.size(); i++) {
                            blockList.add((Integer) rawBlockList.get(i));
                        }
                    }
                    if (checkForBlock(owner.blockedList, blockList, owner.id, targetUserId)) {
                        sendBtn.setVisible(false);
                        return;
                    } else {
                        sendBtn.setVisible(true);
                    }
                    if (!isPrivateChat) {
                        startNormalChat(targetUserId);
                    } else {
                        startPrivateChat(targetUserId);
                    }
                } else {
                    query = new BasicDBObject();
                    query.append(Constants.CHAT.CHAT_NAME, newValue.replace("@", ""));
                    DBCollection chatCol = rootDB.getCollection(Constants.CHAT_COL);
                    BasicDBObject targetChat = (BasicDBObject) chatCol.findOne(query);
                    if (owner.mentionedGroupNameList.contains(newValue.replace("@", ""))) {
                        owner.mentionedGroupNameList.remove(newValue.replace("@", ""));
                        BasicDBList rawMentionList = (BasicDBList) targetChat.get(Constants.CHAT.MENTIONED_MEMBERS);
                        List<Integer> mentionList = new ArrayList<>();
                        for (int i = rawMentionList.size() - 1; i >= 0; i--) {
                            mentionList.add((Integer) rawMentionList.get(i));
                            if (mentionList.get(mentionList.size() - 1) == owner.id) {
                                rawMentionList.remove(i);
                            }
                        }
                        targetChat.put(Constants.CHAT.MENTIONED_MEMBERS, rawMentionList);
                        chatCol.update(query, targetChat);
                        int index = groupsAndFriendItems.indexOf(newValue);
                        groupsAndFriendItems.set(index, groupsAndFriendItems.get(index).replace("@", ""));
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                System.out.println("Changing data");
                                if (friendsListProperty != null) {
                                    friendsListProperty.set(FXCollections.observableArrayList(groupsAndFriendItems));
                                }
                            }
                        });
                    }
                    if (targetChat != null) {
                        startGroupChat(targetChat);
                    }
                }
            }
        }
    };

//    private class ColorRectCell extends ListCell<String> {
//        @Override
//        public void updateItem(String item, boolean empty) {
//            super.updateItem(item, empty);
//            if (owner.mentionedGroupNameList.contains(item)) {
//                Label label = new Label();
//                if (item != null) {
//                    label.setMaxSize(10.0,10.0);
//                    label.getGraphic().setStyle("-fx-background-color: #FF1744; ");
//                    label.getGraphic().setStyle("-fx-text-fill: #FFF;");
//                    label.setText(item);
//                    setGraphic(label);
//                    setText(item);
//                } else {
//                    setGraphic(null);
//                }
//            } else {
//                Label label = new Label();
//                label.setMaxSize(10.0, 10.0);
//                label.setStyle("-fx-background-color: #FF1744; ");
//                label.setStyle("-fx-text-fill: #FFF;");
//                label.setText(item);
//                setGraphic(label);
//                setText(item);
//            }
//        }
//    }


    private boolean checkForBlock(List<Integer> blockList1, List<Integer> blockList2, int id1, int id2) {
        for (int i = 0; i < blockList1.size(); i++) {
            if (blockList1.get(i) == id2 || blockList1.get(i) == id1) {
                return true;
            }
        }
        for (int i = 0; i < blockList2.size(); i++) {
            if (blockList2.get(i) == id2 || blockList2.get(i) == id1) {
                return true;
            }
        }
        return false;
    }

    private void startPrivateChat(int targetUserID) {
        isGroupChat = false;
        DBCollection chatCol = rootDB.getCollection(Constants.CHAT_COL);
        BasicDBObject query = new BasicDBObject();
        BasicDBList subQuery = new BasicDBList();
        List<Integer> participantUsers = new ArrayList<>();
        participantUsers.add(targetUserID);
        participantUsers.add(owner.id);
        participantUsers.sort(new Comparator<Integer>() {
            @Override
            public int compare(Integer a, Integer b) {
                if (a > b) {
                    return 1;
                } else if (a == b) {
                    return 0;
                } else {
                    return -1;
                }
            }
        });
        for (int i = 0; i < participantUsers.size(); i++) {
            subQuery.add(participantUsers.get(i));
        }
        query.put(Constants.CHAT.MEMBERS_LIST_NAME, subQuery);
        System.out.println(query);
        BasicDBObject chatObj = (BasicDBObject) chatCol.findOne(query);
        if (chatObj == null) {
            chatObj = new BasicDBObject();
            chatObj.put(Constants.CHAT.MEMBERS_LIST_NAME, subQuery);
            chatObj.put(Constants.CHAT.CHAT_ID, ObjectId.get().toStringMongod());
            chatObj.put(Constants.CHAT.CHAT_NAME, "");
            chatObj.put(Constants.CHAT.IS_CHANEL, false);
            chatObj.put(Constants.CHAT.ADMIN, new BasicDBList());
            chatCol.insert(chatObj);
        }
        currentChat = new Chat();
        currentChat.chatId = chatObj.getString(Constants.CHAT.CHAT_ID);
        query = new BasicDBObject();
        query.append(Constants.PRIVATE_TALK.PARENT_CHAT_ID, currentChat.chatId);
        BasicDBObject sortQuery = new BasicDBObject();
        sortQuery.append(Constants.PRIVATE_TALK.DATE, -1);
        cancelAndStartNewTalkRefresher(query, sortQuery);
    }

    private void startNormalChat(int targetUserID) {
        isGroupChat = false;
        DBCollection chatCol = rootDB.getCollection(Constants.CHAT_COL);
        BasicDBObject query = new BasicDBObject();
        BasicDBList subQuery = new BasicDBList();
        List<Integer> participantUsers = new ArrayList<>();
        participantUsers.add(targetUserID);
        participantUsers.add(owner.id);
        participantUsers.sort(new Comparator<Integer>() {
            @Override
            public int compare(Integer a, Integer b) {
                if (a > b) {
                    return 1;
                } else if (a == b) {
                    return 0;
                } else {
                    return -1;
                }
            }
        });
        for (int i = 0; i < participantUsers.size(); i++) {
            subQuery.add(participantUsers.get(i));
        }
        query.put(Constants.CHAT.MEMBERS_LIST_NAME, subQuery);
        System.out.println(query);
        BasicDBObject chatObj = (BasicDBObject) chatCol.findOne(query);
        if (chatObj == null) {
            chatObj = new BasicDBObject();
            chatObj.put(Constants.CHAT.MEMBERS_LIST_NAME, subQuery);
            chatObj.put(Constants.CHAT.CHAT_ID, ObjectId.get().toStringMongod());
            chatObj.put(Constants.CHAT.CHAT_NAME, "");
            chatObj.put(Constants.CHAT.IS_CHANEL, false);
            chatObj.put(Constants.CHAT.ADMIN, new BasicDBList());
            chatCol.insert(chatObj);
        }
        currentChat = new Chat();
        currentChat.chatId = chatObj.getString(Constants.CHAT.CHAT_ID);

        talkCol = rootDB.getCollection(Constants.TALK_NAME);

        List<Talk> userTalks = new ArrayList<>();
        query = new BasicDBObject();
        query.append(Constants.TALK.PARENT_CHAT_ID, currentChat.chatId);
        BasicDBObject sortQuery = new BasicDBObject();
        sortQuery.append(Constants.TALK.DATE, -1);
        DBCursor c = talkCol.find(query).limit(Constants.LOAD_MESSAGE_MAX).sort(sortQuery);
        while (c.hasNext()) {
            BasicDBObject rawTalk = (BasicDBObject) c.next();
            Talk talk = Constants.generateTalkObj(rawTalk);
            userTalks.add(talk);
        }
        userTalks.sort(new Comparator<Talk>() {
            @Override
            public int compare(Talk talk1, Talk talk2) {
                return talk1.date.compareTo(talk2.date);
            }
        });
        clearChatField();
        for (int i = 0; i < userTalks.size(); i++) {
            showTextToUser(userTalks.get(i));
        }
        currentChat.talkList = userTalks;
        cancelAndStartNewTalkRefresher(query, sortQuery);
    }

    private void startGroupChat(BasicDBObject rawChat) {
        isPrivateChat = false;
        privateChatMod.setVisible(false);
        isGroupChat = true;
        currentChat = new Chat();
        currentChat.chatId = rawChat.getString(Constants.CHAT.CHAT_ID);
        currentChat.isChanel = rawChat.getBoolean(Constants.CHAT.IS_CHANEL);
        currentChat.adminId = rawChat.getInt(Constants.CHAT.ADMIN);
        if (currentChat.adminId != owner.id && currentChat.isChanel) {
            sendBtn.setVisible(false);
        } else {
            sendBtn.setVisible(true);
        }
        talkCol = rootDB.getCollection(Constants.TALK_NAME);
        List<Talk> userTalks = new ArrayList<>();
        BasicDBObject query = new BasicDBObject();
        query.append(Constants.TALK.PARENT_CHAT_ID, currentChat.chatId);
        BasicDBObject sortQuery = new BasicDBObject();
        sortQuery.append(Constants.TALK.DATE, -1);
        DBCursor c = talkCol.find(query).limit(Constants.LOAD_MESSAGE_MAX).sort(sortQuery);
        while (c.hasNext()) {
            BasicDBObject rawTalk = (BasicDBObject) c.next();
            Talk talk = Constants.generateTalkObj(rawTalk);
            userTalks.add(talk);
        }
        userTalks.sort(new Comparator<Talk>() {
            @Override
            public int compare(Talk talk1, Talk talk2) {
                return talk1.date.compareTo(talk2.date);
            }
        });
        clearChatField();
        for (int i = 0; i < userTalks.size(); i++) {
            showTextToUser(userTalks.get(i));
        }
        currentChat.talkList = userTalks;
        cancelAndStartNewTalkRefresher(query, sortQuery);
    }

    private void cancelAndStartNewTalkRefresher(BasicDBObject query, BasicDBObject sortQuery) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                endOfProgram = true;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                endOfProgram = false;
                if (isPrivateChat) {
                    startPrivateTalkRefresher(query, sortQuery);
                } else {
                    startTalkRefresher(query, sortQuery);
                }
            }
        });
        t.start();
    }

    private void startPrivateTalkRefresher(BasicDBObject query, BasicDBObject sortQuery) {
        Thread t = new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        while (!endOfProgram) {
                            BasicDBObject subQuery = new BasicDBObject();
                            subQuery.append("$lt", new Date(System.currentTimeMillis()));
                            BasicDBObject removeQuery = new BasicDBObject();
                            removeQuery.append(Constants.PRIVATE_TALK.EXPIRE_AT, subQuery);
                            talkCol = rootDB.getCollection(Constants.PRIVATE_TALK_NAME);
                            talkCol.remove(removeQuery);
                            DBCursor c = talkCol.find(query).sort(sortQuery).limit(Constants.LOAD_MESSAGE_MAX);
                            List<PrivateTalk> newTalks = new ArrayList<>();
                            while (c.hasNext()) {
                                BasicDBObject rawTalk = (BasicDBObject) c.next();
                                PrivateTalk talk = Constants.generatePrivateTalkObj(rawTalk);
                                newTalks.add(talk);
                            }
                            newTalks.sort(new Comparator<PrivateTalk>() {
                                @Override
                                public int compare(PrivateTalk talk1, PrivateTalk talk2) {
                                    return talk1.date.compareTo(talk2.date);
                                }
                            });
                            clearChatField();
                            for (int i = 0; i < newTalks.size(); i++) {
//                                if (!talkContain(currentChat.talkList, newTalks.get(i))) {
//                                currentChat.talkList.add(newTalks.get(i));
                                showTextToUser(newTalks.get(i));
//                            }
                            }
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

        );
        t.start();
    }

    private void cancelRefresher() {
        endOfProgram = true;
    }

    private void startTalkRefresher(BasicDBObject query, BasicDBObject sortQuery) {
        Thread t = new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        while (!endOfProgram) {
                            if (currentChat.talkList.size() > 0) {//If data exist o.w load all
                                BasicDBObject subQuery = new BasicDBObject();
                                subQuery.append("$gt", currentChat.talkList.get(currentChat.talkList.size() - 1).date);
                                query.append(Constants.TALK.DATE, subQuery);
                            }
                            talkCol = rootDB.getCollection(Constants.TALK_NAME);
                            DBCursor c = talkCol.find(query).sort(sortQuery).limit(Constants.LOAD_MESSAGE_MAX);
                            System.out.println("Talk query " + query);
                            List<Talk> newTalks = new ArrayList<>();
                            while (c.hasNext()) {
                                BasicDBObject rawTalk = (BasicDBObject) c.next();
                                System.out.println("Talk query " + rawTalk);
                                Talk talk = Constants.generateTalkObj(rawTalk);
                                if (!currentChat.talkList.contains(talk)) {
                                    newTalks.add(talk);
                                }
                            }
                            newTalks.sort(new Comparator<Talk>() {
                                @Override
                                public int compare(Talk talk1, Talk talk2) {
                                    return talk1.date.compareTo(talk2.date);
                                }
                            });

                            for (int i = 0; i < newTalks.size(); i++) {
                                if (!talkContain(currentChat.talkList, newTalks.get(i))) {
                                    currentChat.talkList.add(newTalks.get(i));
                                    showTextToUser(newTalks.get(i));
                                }
                            }
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
        );
        t.start();
    }

    private boolean talkContain(List<Talk> talkList, Talk newTalk) {
        for (int i = 0; i < talkList.size(); i++) {
            if (talkList.get(i).date.equals(newTalk.date)) {
                return true;
            }
        }
        return false;
    }

    private synchronized void clearChatField() {
        chatField.clear();
    }

    private void showTextToUser(Talk talk) {
        String text = "";
        if (talk.name.equals(owner.name)) {
            text += "Me";
        } else {
            text += talk.name;
        }
        text += " : ";
        text += talk.text;
        text += "\n" + talk.date.getDay() + ":" + talk.date.getHours() + ":" + talk.date.getMinutes() + "\n";
        chatField.appendText(text);
    }

    private void showTextToUser(PrivateTalk talk) {
        String text = "";
        if (talk.name.equals(owner.name)) {
            text += "Me";
        } else {
            text += talk.name;
        }
        text += " : ";
        text += talk.text;
        text += "\n" + talk.date.getDay() + ":" + talk.date.getHours() + ":" + talk.date.getMinutes();
        text += "\n" + talk.expireData.getDay() + ":" + talk.expireData.getHours() + ":" + talk.expireData.getMinutes() + "\n";
        chatField.appendText(text);
    }

    private void send(String message) {
        if (isGroupChat) {
            Talk talk = new Talk();
            talk.id = ObjectId.get().toString();
            talk.date = new Date(System.currentTimeMillis());
            talk.text = message;
            talk.name = owner.name;
            currentChat.talkList.add(talk);
            BasicDBObject rawTalk = Constants.generateTalkBasicDBObj(talk, currentChat.chatId);
            talkCol = rootDB.getCollection(Constants.TALK_NAME);
            talkCol.insert(rawTalk);
            if (message.contains("@")) {
                String[] parts = message.split(" ");
                for (int i = 0; i < parts.length; i++) {
                    if (parts[i].trim().contains("@")) {
                        String pureName = parts[i].replace("@", "").trim();
                        for (int k = 0; k < allUser.size(); k++) {
                            if (allUser.get(k).name.equals(pureName)) {
                                DBCollection chatCol = rootDB.getCollection(Constants.CHAT_COL);
                                BasicDBObject query = new BasicDBObject();
                                query.append(Constants.CHAT.CHAT_ID, currentChat.chatId);
                                BasicDBObject rawChat = (BasicDBObject) chatCol.findOne(query);
                                BasicDBList rawMentioned = (BasicDBList) rawChat.get(Constants.CHAT.MENTIONED_MEMBERS);
                                if (rawMentioned == null) {
                                    rawMentioned = new BasicDBList();
                                }
                                rawMentioned.add(allUser.get(k).id);
                                rawChat.put(Constants.CHAT.MENTIONED_MEMBERS, rawMentioned);
                                chatCol.update(query, rawChat);
                                break;
                            }
                        }
                        break;
                    }
                }
            }
            showTextToUser(talk);
        } else if (isPrivateChat) {
            PrivateTalk talk = new PrivateTalk();
            talk.id = ObjectId.get().toString();
            talk.date = new Date(System.currentTimeMillis());
            talk.text = message;
            talk.name = owner.name;
            talk.expireData = new Date(System.currentTimeMillis() + Constants.PRIVATE_CHAT_EXPIRE_TIME);
            BasicDBObject rawTalk = Constants.generatePrivateTalkBasicDBObj(talk, currentChat.chatId);
            talkCol = rootDB.getCollection(Constants.PRIVATE_TALK_NAME);
            talkCol.insert(rawTalk);
            showTextToUser(talk);
        } else {
            Talk talk = new Talk();
            talk.id = ObjectId.get().toString();
            talk.date = new Date(System.currentTimeMillis());
            talk.text = message;
            talk.name = owner.name;
            currentChat.talkList.add(talk);
            BasicDBObject rawTalk = Constants.generateTalkBasicDBObj(talk, currentChat.chatId);
            talkCol = rootDB.getCollection(Constants.TALK_NAME);
            talkCol.insert(rawTalk);
            showTextToUser(talk);
        }
    }


}
