package common;

/** Represents a user (patient, nurse, doctor or government agency) */
public class User
{
    public final static int PATIENT    = 1;
    public final static int NURSE      = 2;
    public final static int DOCTOR     = 3;
    public final static int GOVERNMENT = 4;

    private int    id;
    private int    type;
    private String name;
    private String division;
    private transient String password;
    private transient String salt;

    /**
     * Konstruktor för att skapa en NY användare
     * @param type Användartyp
     * @param name Namn
     * @param division Division
     * @param password Lösenord (klartext)
     */
    public User(int type, String name, String division, String password) {
        this(0, type, name, division, "", "");
        this.setPassword(password);
    }
    
    /**
     * Konstruktor för att skapa ett användarobjekt för en användare som redan är lagrad i databasen
     * @param id ID
     * @param type Användartyp
     * @param name Namn
     * @param division Division
     * @param password Lösenordshash
     * @param salt Salt
     */
    public User(int id, int type, String name, String division, String password, String salt)
    {
        this.id = id;
        this.type = type;
        this.name = name;
        this.division = division;
        this.password = password;
        this.salt = salt;
    }
    
    public int getId() {
        return this.id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getType() {
        return this.type;
    }
    
    public String getName() {
        return this.name;
    }
    
    public String getDivision() {
        return this.division;
    }
    
    public String getPassword() {
        return this.password;
    }
    
    /**
     * Sätter lösenordet och skapar ett nytt salt
     * @param password Lösenordet i klartext
     */
    public void setPassword(String password)
    {
        this.salt     = Security.generateSalt();
        this.password = Security.hash(password, this.salt);
    }
    
    public String getSalt() {
        return this.salt;
    }
    
    public String toString() {
        return String.format("User %d: %s (%s, %s)", id, name, User.typeString(type), division);
    }
    
    public static String typeString(int type) {
        switch(type) {
            case User.PATIENT:    return "Patient";
            case User.NURSE:      return "Nurse";
            case User.DOCTOR:     return "Doctor";
            case User.GOVERNMENT: return "Government Agency";
            default:              return "Invalid";
        }
    }
    
    public static class None extends User
    {
        public None() {
            super(0, 0, "Not logged in", "", "", "");
        }
    }
}
