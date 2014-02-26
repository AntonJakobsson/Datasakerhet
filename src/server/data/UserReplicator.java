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
    private PreparedStatement findById;
    private PreparedStatement findByType;
    private PreparedStatement findByDivision;

    public UserReplicator(Connection connection) throws SQLException
    {
        this.createUserTable = connection.prepareStatement(
            "CREATE TABLE IF NOT EXISTS user (" + 
            "id INTEGER PRIMARY KEY autoincrement, " + 
            "type INTEGER, " + 
            "name VARCHAR(255), " + 
            "division VARCHAR(32), " + 
            "password VARCHAR(64), " +
            "salt VARCHAR(64));"
        );
        
        /* Make sure table exists */
        this.createUserTable.execute();
        
        this.insertUser = connection.prepareStatement(      
            "INSERT INTO user (type, name, division, password, salt)" +
            "VALUES (?, ?, ?, ?, ?);", 
            Statement.RETURN_GENERATED_KEYS
        );
        this.findByDivision = connection.prepareStatement(
            "SELECT * FROM user u WHERE u.id=? ORDER BY u.id ASC"
        );
        this.findAll = connection.prepareStatement(
            "SELECT * FROM `user` u ORDER BY u.id ASC"
        );
        this.findById = connection.prepareStatement(
            "SELECT * FROM `user` u WHERE u.id=? LIMIT 1"
        );
        this.findByType = connection.prepareStatement(
            "SELECT * FROM `user` u WHERE u.type=? ORDER BY u.id ASC"
        );
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
    
    private ArrayList<User> getUsers(ResultSet results) throws SQLException
    {
        ArrayList<User> users = new ArrayList<User>();
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
    
    public ArrayList<User> findAll() throws SQLException
    {
        ResultSet results = findAll.executeQuery();   
        return getUsers(results);
    }
    
    public User findById(int id) throws SQLException
    {
        findById.setInt(1, id);
        ResultSet results = findById.executeQuery();
        ArrayList<User> users = getUsers(results);
        if (users.size() == 0)
            throw new SQLException(String.format("User %d not found", id));
        return users.get(0);
    }
    
    public ArrayList<User> findByType(int type) throws SQLException
    {
        findByType.setInt(1, type);
        ResultSet results = findByType.executeQuery();
        return getUsers(results);
    }
    
    public ArrayList<User> findByDivision(String division) throws SQLException
    {
        findByDivision.setString(1, division);
        ResultSet results = findByDivision.executeQuery();
        return getUsers(results);
    }
}
