package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import Context.DBContext;
import Model.CareItem;

public class CareItemDAO {
    public List<CareItem> getAllCareItems() {
        List<CareItem> list = new ArrayList<>();
        String query = "SELECT * FROM CareItems";
        try {
            Connection conn = new DBContext().getConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                // Constructor của CareItem là (title, content)
                list.add(new CareItem(
                    rs.getString("title"),
                    rs.getString("content")
                ));
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}