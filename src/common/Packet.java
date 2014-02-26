package common;

import java.io.UnsupportedEncodingException;

public class Packet
{
    private int type;
    private int code;
    private byte[] data;
    
    public Packet(int type, int code, String data)
    {
        this.type = type;
        this.code = code;
        try {
            this.data = data.getBytes("UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            /* Not gonna happen... */
            e.printStackTrace();
        }
    }
    
    public int getType() {
        return this.type;
    }
    
    public int getCode() {
        return this.code;
    }
    
    public byte[] getBytes() {
        return this.data;
    }
}
