package client;

import java.util.ArrayList;

import common.PacketFactory;
import common.User;

public class NetworkState
{
    private Client client;
    private User user = new User.None();
    private ArrayList<User> queryUserList;
    
    public NetworkState(Client client) 
    {
        this.client = client;
    }
    
    public synchronized void setUser(User user) {
        this.user = user;
        notifyAll();
    }
    
    public synchronized User getUser() {
        return this.user;
    }
    
    public synchronized boolean auth(String password) throws InterruptedException 
    {
        this.user = null;
        client.write(PacketFactory.auth(password));
        while(this.user == null)
            wait();
        return user.getType() != 0;
    }
    
    public synchronized ArrayList<User> queryUsers(int type) throws InterruptedException
    {
        this.queryUserList = null;
        client.write(PacketFactory.queryUsers(type));
        while(this.queryUserList == null)
            wait();
        return queryUserList;
    }
    
    public synchronized void setQueryUsers(ArrayList<User> list)
    {
        this.queryUserList = list;
        notifyAll();
    }
}
