package common;

import java.util.ArrayList;

import com.google.gson.Gson;

public class PacketFactory
{
    public static Packet auth(String password) 
    {
        return new Packet(Packet.AUTH, 0, password);
    }
    
    public static Packet message(String message) 
    {
        return new Packet(Packet.MESSAGE, 0, message);
    }
    
    /* query users */
    
    public static Packet queryUsers(int type)
    {
        return new Packet(Packet.QUERY_USER, type, "");
    }
    
    public static Packet queryUsersReply(ArrayList<User> results)
    {
    	return new Packet(Packet.QUERY_USER, results.size(), gson().toJson(results));
    }
    
    /* query records */
    
    public static Packet queryRecords(User user)
    {
    	return new Packet(Packet.QUERY_REC, user.getId(), "");
    }
    
    public static Packet queryRecordsReply(ArrayList<Record> results)
    {
    	return new Packet(Packet.QUERY_REC, results.size(), gson().toJson(results));
    }
    
    /* Singleton GSON */
    private static Gson _gson;
    private static Gson gson() 
    {
        if (_gson == null)
            _gson = new Gson();
        return _gson;
    }
}
