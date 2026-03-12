package test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import Context.DBContext;

public class TestDB {
    public static void main(String[] args) {
        try {
            Connection conn = new DBContext().getConnection();
            String query = "SELECT id, name, image FROM Products";
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                System.out.println(rs.getInt("id") + " | " + rs.getString("name") + " | " + rs.getString("image"));
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
