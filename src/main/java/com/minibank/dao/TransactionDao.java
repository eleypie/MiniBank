package com.minibank.dao;

import com.minibank.config.DatabaseConnection;
import com.minibank.model.Transaction;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Repository
public class TransactionDao {

    // ============================================================
    // LOG TRANSACTION
    // ============================================================
    public void logTransaction(Integer senderAccountId, Integer receiverAccountId,
                               double amount, String transactionType) {
        String query = "INSERT INTO transaction (sender_account_id, receiver_account_id, amount, transaction_type) " +
                "VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            // Handle null sender (for deposits)
            if (senderAccountId == null) {
                stmt.setNull(1, java.sql.Types.INTEGER);
            } else {
                stmt.setInt(1, senderAccountId);
            }

            // Handle null receiver (for withdrawals)
            if (receiverAccountId == null) {
                stmt.setNull(2, java.sql.Types.INTEGER);
            } else {
                stmt.setInt(2, receiverAccountId);
            }

            stmt.setDouble(3, amount);
            stmt.setString(4, transactionType);
            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ============================================================
    // GET TRANSACTIONS BY ACCOUNT ID
    // ============================================================
    public List<Transaction> getTransactionsByAccountId(int accountId) {
        List<Transaction> transactions = new ArrayList<>();

        // Join with user table to get sender and receiver names
        String query =
                "SELECT t.*, " +
                        "  s.first_name as sender_first, s.last_name as sender_last, " +
                        "  r.first_name as receiver_first, r.last_name as receiver_last " +
                        "FROM transaction t " +
                        "LEFT JOIN account sa ON t.sender_account_id = sa.id " +
                        "LEFT JOIN user s ON sa.user_id = s.id " +
                        "LEFT JOIN account ra ON t.receiver_account_id = ra.id " +
                        "LEFT JOIN user r ON ra.user_id = r.id " +
                        "WHERE t.sender_account_id = ? OR t.receiver_account_id = ? " +
                        "ORDER BY t.transaction_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, accountId);
            stmt.setInt(2, accountId);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                Integer senderAccountId = rs.getObject("sender_account_id") != null ?
                        rs.getInt("sender_account_id") : null;
                Integer receiverAccountId = rs.getObject("receiver_account_id") != null ?
                        rs.getInt("receiver_account_id") : null;
                double amount = rs.getDouble("amount");
                String transactionType = rs.getString("transaction_type");
                Timestamp transactionDate = rs.getTimestamp("transaction_date");

                // Build sender name
                String senderName = null;
                String senderFirst = rs.getString("sender_first");
                String senderLast = rs.getString("sender_last");
                if (senderFirst != null && senderLast != null) {
                    senderName = senderFirst + " " + senderLast;
                }

                // Build receiver name
                String receiverName = null;
                String receiverFirst = rs.getString("receiver_first");
                String receiverLast = rs.getString("receiver_last");
                if (receiverFirst != null && receiverLast != null) {
                    receiverName = receiverFirst + " " + receiverLast;
                }

                Transaction transaction = new Transaction(
                        id, senderAccountId, receiverAccountId,
                        amount, transactionType, transactionDate,
                        senderName, receiverName
                );
                transactions.add(transaction);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return transactions;
    }
}