package server;

import java.net.InetAddress;
import java.util.HashMap;

public class IPBans
{
    protected HashMap<String, Long> bans;
    protected HashMap<String, Integer> attempts;
    
    public IPBans()
    {
        this.bans = new HashMap<String, Long>();
        this.attempts = new  HashMap<String, Integer>();
    }
    
    public void ban(InetAddress address, int minutes)
    {
        long expires = System.currentTimeMillis() + (long)minutes * 60000L;
        this.bans.put(address.getHostAddress(), expires);
    }
    
    public void attempt(InetAddress address)
    {
        Integer count = this.attempts.get(address.getHostAddress());
        int c = 0;
        if (count != null)
            c = count.intValue();
        c++;
        
        if (c >= 3) {
            this.attempts.put(address.getHostAddress(), new Integer(0));
            ban(address, 5);
            return;
        }
        
        this.attempts.put(address.getHostAddress(), new Integer(c));
    }
    
    public boolean isBanned(InetAddress address)
    {
        Long expires = this.bans.get(address.getHostAddress());
        if (expires == null)
            return false;
        
        if (System.currentTimeMillis() > expires)
        {
            this.bans.remove(address.getHostAddress());
            return false;
        }
        
        return true;
    }
}