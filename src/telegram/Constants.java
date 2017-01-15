package telegram;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import telegram.models.Chat;
import telegram.models.PrivateTalk;
import telegram.models.Talk;
import telegram.models.User;

import java.net.UnknownHostException;

/**
 * Created by M.Fakhreddin on 6/24/2016.
 */
public class Constants {
    public static final int LOAD_MESSAGE_MAX = 3;
    public static final int PRIVATE_CHAT_EXPIRE_TIME = 30000;
    public static final int ACCOUNT_BLOCKADE_REPORT_NEED = 1;
    public static final int ACCOUNT_BLOCKADE_TIME = 30000;

    private static DB rootDB = null;
    private static MongoClient mongoClient = null;

    public static DB getRootDB() {
        if (mongoClient == null) {
            // To connect to mongodb server
            try {
                mongoClient = new MongoClient("localhost", 27017);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
        if (mongoClient != null) {
            rootDB = mongoClient.getDB(DB_NAME);
            return rootDB;
        }
        return null;
    }

    public static void updateUser(User targetUser) {
        DBCollection userCol = rootDB.getCollection(Constants.USER_COL_NAME);
        BasicDBObject rawUser = new BasicDBObject();
        rawUser.append(Constants.USER.ID, targetUser.id);
        rawUser.append(Constants.USER.NAME, targetUser.name);
        rawUser.append(Constants.USER.PASSWORD, targetUser.password);
        rawUser.append(Constants.USER.IS_REPORTED, targetUser.isReported);
        rawUser.append(Constants.USER.REPORT_COUNT, targetUser.totalReportCount);
        rawUser.append(Constants.USER.REPORT_EXPIRE_TIME, targetUser.reportExpireTime);
        rawUser.append(Constants.USER.PHONE_NUMBER, targetUser.phoneNumber);
        rawUser.append(Constants.USER.MORE_INFO, targetUser.moreInfo);
        rawUser.append(Constants.USER.NAME_AND_FAMILY_NAME, targetUser.realName);
//            BasicDBList rawFriendList = new BasicDBList();
//            for (int i = 0; i < user.friendList.size(); i++) {
//                rawFriendList.add(user.friendList.get(i));
//            }
        rawUser.append(Constants.USER.FRIEND_LIST, targetUser.friendList);
//            BasicDBList rawBlockList = new BasicDBList();
//            for (int i = 0; i < user.blockedList.size(); i++) {
//                rawFriendList.add(user.friendList.get(i));
//            }
        rawUser.append(Constants.USER.BLOCKED_LIST, targetUser.blockedList);
        rawUser.append(Constants.USER.GROUP_LIST, targetUser.groupAndChanelList);
        BasicDBObject query = new BasicDBObject();
        query.append(Constants.USER.ID, targetUser.id);
        userCol.update(query, rawUser);
    }

    public static Talk generateTalkObj(BasicDBObject rawTalk) {
        Talk talk = new Talk();
        talk.id = rawTalk.getString(Constants.TALK.ID);
        talk.date = rawTalk.getDate(Constants.TALK.DATE);
        talk.name = rawTalk.getString(Constants.TALK.NAME);
        talk.text = rawTalk.getString(Constants.TALK.TEXT);
        return talk;
    }

    public static BasicDBObject generateTalkBasicDBObj(Talk talk, String parentChatId) {
        BasicDBObject rawTalk = new BasicDBObject();
        rawTalk.append(Constants.TALK.ID, talk.id);
        rawTalk.append(Constants.TALK.PARENT_CHAT_ID, parentChatId);
        rawTalk.append(Constants.TALK.DATE, talk.date);
        rawTalk.append(Constants.TALK.NAME, talk.name);
        rawTalk.append(Constants.TALK.TEXT, talk.text);
        return rawTalk;
    }

    public static PrivateTalk generatePrivateTalkObj(BasicDBObject rawTalk) {
        PrivateTalk talk = new PrivateTalk();
        talk.id = rawTalk.getString(Constants.PRIVATE_TALK.ID);
        talk.date = rawTalk.getDate(Constants.PRIVATE_TALK.DATE);
        talk.name = rawTalk.getString(Constants.PRIVATE_TALK.NAME);
        talk.text = rawTalk.getString(Constants.PRIVATE_TALK.TEXT);
        talk.expireData = rawTalk.getDate(Constants.PRIVATE_TALK.EXPIRE_AT);
        return talk;
    }

    public static BasicDBObject generatePrivateTalkBasicDBObj(PrivateTalk talk, String parentChatId) {
        BasicDBObject rawTalk = new BasicDBObject();
        rawTalk.append(Constants.PRIVATE_TALK.ID, talk.id);
        rawTalk.append(Constants.PRIVATE_TALK.PARENT_CHAT_ID, parentChatId);
        rawTalk.append(Constants.PRIVATE_TALK.DATE, talk.date);
        rawTalk.append(Constants.PRIVATE_TALK.NAME, talk.name);
        rawTalk.append(Constants.PRIVATE_TALK.TEXT, talk.text);
        rawTalk.append(Constants.PRIVATE_TALK.EXPIRE_AT, talk.expireData);
        return rawTalk;
    }

    public static BasicDBObject generateRawChat(Chat chat) {
        BasicDBObject dbObject = new BasicDBObject();
        dbObject.append(CHAT.CHAT_ID, chat.chatId);
        dbObject.append(CHAT.CHAT_NAME, chat.chatName);
        dbObject.append(CHAT.ADMIN, chat.adminId);
        dbObject.append(CHAT.IS_CHANEL, chat.isChanel);
        dbObject.append(CHAT.MEMBERS_LIST_NAME, chat.members);
        return dbObject;
    }

    public final static String USER_NAME = "admin";
    public final static String PASSWORD = "1234";


    public static final String DB_NAME = "chat_man";

    public static String USER_COL_NAME = "user_col";

    public static class USER {
        public static final String ID = "_id";
        public static final String NAME = "name";
        public static final String PASSWORD = "password";
        public static final String PHONE_NUMBER = "phone_number";
        public static final String MORE_INFO = "more_info";
        public static final String NAME_AND_FAMILY_NAME = "name_family_name";
        public static final String IS_REPORTED = "is_reported";
        public static final String REPORT_EXPIRE_TIME = "report_start_time";
        public static final String REPORT_COUNT = "report_count";
        public static final String FRIEND_LIST = "friend_list";
        public static final String GROUP_LIST = "group_list";
        public static final String BLOCKED_LIST = "blocked_list";

    }

    public static final String CHAT_COL = "chat_col";

    public static class CHAT {
        public static final String CHAT_ID = "chat_id";
        public static final String CHAT_NAME = "chat_name";
        public static final String MEMBERS_LIST_NAME = "members";//It is a list
        public static final String REMOVED_MEMBERS = "removed_members";
        public static final String MENTIONED_MEMBERS = "mentioned_members";
        public static final String ADMIN = "admin";
        public static final String IS_CHANEL = "is_chanel";

    }

    public static final String TALK_NAME = "talk";

    public static class TALK {
        public static final String ID = "id";
        public static final String PARENT_CHAT_ID = "parent_id";
        public static final String NAME = "name";
        public static final String TEXT = "text";
        public static final String DATE = "date";
    }

    public static final String PRIVATE_TALK_NAME = "private_talk_name";

    public static class PRIVATE_TALK {
        public static final String ID = "id";
        public static final String PARENT_CHAT_ID = "parent_id";
        public static final String NAME = "name";
        public static final String TEXT = "text";
        public static final String DATE = "date";
        public static final String EXPIRE_AT = "expireAt";//It is reserved do not change this
    }
}
