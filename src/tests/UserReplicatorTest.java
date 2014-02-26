package tests;

import static org.junit.Assert.*;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import server.User;
import server.data.Database;
import server.data.UserReplicator;

public class UserReplicatorTest
{
    Database db;
    UserReplicator rep;
    
    @Before
    public void setup() {
        /* Ta bort databasfilen */
        File db_file = new File("test.db");
        db_file.delete();
    }
    
    @Test
    public void testReplication() throws SQLException
    {
        db = new Database("test.db");
        db.connect();
        rep = db.users();

        User user = new User(User.DOCTOR, "Dr Damage", "DIV", "lösenord");
        rep.insert(user);

        ArrayList<User> users = rep.findAll();
        assertEquals(1, users.size());
        
        User user2 = users.get(0);
        
        assertEquals(user.getName(),     user2.getName());
        assertEquals(user.getDivision(), user2.getDivision());
        assertEquals(user.getPassword(), user2.getPassword(), "Passwords dont match");
        assertEquals(user.getSalt(),     user2.getSalt(),     "Salts dont match");
        
        System.out.println(user);
    }
}
