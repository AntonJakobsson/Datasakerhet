package common;

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
    
    public static Packet queryUsers(int type)
    {
        return new Packet(Packet.QUERY_USER, type, "");
    }
    
    public static Packet queryRecords(User user)
    {
    	return new Packet(Packet.QUERY_REC, user.getId(), "");
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
