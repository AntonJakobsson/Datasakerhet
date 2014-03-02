package client;

import java.util.ArrayList;

import common.PacketFactory;
import common.Record;
import common.User;

public class NetworkState
{
    private Client client;
    private User user = new User.None();
    private ArrayList<User> queryUserList;
    private ArrayList<Record> queryRecordList;
    private Record record;
    
    private boolean error;
    private String err_message;
    
    public NetworkState(Client client) 
    {
        this.client = client;
    }
    
    public synchronized void error(String message)
    {
    	this.error = true;
    	this.err_message = message;
    	notifyAll();
    }
    
    public synchronized void setUser(User user) {
        this.user = user;
        notifyAll();
    }
    
    public synchronized User getUser() {
        return this.user;
    }
    
    public synchronized User auth(String password) 
    {
    	this.error = false;
        this.user = null;
        client.write(PacketFactory.auth(password));
        while(this.user == null && !error) { 
        	try { wait(); } 
        	catch(InterruptedException ex) { }
        }
        if (error) throw new AccessDeniedException(err_message);
        return this.user;
    }
    
    /* User Query */
    
    public synchronized ArrayList<User> queryUsers(int type)
    {
    	this.error = false;
        this.queryUserList = null;
        client.write(PacketFactory.queryUsers(type));
        while(this.queryUserList == null && !error) { 
        	try { wait(); } 
        	catch(InterruptedException ex) { }
        }
        if (error) throw new AccessDeniedException(err_message);
        return queryUserList;
    }
    
    public synchronized void setUserList(ArrayList<User> list)
    {
        this.queryUserList = list;
        notifyAll();
    }
    
    /* Record Query */
    
    public synchronized ArrayList<Record> queryRecords(User user)
    {
    	this.error = false;
    	this.queryRecordList = null;
    	client.write(PacketFactory.queryRecords(user));
    	while(queryRecordList == null && !error) { 
    		try { wait(); } 
    		catch(InterruptedException ex) { }
    	}
    	if (error) throw new AccessDeniedException(err_message);
    	return queryRecordList;
    }
    
    public synchronized void setRecordList(ArrayList<Record> list)
    {
    	this.queryRecordList = list;
    	notifyAll();
    }
    
    /* Post Record */
    
    public synchronized Record postRecord(Record record)
    {
    	this.error = false;
    	this.record = null;
    	client.write(PacketFactory.postRecord(record));
    	while(this.record == null && !error) {
    		try { wait(); } 
    		catch(InterruptedException ex) { }
    	}
    	if (error) throw new AccessDeniedException(err_message);
    	return this.record;
    }
    
    /* Delete Record */
    
    public synchronized void deleteRecord(Record record)
    {
    	this.error = false;
    	this.record = null;
    	client.write(PacketFactory.deleteRecord(record));
    	while(this.record == null && !error) {
    		try { wait(); } 
    		catch(InterruptedException ex) { }
    	}
    	if (error) throw new AccessDeniedException(err_message);
    }
    
    public synchronized void setRecord(Record record)
    {
    	this.record = record;
    	notifyAll();
    }
}
