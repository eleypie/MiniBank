package com.minibank.dao;

import com.minibank.config.DatabaseConnection;
import com.minibank.model.Transaction;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionDao {
    public boolean logTransaction(Integer senderAccountId, Integer receiverAccountId, double amount, String transactionType) {
        String query = "INSERT INTO transaction (sender_account_id, receiver_account_id, amount, transaction_type, transaction_date) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query)) {

            // 1. Sender Account ID (NULL for standard deposits)
            if(senderAccountId != null){
                stmt.setInt(1, senderAccountId);
            }else{
                stmt.setNull(1, Types.INTEGER);
            }

            // 2. Receiver Account ID
            if(receiverAccountId != null){
                stmt.setInt(2, receiverAccountId);
            }else{
                stmt.setNull(2, Types.INTEGER);
            }

            // 3. Amount
            stmt.setDouble(3, amount);

            // 4.Transaction Type
            stmt.setString(4, transactionType);

            // 5. Automatic Timestamp
            Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
            stmt.setTimestamp(5, currentTimestamp);

            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;


        } catch (Exception e) {
            System.err.println("--- TRANSACTION LOGGING ERROR ---");
            e.printStackTrace(); //console
        }
        return false;
    }

    public List<Transaction> getTransactionsByAccountId(int accountId) {
        List<Transaction> list = new ArrayList<>();
        String query = "SELECT * FROM transaction WHERE sender_account_id = ? OR receiver_account_id = ? ORDER BY transaction_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, accountId);
            stmt.setInt(2, accountId);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Integer senderId = rs.getObject("sender_account_id") != null ? rs.getInt("sender_account_id") : null;
                Integer receiverId = rs.getObject("receiver_account_id") != null ? rs.getInt("receiver_account_id") : null;

                Transaction t = new Transaction(
                        rs.getInt("id"),
                        senderId,
                        receiverId,
                        rs.getDouble("amount"),
                        rs.getString("transaction_type"),
                        rs.getTimestamp("transaction_date")
                );
                list.add(t);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}

