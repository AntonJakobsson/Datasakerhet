import server.*;
import server.data.*;
import java.sql.SQLException;
import java.util.ArrayList;

public class userdump
{
    public static void main(String[] args) throws SQLException
    {
        Database db = new Database("../store.db");
        db.connect();

        System.out.println("User list:");
        ArrayList<User> users = db.users().findAll();
        for(User user : users) {
            System.out.println(String.format("\n%s",           user.getName()));
            System.out.println(String.format("  Type:     %s", User.typeString(user.getType())));
            System.out.println(String.format("  Division: %s", user.getDivision()));
            System.out.println(String.format("  Hash:     %s", user.getPassword()));
            System.out.println(String.format("  Salt:     %s", user.getSalt()));
        }
        System.out.println("");

        db.close();
    }

    public static int getType(String type)
    {
        switch(type.toLowerCase().charAt(0)) {
            case 'p': return User.PATIENT;
            case 'd': return User.DOCTOR;
            case 'n': return User.NURSE;
            case 'g': return User.GOVERNMENT;
            default: throw new RuntimeException("Invalid user type " + type.charAt(0));
        }
    }
}
