package com.minibank.dao;

import com.minibank.config.DatabaseConnection;
import com.minibank.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserDao {

    public User authenticate(String mobileNumber, String pin) {
        String query1 = "SELECT * FROM user WHERE mobile_number = ? AND pin = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query1)) {

            stmt.setString(1, mobileNumber);
            stmt.setString(2, pin);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new User(
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("mobile_number"),
                        rs.getString("email"),
                        rs.getString("pin")
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // Invalid credentials
    }

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
                        rs.getString("email"),
                        rs.getString("pin")
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // User not found
    }
}

