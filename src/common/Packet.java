package common;

import java.io.UnsupportedEncodingException;

public class Packet
{
    public final static int AUTH       = 0x01;
    public final static int MESSAGE    = 0x02;
    public final static int QUERY_USER = 0x10;
    public final static int QUERY_REC  = 0x11;
    public final static int POST       = 0x20;
    public final static int DELETE     = 0x21;
    
    public final static int SUCCESS    = 0xF0;
    public final static int ERROR      = 0xF1;
    
    private int type;
    private int code;
    private String data;
    
    public Packet(int type, int code, String data)
    {
        this.type = type;
        this.code = code;
        this.data = data;
    }
    
    public int getType() {
        return this.type;
    }
    
    public int getCode() {
        return this.code;
    }
    
    public String getString() {
        return this.data;
    }
    
    public byte[] getBytes() {
        try {
            return this.data.getBytes("UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            /* Not gonna happen... */
            e.printStackTrace();
        }
        return new byte[0];
    }
}