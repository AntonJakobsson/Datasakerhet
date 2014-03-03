package server;

import java.net.InetAddress;
import java.util.HashMap;

public class IPBans
{
    protected HashMap<String, Long> bans;
    
    public IPBans()
    {
        this.bans = new HashMap<String, Long>();
    }
    
    public void ban(InetAddress address, int minutes)
    {
        long expires = System.currentTimeMillis() + (long)minutes * 60000L;
        this.bans.put(address.getHostAddress(), expires);
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
