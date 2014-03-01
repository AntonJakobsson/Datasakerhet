package common.packets;


public class AuthPacket
{
	public final static int ACCEPT = 0x50;
    public final static int DECLINE = 0x51;
    public final static int ERROR = 0x60;
	
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
