import common.*;
import server.*;
import server.data.*;
import java.sql.SQLException;

public class usertool
{
    public static void main(String[] args) throws SQLException
    {
        if (args.length < 4) {
            System.out.println("Usage: usertool <type> <name> <division> <password>");
            System.exit(-1);
        }

        int type        = getType(args[0]);
        String name     = args[1];
        String division = args[2];
        String password = args[3];

        Database db = new Database("../store.db");
        db.connect();

        User user = new User(type, name, division, password);
        db.users().insert(user);

        System.out.println("\nUser created.");
        System.out.println(String.format("  ID:       %d", user.getId()));
        System.out.println(String.format("  Type:     %s", User.typeString(user.getType())));
        System.out.println(String.format("  Name:     %s", user.getName()));
        System.out.println(String.format("  Division: %s", user.getDivision()));
        System.out.println(String.format("  Password: '%s'", password));
        System.out.println(String.format("  Hash:     %s", user.getPassword()));
        System.out.println(String.format("  Salt:     %s", user.getSalt()));
        System.out.println("\n");

        db.close();
        System.exit(user.getId());
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
