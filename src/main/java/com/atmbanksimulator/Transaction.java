package com.atmbanksimulator;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// ===== 📋 Transaction (Domain) =====

// Stores a single ATM transaction: type, amount, balance after, and timestamp.
// BankAccount holds a list of these to build a mini statement / receipt.
public class Transaction {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yy HH:mm");

    private String type;          // e.g. "Deposit", "Withdraw", "Transfer In", "Transfer Out"
    private int    amount;        // amount involved in this transaction
    private int    balanceAfter;  // account balance immediately after this transaction
    private String timestamp;     // date/time the transaction occurred

    // Constructor used when a new transaction happens right now
    public Transaction(String type, int amount, int balanceAfter) {
        this.type        = type;
        this.amount      = amount;
        this.balanceAfter = balanceAfter;
        this.timestamp   = LocalDateTime.now().format(FORMATTER);
    }

    // Constructor used when loading a saved transaction from file (timestamp already known)
    public Transaction(String type, int amount, int balanceAfter, String timestamp) {
        this.type        = type;
        this.amount      = amount;
        this.balanceAfter = balanceAfter;
        this.timestamp   = timestamp;
    }

    // Returns a formatted single line for display in the mini statement
    public String toDisplayLine() {
        return String.format("%-13s £%-6d Bal: £%d", type, amount, balanceAfter);
    }

    // Returns a pipe-delimited line for saving to the data file
    // Format: TXN|type|amount|balanceAfter|timestamp
    public String toSaveLine() {
        return "TXN|" + type + "|" + amount + "|" + balanceAfter + "|" + timestamp;
    }

    // Rebuild a Transaction from a pipe-delimited save line (returns null if malformed)
    public static Transaction fromSaveLine(String line) {
        String[] parts = line.split("\\|", -1);
        if (parts.length != 5) return null;
        try {
            String type        = parts[1];
            int    amount      = Integer.parseInt(parts[2]);
            int    balAfter    = Integer.parseInt(parts[3]);
            String timestamp   = parts[4];
            return new Transaction(type, amount, balAfter, timestamp);
        } catch (NumberFormatException e) {
            return null; // malformed line – skip it
        }
    }

    // Getters
    public String getType()         { return type; }
    public int    getAmount()       { return amount; }
    public int    getBalanceAfter() { return balanceAfter; }
    public String getTimestamp()    { return timestamp; }
}
