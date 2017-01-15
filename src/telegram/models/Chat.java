package telegram.models;

import java.util.List;

/**
 * Created by M.Fakhreddin on 6/25/2016.
 */
public class Chat {
    public String chatId;
    public String chatName;
    public int adminId;
    public boolean isChanel;
    public List<Integer> members;
    public List<Talk> talkList;
}
