package telegram.controllers;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import telegram.Constants;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by M.Fakhreddin on 6/24/2016.
 */
public class LoginPageController implements Initializable {
    @FXML
    TextField nameInputField;
    @FXML
    TextField passwordInputFiled;
    @FXML
    Button signupBtn;
    @FXML
    Button loginBtn;
    private DB rootDB;
    private DBCollection userCol;
    private Stage stage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.rootDB = Constants.getRootDB();
        this.userCol = rootDB.getCollection(Constants.USER_COL_NAME);

    }

    public void OnClick(ActionEvent event) {
        if (event.getSource().equals(loginBtn)) {
            String username = nameInputField.getText().trim() + "";
            String password = passwordInputFiled.getText().trim() + "";
            if (!username.equals("")) {
                BasicDBObject query = new BasicDBObject();
                query.append(Constants.USER.NAME, username);
                query.append(Constants.USER.PASSWORD, password);
                DBCursor c = userCol.find(query);
                if (c.count() != 0) {
                    BasicDBObject curUserObj = (BasicDBObject) c.next();
                    System.out.print(curUserObj);
                    int userID = (int) curUserObj.get(Constants.USER.ID);
                    System.out.println("User exist login successful");
                    try {
                        startApp(userID);
                    } catch (IOException e) {
                        System.err.println("Starting the app failed");
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("User not found login failed");
                }
            }
        } else if (event.getSource().equals(signupBtn)) {
            String username = nameInputField.getText().trim() + "";
            String password = passwordInputFiled.getText().trim() + "";
            if (!username.equals("")) {
                BasicDBObject query = new BasicDBObject();
                query.append(Constants.USER.NAME, username);
                DBCursor c = userCol.find(query);
                if (c.count() == 0) {
                    query = new BasicDBObject();
                    query.append(Constants.USER.NAME, username);
                    query.append(Constants.USER.PASSWORD, password);
                    DBCursor cC = userCol.find();
                    int userId = cC.size() + 1;
                    query.append(Constants.USER.ID, userId);
                    query.append(Constants.USER.IS_REPORTED, false);
                    query.append(Constants.USER.REPORT_COUNT, 0);
                    userCol.insert(query);
                    System.out.println("User not exist signUp successful");
                    try {
                        startApp(userId);
                    } catch (IOException e) {
                        System.err.println("Starting the app failed");
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("User exist signup failed");
                }
            }

        }
    }

    private void startApp(int userID) throws IOException {
        Stage mainPage = new Stage();
//        mainPage.initStyle(StageStyle.UNDECORATED);
        mainPage.initModality(Modality.APPLICATION_MODAL);
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("../views/MainPageView.fxml"));
        Parent content = loader.load();
        mainPage.setScene(new Scene(content));
        mainPage.setResizable(false);
        mainPage.show();
        MainPageController controller = loader.getController();
        controller.setCurrUser(userID);
        controller.setStage(mainPage);
        stage.close();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
