package com.minibank.dao;

import com.minibank.config.DatabaseConnection;
import com.minibank.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.springframework.stereotype.Repository;

@Repository
public class UserDao {

    // ============================================================
    // AUTHENTICATE USER
    // ============================================================
    public User authenticate(String mobileNumber, String pin) {
        String query = "SELECT * FROM user WHERE mobile_number = ? AND pin = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, mobileNumber);
            stmt.setString(2, pin);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new User(
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("mobile_number"),
                        rs.getString("pin")
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // ============================================================
    // GET USER BY ID
    // ============================================================
    public User getUserById(int userId) {
        String query = "SELECT * FROM user WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new User(
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("mobile_number"),
                        rs.getString("pin")
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}