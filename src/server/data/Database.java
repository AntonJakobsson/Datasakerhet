package server.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database
{
	public final static String FILE = "store.db";
	
    private boolean connected;
    private String path;
    private Connection connection;
    private UserReplicator userReplicator;
    private RecordReplicator recordReplicator;
    
    public Database(String databaseFile)
    {
        try {
            Class.forName("org.sqlite.JDBC");
        }
        catch (ClassNotFoundException e) {
            System.out.println("sqlite JDBC driver not found");
            System.exit(-1);
        }
        this.path = databaseFile;
    }
    
    public void connect()
    {
        try {
            this.connection = DriverManager.getConnection("jdbc:sqlite:" + path);
            this.connection.setAutoCommit(true);
            this.userReplicator = new UserReplicator(this.connection);
            this.recordReplicator = new  RecordReplicator(this.connection);
        }
        catch (SQLException e) {
            System.out.println("Unable to connect to database:");
            e.printStackTrace();
            System.exit(-1);
        }
        connected = true;
    }
    
    public void close()
    {
        try {
            this.connection.close();
        }
        catch (SQLException e) { }
        connected = false;
    }
    
    public UserReplicator users() 
    {
        if (!connected) throw new  RuntimeException("Not connected to database");
        return this.userReplicator;
    }
    
    public RecordReplicator records() 
    {
        if (!connected) throw new  RuntimeException("Not connected to database");
        return this.recordReplicator;
    }
}
