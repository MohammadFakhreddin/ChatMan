package telegram.models;

import java.util.Date;
import java.util.List;

/**
 * Created by M.Fakhreddin on 6/25/2016.
 */
public class User {
    public int id;
    public String name;
    public String realName;
    public String phoneNumber;
    public String password;
    public boolean isReported;
    public Date reportExpireTime;
    public int totalReportCount;
    public List<Integer> friendList;
    public List<String> groupAndChanelList;
    public List<Integer> blockedList;
    public String moreInfo;
    public List<String> mentionedGroupNameList;
}
