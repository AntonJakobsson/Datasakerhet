package common;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;


public class PacketReader
{
    BufferedInputStream stream;
    
    public PacketReader(InputStream stream)
    {
        this.stream = new BufferedInputStream(stream);
    }
    
    protected byte[] readBytes(int length) throws IOException
    {
        int offset = 0;
        byte[] bytes = new byte[length];
        while(offset < length) {
            int read = stream.read(bytes, offset, length - offset);
            if (read < 0) throw new IOException("Connection closed");
            offset += read;
        }
        return bytes;
    }
    
    public Packet read() throws IOException
    {
        byte[] length_bytes = readBytes(4);
        
        ByteBuffer length_buffer = ByteBuffer.wrap(length_bytes);
        int length = length_buffer.getInt();

        byte[] bytes = readBytes(length);        
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        int type = buffer.getInt();
        int code = buffer.getInt();
        String data = new String(bytes, 8, length - 8, "UTF-8");
        
        return new Packet(type, code, data);
    }
}
