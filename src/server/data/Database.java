package server.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database
{
    private String path;
    private Connection connection;
    private UserReplicator userReplicator;
    
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
        }
        catch (SQLException e) {
            System.out.println("Unable to connect to database:");
            e.printStackTrace();
            System.exit(-1);
        }
    }
    
    public void close()
    {
        try {
            this.connection.close();
        }
        catch (SQLException e) { }
    }
    
    public UserReplicator users() {
        return this.userReplicator;
    }
}
