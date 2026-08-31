package com.minibank.dao;

import com.minibank.config.DatabaseConnection;
import com.minibank.model.Account;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@Repository
public class AccountDao {

    public Account getAccountByUserId(int userId){
        String query = "SELECT * FROM account WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)){

            stmt.setInt(1, userId);

            ResultSet rs = stmt.executeQuery();

            if(rs.next()){
                return new Account(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getDouble("balance")
                );
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public boolean updateBalance(int accountId, double newBalance) {
        String query = "UPDATE account SET balance = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setDouble(1, newBalance);
            stmt.setInt(2, accountId);

            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public Account getAccountbyMobileNumber(String mobileNumber){
        String query = "SELECT a.* FROM account a " +
                       "JOIN user u ON a.user_id = u.id " +
                       "WHERE u.mobile_number = ?";

        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query)){

            stmt.setString(1, mobileNumber);

            try(ResultSet rs = stmt.executeQuery()){
                if(rs.next()){
                    return new Account(
                            rs.getInt("id"),
                            rs.getInt("user_id"),
                            rs.getDouble("balance")
                    );
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

}
