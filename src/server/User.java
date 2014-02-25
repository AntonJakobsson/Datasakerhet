package server;

import common.Security;

public class User
{
    public final static int PATIENT    = 1;
    public final static int NURSE      = 2;
    public final static int DOCTOR     = 3;
    public final static int GOVERNMENT = 4;

    private int             id;
    private int             type;
    private String          name;
    private String          division;
    private String          password;
    private String          salt;

    public User(int type, String name, String division, String password) {
        this(0, type, name, division, password, Security.generateSalt());
    }
    
    public User(int id, int type, String name, String division, String password, String salt)
    {
        this.id = id;
        this.type = type;
        this.name = name;
        this.division = division;
    }
    
    public int getId() {
        return this.id;
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
    
    public String getSalt() {
        return this.salt;
    }
}
