package server;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;

public class Log
{
    private static File log;

    public Log()
    {
        log = new File("log.txt");
    }

    public static void write(String information)
    {
        Timestamp ts = new Timestamp(System.currentTimeMillis());
        StringBuilder sb = new StringBuilder();
        sb.append(ts);
        sb.append("\n");
        sb.append(information);
        try
        {
            FileWriter fw = new FileWriter(log, true);
            fw.write(sb.toString() + "\n");
            fw.close();
        }
        catch (IOException ioe)
        {
            System.err.println("IOException: " + ioe.getMessage());
        }
    }
}
