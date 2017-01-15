package telegram.controllers;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import telegram.Constants;
import telegram.models.User;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by M.Fakhreddin on 6/29/2016.
 */
public class ManageGroupController implements Initializable {
    @FXML
    private ListView membersList;
    @FXML
    private ListView contactList;
    @FXML
    private Button leaveBtn;
    @FXML
    private Button manageBtn;
    @FXML
    private Label groupName;
    private boolean isAdmin;
    private int chatID;
    private User owner;
    private DB rootDB;
    private ListProperty<String> contactListProperty;
    private List<String> contactItems;
    private List<Integer> contactId;
    private ListProperty<String> membersListProperty;
    private List<Integer> memberId;
    private List<String> membersItem;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.rootDB = Constants.getRootDB();
        this.contactListProperty = new SimpleListProperty<>();
        this.contactItems = new ArrayList<>();
        this.contactId = new ArrayList<>();
        this.membersListProperty = new SimpleListProperty<>();
        this.memberId = new ArrayList<>();
        this.membersItem = new ArrayList<>();
    }

    public void OnAction(ActionEvent event) {
        if (event.getSource() == leaveBtn) {

        } else if (event.getSource() == manageBtn) {

        }
    }

    public void start(int chatID, User owner,User allUsers, boolean isAdmin) {
        this.isAdmin = isAdmin;
        this.chatID = chatID;
        this.owner = owner;
        DBCollection chatCol = rootDB.getCollection(Constants.CHAT_COL);
        BasicDBObject query = new BasicDBObject();
        query.append(Constants.CHAT.CHAT_ID, chatID);
        DBObject rawChat = chatCol.findOne(query);
//        membersList = rawChat
        membersList.getSelectionModel().selectedItemProperty();
        if (isAdmin) {
            membersList.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {

                }
            });
        } else {

        }
    }

}
