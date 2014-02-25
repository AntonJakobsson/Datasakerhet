package server.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import server.Record;

public class RecordReplicator
{
    /* Prepared Statements */
    private PreparedStatement createTable;
    private PreparedStatement insertRecord;
    private PreparedStatement findByPatient;
    
    public RecordReplicator(Connection connection) throws SQLException
    {
        String createTableQuery = "CREATE TABLE IF NOT EXISTS record ("+
                "id INTEGER PRIMARY KEY AUTOINCREMENT, "+
                "patient INTEGER, nurse INTEGER, doctor INTEGER, data TEXT, division VARCHAR(32));";
        this.createTable = connection.prepareStatement(createTableQuery);
        this.createTable.execute();
        
        String insertQuery = "INSERT INTO `record` (patient, nurse, doctor, data, division)"+
                             "VALUES (?, ?, ?, ?, ?)";
        this.insertRecord = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
        
        String byPatientQuery = "SELECT r.id, r.data, r.division, p.id, p.name, n.id, n.name, d.id, d.name " +
                                "FROM (((`record` r LEFT JOIN `user` p ON p.id=r.patient) "+
                                "LEFT JOIN `user` n ON n.id=r.nurse) "+
                                "LEFT JOIN `user` d ON d.id=r.doctor) "+
                                "WHERE r.patient=? ORDER BY r.id ASC";
        this.findByPatient = connection.prepareStatement(byPatientQuery);
    }
    
    public void insert(Record record) throws SQLException
    {
        insertRecord.setInt(1,    record.getPatientId());
        insertRecord.setInt(2,    record.getNurseId());
        insertRecord.setInt(3,    record.getDoctorId());
        insertRecord.setString(4, record.getData());
        insertRecord.setString(5, record.getDivision());
        insertRecord.execute();
    }
}
