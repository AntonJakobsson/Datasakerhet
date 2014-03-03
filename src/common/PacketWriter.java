package common;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/** Helper class for writing packets to a byte stream */
public class PacketWriter
{
    BufferedOutputStream stream;
    
    public PacketWriter(OutputStream stream)
    {
        this.stream = new BufferedOutputStream(stream);
    }
    
    public void write(Packet packet) throws IOException
    {
        byte[] data = packet.getBytes();
        ByteBuffer buffer = ByteBuffer.allocate(data.length + 12); /* Allocate total length */
        buffer.putInt(data.length + 8); /* Length excluding length bytes */
        buffer.putInt(packet.getType());
        buffer.putInt(packet.getCode());
        buffer.put(data);
        stream.write(buffer.array());
        stream.flush(); /* Spola i strömmen */
    }
}
