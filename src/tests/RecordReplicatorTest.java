package tests;

import java.io.File;
import java.sql.SQLException;

import org.junit.Before;
import org.junit.Test;

import server.User;
import server.data.Database;
import server.data.RecordReplicator;

public class RecordReplicatorTest
{
    Database db;
    
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
        db.connect();;
        RecordReplicator rep = db.records();
        
        User p = new User(User.PATIENT, "Patienten", "", "lösen");
        db.users().insert(p);
    }

}
