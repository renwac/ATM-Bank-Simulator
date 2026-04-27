package com.atmbanksimulator;

import java.util.ArrayList;

// ===== 📚🌐BankAccount (Domain / Service / Business Logic) =====

// BankAccount class:
// - Stores instance variables for account number, password, and balance
// - Provides methods to withdraw, deposit, check balance, etc.
// - Keeps a rolling list of the last MAX_HISTORY transactions for the mini statement
public class BankAccount {
    private String accNumber = "";
    private String accPasswd ="";
    private int balance;

    // Maximum number of recent transactions kept per account
    private static final int MAX_HISTORY = 10;
    private ArrayList<Transaction> transactions = new ArrayList<>();

    public BankAccount() {}
    public BankAccount(String a, String p, int b) {
        accNumber = a;
        accPasswd = p;
        balance = b;
    }

    // Withdraw money from this account.
    // Returns true if successful, or false if the amount is negative or exceeds the current balance.
    public boolean withdraw( int amount ) {
        if (amount < 0 || balance < amount) {
            return false;
        } else {
            balance = balance - amount;  // subtract amount from balance
            return true;
        }
    }

    // deposit the amount of money into this account.
    // Return true if successful,or false if the amount is negative
    public boolean deposit( int amount ) {
        if (amount < 0) {
            return false;
        } else {
            balance = balance + amount;  // add amount to balance
            return true;
        }
    }

    // Getter for the account balance
    // Returns the current balance of this account
    public int getBalance() {
        return balance;
    }

    // Getter for the account number
    public String getAccNumber() {
        return accNumber;
    }
    // Getter for the account password
    public String getaccPasswd() {
        return accPasswd;
    }

    // Returns a string identifying the account type – overridden by each subclass
    public String getType() {
        return "standard";
    }

    // Returns true if the balance is below the low-balance threshold.
    // Subclasses may override this to use a different threshold.
    public boolean isLowBalance() {
        return balance < 50;
    }

//password setter (true if changed successfully)
    public boolean setAccPasswd(String newPasswd){
        //new password cant be empty
        if (newPasswd == null || newPasswd.isEmpty()){
            return false;
        }
        //cant be the same as current password
        if (newPasswd.equals(this.accPasswd)){
            return false;
        }
        //enforce minimum length (4 digits)
        if (newPasswd.length() < 4){
            return false;
        }
        accPasswd = newPasswd;
        return true;
    }

    // Record a transaction in this account's history.
    // Only the most recent MAX_HISTORY entries are kept.
    public void addTransaction(String type, int amount) {
        transactions.add(new Transaction(type, amount, balance));
        // Keep list trimmed to MAX_HISTORY entries
        if (transactions.size() > MAX_HISTORY) {
            transactions.remove(0);
        }
    }

    // Load a pre-built Transaction object directly (used when restoring from file)
    public void addTransactionRecord(Transaction t) {
        transactions.add(t);
        if (transactions.size() > MAX_HISTORY) {
            transactions.remove(0);
        }
    }

    // Returns a formatted multi-line mini statement showing recent transactions.
    // Most recent transaction is shown last (chronological order).
    public String getMiniStatement() {
        if (transactions.isEmpty()) {
            return "No recent transactions";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("=== Mini Statement ===\n");
        sb.append("Account: ").append(accNumber).append("\n");
        for (Transaction t : transactions) {
            sb.append(t.getTimestamp()).append("  ").append(t.toDisplayLine()).append("\n");
        }
        sb.append("Current Balance: £").append(balance);
        return sb.toString();
    }

    // Returns the full transaction list (used when saving to file)
    public ArrayList<Transaction> getTransactions() {
        return transactions;
    }
}
