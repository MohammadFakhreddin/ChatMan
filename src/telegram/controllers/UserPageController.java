package telegram.controllers;

import com.mongodb.DB;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import telegram.Constants;
import telegram.models.User;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by M.Fakhreddin on 6/27/2016.
 */
public class UserPageController implements Initializable {
    @FXML
    private TextField userNameField;
    @FXML
    private TextField nameField;
    @FXML
    private TextField phoneNumberField;
    @FXML
    private TextArea moreInfoField;
    @FXML
    private Button createChanelBtn;
    @FXML
    private Button createGroupBtn;
    @FXML
    private Button reportBtn;
    @FXML
    private Button friendBtn;
    @FXML
    private Button unFriendBtn;
    @FXML
    private Button blockBtn;
    @FXML
    private Button unBlockBtn;
    @FXML
    private Button acceptChangesBtn;
    private boolean isOwnUser;
    private Stage stage;
    private User targetUser;
    private User ownUser;
    private boolean isFriend;
    private boolean isBlocked;
    private DB rootDB;
    private List<User> allUsers;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.rootDB = Constants.getRootDB();
    }

    public void start(Stage stage, User ownUser, List<User> allUsers) {
        this.stage = stage;
        this.isOwnUser = true;
        this.ownUser = ownUser;
        this.targetUser = ownUser;
        this.allUsers = allUsers;
        reportBtn.setVisible(false);
        friendBtn.setVisible(false);
        blockBtn.setVisible(false);
        unFriendBtn.setVisible(false);
        unBlockBtn.setVisible(false);
        createChanelBtn.setVisible(true);
        createGroupBtn.setVisible(true);
        userNameField.setEditable(true);
        nameField.setEditable(true);
        phoneNumberField.setEditable(true);
        moreInfoField.setEditable(true);
        userNameField.setText(targetUser.name + "");
        nameField.setText(targetUser.realName + "");
        phoneNumberField.setText(targetUser.phoneNumber + "");
        moreInfoField.setText(targetUser.moreInfo + "");

        updateFields(ownUser);
    }

    public void start(Stage stage, User targetUser, User ownUser, boolean isFriend, boolean isBlocked, List<User> allUsers) {
        this.stage = stage;
        this.isOwnUser = false;
        this.targetUser = targetUser;
        this.ownUser = ownUser;
        this.isFriend = isFriend;
        this.isBlocked = isBlocked;
        this.allUsers = allUsers;
        reportBtn.setVisible(true);
        friendBtn.setVisible(true);
        if (isFriend) {
            friendBtn.setVisible(false);
            unFriendBtn.setVisible(true);
        } else {
            friendBtn.setVisible(true);
            unFriendBtn.setVisible(false);
        }
        if (isBlocked) {
            blockBtn.setVisible(false);
            unBlockBtn.setVisible(true);
        } else {
            blockBtn.setVisible(true);
            unBlockBtn.setVisible(false);
        }
        createChanelBtn.setVisible(false);
        createGroupBtn.setVisible(false);
        acceptChangesBtn.setVisible(false);

        userNameField.setEditable(false);
        nameField.setEditable(false);
        phoneNumberField.setEditable(false);
        moreInfoField.setEditable(false);

        updateFields(targetUser);
    }

    private void updateFields(User targetUser) {
        if (targetUser.name == null) {
            targetUser.name = "";
        }
        if (targetUser.realName == null) {
            targetUser.realName = "";
        }
        if (targetUser.phoneNumber == null) {
            targetUser.phoneNumber = "";
        }
        if (targetUser.moreInfo == null) {
            targetUser.moreInfo = "";
        }
        userNameField.setText(targetUser.name + "");
        nameField.setText(targetUser.realName + "");
        phoneNumberField.setText(targetUser.phoneNumber + "");
        moreInfoField.setText(targetUser.moreInfo + "");
    }

    public void OnAction(ActionEvent event) {
        if (event.getSource() == acceptChangesBtn) {
            if (!userNameField.getText().trim().equals("")) {
                targetUser.name = userNameField.getText().trim();
            }
            if (!nameField.getText().trim().equals("")) {
                targetUser.realName = nameField.getText().trim();
            }
            if (!phoneNumberField.getText().trim().equals("")) {
                targetUser.phoneNumber = phoneNumberField.getText().trim();
            }
            if (!moreInfoField.getText().trim().equals("")) {
                targetUser.moreInfo = moreInfoField.getText().trim();
            }
            Constants.updateUser(targetUser);
            stage.close();
        } else if (event.getSource() == friendBtn) {
            isFriend = true;
            ownUser.friendList.add(targetUser.id);
            unFriendBtn.setVisible(true);
            friendBtn.setVisible(false);
            Constants.updateUser(ownUser);
        } else if (event.getSource() == unFriendBtn) {
            for (int i = 0; i < ownUser.friendList.size(); i++) {
                if (ownUser.friendList.get(i) == targetUser.id) {
                    ownUser.friendList.remove(i);
                }
            }
            isFriend = false;
            unFriendBtn.setVisible(false);
            friendBtn.setVisible(true);
            Constants.updateUser(ownUser);
        } else if (event.getSource() == blockBtn) {
            ownUser.blockedList.add(targetUser.id);
            isBlocked = true;
            unBlockBtn.setVisible(true);
            blockBtn.setVisible(false);
            Constants.updateUser(ownUser);
        } else if (event.getSource() == unBlockBtn) {
            for (int i = 0; i < ownUser.blockedList.size(); i++) {
                if (ownUser.blockedList.get(i) == targetUser.id) {
                    ownUser.blockedList.remove(i);
                }
            }
            isBlocked = false;
            unBlockBtn.setVisible(false);
            blockBtn.setVisible(true);
            Constants.updateUser(ownUser);
        } else if (event.getSource() == createGroupBtn) {
            Stage chanelPage = new Stage();
//        mainPage.initStyle(StageStyle.UNDECORATED);
            chanelPage.initModality(Modality.WINDOW_MODAL);
            chanelPage.initOwner(((Node) event.getSource()).getScene().getWindow());
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("../views/CreateGroupChanelPage.fxml"));
            Parent content = null;
            try {
                content = loader.load();
                chanelPage.setScene(new Scene(content));
                chanelPage.setResizable(false);
                chanelPage.show();
                CreateGroupChanelPageController controller = loader.getController();
                controller.start(chanelPage, false, ownUser, allUsers);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (event.getSource() == createChanelBtn) {
            Stage chanelPage = new Stage();
//        mainPage.initStyle(StageStyle.UNDECORATED);
            chanelPage.initModality(Modality.WINDOW_MODAL);
            chanelPage.initOwner(((Node) event.getSource()).getScene().getWindow());
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("../views/CreateGroupChanelPage.fxml"));
            Parent content = null;
            try {
                content = loader.load();
                chanelPage.setScene(new Scene(content));
                chanelPage.setResizable(false);
                chanelPage.show();
                CreateGroupChanelPageController controller = loader.getController();
                controller.start(chanelPage, true, ownUser, allUsers);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (event.getSource() == reportBtn) {
            System.out.println("Reporting " + targetUser.name);
            targetUser.totalReportCount++;
            if (targetUser.totalReportCount >= Constants.ACCOUNT_BLOCKADE_REPORT_NEED) {
                targetUser.isReported = true;
                targetUser.reportExpireTime = new Date(System.currentTimeMillis() + Constants.ACCOUNT_BLOCKADE_TIME);
                targetUser.totalReportCount = 0;
            }
            Constants.updateUser(targetUser);
        }
    }


}
