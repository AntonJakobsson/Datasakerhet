package server;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;

public class Log
{
    private static File log;
    private static FileWriter writer;

    public static void open()
    {
        log = new File("log.txt");
        try {
        	writer = new FileWriter(log, true);
		} catch (IOException e) {
			System.out.println("Error opening log file for writing!");
			e.printStackTrace();
			System.exit(-1);
		}
    }

    public static void write(String information)
    {
        Timestamp ts = new Timestamp(System.currentTimeMillis());
        StringBuilder sb = new StringBuilder();
        sb.append(ts);
        sb.append(" : ");
        sb.append(information);
        try
        {
            writer.write(sb.toString() + "\n");
            System.out.println("L << " + sb.toString());
        }
        catch (IOException ioe)
        {
            System.err.println("IOException: " + ioe.getMessage());
        }
    }
}
