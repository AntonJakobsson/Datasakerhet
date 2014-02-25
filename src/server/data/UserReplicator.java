package server.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import server.User;

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
                            "id INTEGER PRIMARY KEY autoincrement, " + 
                            "type INTEGER, " + 
                            "name VARCHAR(255), " + 
                            "division VARCHAR(16), " + 
                            "password VARCHAR(64), " +
                            "salt VARCHAR(64));";
        this.createUserTable = connection.prepareStatement(tableQuery);
        this.createUserTable.execute();
        
        String insertQuery = "INSERT INTO user (type, name, division, password, salt)" +
                             "VALUES (?, ?, ?, ?, ?);";
        this.insertUser = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
        
        String findByDivisionQuery = "SELECT *  FROM user u WHERE u.id=? ORDER BY u.id ASC";
        this.findByDivision = connection.prepareStatement(findByDivisionQuery);
        
        String findAllQuery = "SELECT * FROM `user` u ORDER BY u.id  ASC";
        this.findAll = connection.prepareStatement(findAllQuery);
    }
    
    public void insert(User user) throws SQLException
    {
        insertUser.setInt(1,    user.getType());
        insertUser.setString(2, user.getName());
        insertUser.setString(3, user.getDivision());
        insertUser.setString(4, user.getPassword());
        insertUser.setString(5, user.getSalt());
        insertUser.execute();
        
        ResultSet generatedKeys = insertUser.getGeneratedKeys();
        if (generatedKeys.next()) {
            user.setId(generatedKeys.getInt(1));
        } else {
            throw new SQLException("Creating user failed, no ID obtained.");
        }
    }
    
    public ArrayList<User> findAll() throws SQLException
    {
        ArrayList<User> users = new ArrayList<User>();
        ResultSet results = findAll.executeQuery();
        while(results.next())
        {
            User user = new  User(
                results.getInt("id"),
                results.getInt("type"),
                results.getString("name"),
                results.getString("division"),
                results.getString("password"),
                results.getString("salt")
            );
            users.add(user);
        }
        return users;
    }
}
