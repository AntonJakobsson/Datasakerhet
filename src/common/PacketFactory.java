package common;

import com.google.gson.Gson;
import common.packets.AuthPacket;

public class PacketFactory
{
    public static Packet auth(int id, String password) 
    {
        AuthPacket ap = new AuthPacket(id, password);
        return new Packet(Packet.AUTH, 0, gson().toJson(ap));
    }
    
    public static Packet message(int code, String message) 
    {
        return new Packet(Packet.MESSAGE, code, message);
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
