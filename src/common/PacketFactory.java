package common;

import java.util.ArrayList;

import com.google.gson.Gson;

public class PacketFactory
{
	/* authentication */
	
    public static Packet auth(String password) 
    {
        return new Packet(Packet.AUTH, 0, password);
    }
    
    /* message */
    
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
    	return new Packet(Packet.QUERY_USER, Packet.SUCCESS, gson().toJson(results));
    }
    
    public static Packet queryUserDenied(String message)
    {
    	return new Packet(Packet.QUERY_USER, Packet.DENIED, message);
    }
    
    /* query records */
    
    public static Packet queryRecords(User user)
    {
    	return new Packet(Packet.QUERY_REC, user.getId(), "");
    }
    
    public static Packet queryRecordsReply(ArrayList<Record> results)
    {
    	return new Packet(Packet.QUERY_REC, Packet.SUCCESS, gson().toJson(results));
    }
    
    public static Packet queryRecordsDenied(String message)
    {
    	return new Packet(Packet.QUERY_REC, Packet.DENIED, message);
    }
    
    /* post record */
    
    public static Packet postRecord(Record record)
    {
    	return new Packet(Packet.POST, 0, gson().toJson(record));
    }
    
    /* delete record */
    
    public static Packet deleteRecord(Record record)
    {
    	return new Packet(Packet.DELETE, 0, gson().toJson(record));
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
