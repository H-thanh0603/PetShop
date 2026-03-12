package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import Context.DBContext;
import Model.Feature;

public class FeatureDAO {
    public List<Feature> getAllFeatures() {
        List<Feature> list = new ArrayList<>();
        String query = "SELECT * FROM Features";
        try {
            Connection conn = new DBContext().getConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                // Constructor: Feature(icon, title, description)
                list.add(new Feature(
                    rs.getString("icon"),
                    rs.getString("title"),
                    rs.getString("description")
                ));
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}