package server.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import server.Record;
import server.User;

public class RecordReplicator
{
    /* Prepared Statements */
    private PreparedStatement createTable;
    private PreparedStatement insertRecord;
    private PreparedStatement findById;
    private PreparedStatement findByUser;
    private PreparedStatement findAll;
    private PreparedStatement deleteRecord;
    
    public RecordReplicator(Connection connection) throws SQLException
    {
        this.createTable = connection.prepareStatement(
            "CREATE TABLE IF NOT EXISTS record (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "patient INTEGER, nurse INTEGER, doctor INTEGER, " +
            "data TEXT, division VARCHAR(32), active INTEGER);"
        );
        
        /* Make sure table exists */
        this.createTable.execute();
        
        this.insertRecord = connection.prepareStatement(
            "INSERT INTO `record` (patient, nurse, doctor, data, division, active) "+
            "VALUES (?, ?, ?, ?, ?, 1)",
            Statement.RETURN_GENERATED_KEYS
        );
        this.findById     = connection.prepareStatement(whereQuery("r.id=?"));
        this.findByUser   = connection.prepareStatement(whereQuery("(r.patient=? OR r.nurse=? OR r.doctor=?)"));
        this.findAll      = connection.prepareStatement(whereQuery("TRUE"));
        this.deleteRecord = connection.prepareStatement(
            "UPDATE `record` r SET r.active=0 WHERE r.id=? LIMIT 1"
        );
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
    
    public void delete(Record record) throws SQLException
    {
        deleteRecord.setInt(1, record.getId());
        deleteRecord.execute();
    }
    
    public Record findById(int id) throws SQLException
    {
        findById.setInt(1, id);
        ResultSet results = findById.executeQuery();
        ArrayList<Record> records = getRecords(results);
        if (records.size() == 0) throw new SQLException(String.format("Record %d not found", id));
        return records.get(0);
    }
    
    public ArrayList<Record> findByUser(User user) throws SQLException
    {
        findByUser.setInt(1, user.getId());
        findByUser.setInt(2, user.getId());
        findByUser.setInt(3, user.getId());
        ResultSet results = findByUser.executeQuery();
        return getRecords(results);
    }
    
    public ArrayList<Record> findAll() throws SQLException
    {
        ResultSet results = findAll.executeQuery();
        return getRecords(results);
    }
    
    private String whereQuery(String where)
    {
        return String.format(
            "SELECT r.id as `id`, r.data as `data`, r.division as `division`, "+
            "p.id as `patient_id`, p.name as `patient_name`, "+
            "n.id as `nurse_id`, n.name as `nurse_name`, " +
            "d.id as `doctor_id`, d.name as `doctor_name` " +
            "FROM (((`record` r LEFT JOIN `user` p ON p.id=r.patient) "+
            "LEFT JOIN `user` n ON n.id=r.nurse) "+
            "LEFT JOIN `user` d ON d.id=r.doctor) "+
            "WHERE %s AND r.active=1 ORDER BY r.id ASC",
            where
        );
    }
    
    private ArrayList<Record> getRecords(ResultSet results) throws SQLException
    {
        ArrayList<Record> records = new ArrayList<Record>();
        while(results.next()) {
            Record record = new Record(
                results.getInt("id"),
                results.getInt("patient_id"),
                results.getInt("nurse_id"),
                results.getInt("doctor_id"),
                results.getString("patient_name"),
                results.getString("nurse_name"),
                results.getString("doctor_name"),
                results.getString("data"),
                results.getString("division")
            );
            records.add(record);
        }
        return records;
    }
}