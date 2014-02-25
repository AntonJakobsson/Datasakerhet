package server.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserReplicator
{
    /* Prepared Statements */
    private PreparedStatement createUserTable;
    private PreparedStatement insertUser;
    private PreparedStatement findAll;
    private PreparedStatement findByDivision;

    public UserReplicator(Connection connection) throws SQLException
    {
        String tableQuery = "CREATE TABLE IF NOT EXISTS user (" + 
                            "id INT(32) NOT NULL auto_increment, " + 
                            "type INT(32), " + 
                            "name VARCHAR(255), " + 
                            "division VARCHAR(16), " + 
                            "password VARCHAR(64), " +
                            "salt VARCHAR(64), " +
                            "PRIMARY KEY (id));";
        this.createUserTable = connection.prepareStatement(tableQuery);
        
        String insertQuery = "INSERT INTO `user` (type, name, division, password, salt)" +
                             "VALUES (?, ?, ?, ?, ?);";
        this.insertUser = connection.prepareStatement(insertQuery);
        
        String findByDivisionQuery = "SELECT *  FROM `user` u WHERE u.id=? ORDER BY u.id ASC";
        this.findByDivision = connection.prepareStatement(findByDivisionQuery);
        
        String findAllQuery = "SELECT * FROM `user` ORDER BY id  ASC";
        this.findAll = connection.prepareStatement(findAllQuery);
    }

    public void createTable() throws SQLException
    {
        createUserTable.execute();
    }
    
    public void insert(int type, String name, String division, 
                       String password, String salt) throws SQLException
    {
        insertUser.setInt(1, type);
        insertUser.setString(2, name);
        insertUser.setString(3, division);
        insertUser.setString(4, password);
        insertUser.setString(5, salt);
        insertUser.executeUpdate();
    }
    
    public void findAll() throws SQLException
    {
        ResultSet results = findAll.executeQuery();
        while(results.next())
        {
            
        }
    }
}
