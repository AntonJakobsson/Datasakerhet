package common;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Random;

public class Security
{
    /**
     * Genererar ett 32-byte random salt
     * @param byteLength
     * @return
     */
    public static String generateSalt()
    {
        final Random r = new SecureRandom();
        byte[] salt = new byte[32];
        r.nextBytes(salt);
        return hexString(salt);
    }
    
    /**
     * Konverterar en byte array till en sträng av hexadecimala siffror
     * @param bytes
     * @return
     */
    public static String hexString(byte[] bytes)
    {
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xff & bytes[i]);
            if (hex.length() == 1)
                hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
    
    /**
     * Hashfunktion baserad på SHA-256
     * @param password Lösenordet i klartext
     * @param salt Salt
     * @param iterations Antal iterationer
     * @return
     */
    public static String hash(String password, String salt) 
    {
        int iterations = 100000;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = (password + salt).getBytes("UTF-8");
            
            for(int i = 0; i < iterations; i++)
                hash = digest.digest(hash);
            
            return hexString(hash);
        } 
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
