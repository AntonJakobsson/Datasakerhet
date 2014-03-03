package server;

import java.net.InetAddress;
import java.util.HashMap;

/**
 * Keeps track of banned IP addresses
 */
public class IPBans
{
    protected HashMap<String, Long> bans;
    protected HashMap<String, Integer> attempts;
    
    public IPBans()
    {
        this.bans = new HashMap<String, Long>();
        this.attempts = new  HashMap<String, Integer>();
    }
    
    /**
     * Bans an address from connecting to the server
     * @param address Remote address
     * @param minutes Ban duration, in minutes
     * @param reason Reason (for logging purposes)
     */
    public void ban(InetAddress address, int minutes, String reason)
    {
        long expires = System.currentTimeMillis() + (long)minutes * 60000L;
        this.bans.put(address.getHostAddress(), expires);
        Log.write(String.format("%s was banned for %d minutes. Reason: %s", address.getHostAddress(), minutes, reason));
    }
    
    /** 
     * Registers a login attempt from a given address. Returns true if the attempt is "ok", or false if the client should be disconnected 
     */
    public boolean attempt(InetAddress address)
    {
        Integer count = this.attempts.get(address.getHostAddress());
        int c = 0;
        if (count != null)
            c = count.intValue();
        c++;
        
        if (c >= 3) {
            this.attempts.put(address.getHostAddress(), new Integer(0));
            ban(address, 10, "3 failed login attempts");
            return false;
        }
        
        this.attempts.put(address.getHostAddress(), new Integer(c));
        return true;
    }
    
    /** 
     * Checks if a given address is currently banned from connecting to the server 
     */
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