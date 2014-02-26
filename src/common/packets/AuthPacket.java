package common.packets;

public class AuthPacket
{
    private int id;
    private String password;

    public AuthPacket(int id, String password)
    {
        this.id = id;
        this.password = password;
    }
    
    public int getId() {
        return this.id;
    }
    
    public String getPassword() {
        return this.password;
    }
}
