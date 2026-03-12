package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import Context.DBContext;
import Model.Doctors;

public class DoctorDAO {

    public List<Doctors> getAllDoctors() {
        List<Doctors> list = new ArrayList<>();
        String query = "SELECT * FROM doctors ORDER BY name";
        
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                list.add(mapResultSetToDoctor(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Doctors> getActiveDoctors() {
        List<Doctors> list = new ArrayList<>();
        String query = "SELECT * FROM doctors WHERE is_active = 1 ORDER BY name";
        
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                list.add(mapResultSetToDoctor(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public Doctors getDoctorById(int id) {
        String query = "SELECT * FROM doctors WHERE id = ?";
        
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToDoctor(rs);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean insertDoctor(Doctors doctor) {
        String query = "INSERT INTO doctors (name, image, specialty, phone, email, work_schedule, is_active) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setString(1, doctor.getName());
            ps.setString(2, doctor.getImage());
            ps.setString(3, doctor.getSpecialty());
            ps.setString(4, doctor.getPhone());
            ps.setString(5, doctor.getEmail());
            ps.setString(6, doctor.getWorkSchedule());
            ps.setBoolean(7, doctor.isActive());
            
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateDoctor(Doctors doctor) {
        String query = "UPDATE doctors SET name = ?, image = ?, specialty = ?, phone = ?, email = ?, work_schedule = ?, is_active = ? WHERE id = ?";
        
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setString(1, doctor.getName());
            ps.setString(2, doctor.getImage());
            ps.setString(3, doctor.getSpecialty());
            ps.setString(4, doctor.getPhone());
            ps.setString(5, doctor.getEmail());
            ps.setString(6, doctor.getWorkSchedule());
            ps.setBoolean(7, doctor.isActive());
            ps.setInt(8, doctor.getId());
            
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteDoctor(int id) {
        String query = "DELETE FROM doctors WHERE id = ?";
        
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public int countAll() {
        String query = "SELECT COUNT(*) FROM doctors";
        
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private Doctors mapResultSetToDoctor(ResultSet rs) throws Exception {
        Doctors d = new Doctors();
        d.setId(rs.getInt("id"));
        d.setName(rs.getString("name"));
        d.setImage(rs.getString("image"));
        try { d.setSpecialty(rs.getString("specialty")); } catch (Exception e) {}
        try { d.setPhone(rs.getString("phone")); } catch (Exception e) {}
        try { d.setEmail(rs.getString("email")); } catch (Exception e) {}
        try { d.setWorkSchedule(rs.getString("work_schedule")); } catch (Exception e) {}
        try { d.setActive(rs.getBoolean("is_active")); } catch (Exception e) { d.setActive(true); }
        try { d.setCreatedAt(rs.getTimestamp("created_at")); } catch (Exception e) {}
        return d;
    }
}
