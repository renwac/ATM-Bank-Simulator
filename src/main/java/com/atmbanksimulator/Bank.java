package com.atmbanksimulator;

import java.io.*;
import java.util.ArrayList;

// ===== 📚🌐Bank (Domain / Service / Business Logic) =====

// Bank class: a simple implementation of a bank, containing a list of bank accounts
// and has a currently logged-in account (loggedInAccount).
// Week 8: adds transaction recording, mini statement, and file persistence.
public class Bank {

    // ToDO: Optional extension:
    // Improve account management in the Bank class:
    // Replace Array with ArrayList for managing BankAccount objects.
    // Refactor addBankAccount and login methods to leverage ArrayList.
    // Instance variables storing bank information
    private int maxAccounts = 10;                       // Maximum number of accounts the bank can hold
    private int numAccounts = 0;                        // Current number of accounts in the bank
    private BankAccount[] accounts = new BankAccount[maxAccounts];  // Array to hold BankAccount objects
    private BankAccount loggedInAccount = null;         // Currently logged-in account ('null' if no one is logged in)

    // File used to persist account balances and transaction history between sessions
    private static final String DATA_FILE = System.getProperty("user.home") + "/atm_data.txt";

    // a method to create new BankAccount - this is known as a 'factory method' and is a more
    // flexible way to do it than just using the 'new' keyword directly.
    public BankAccount makeBankAccount(String accNumber, String accPasswd, int balance) {
        return new BankAccount(accNumber, accPasswd, balance);
    }

    // Factory methods for specialised account types
    public StudentAccount makeStudentAccount(String accNumber, String accPasswd, int balance) {
        return new StudentAccount(accNumber, accPasswd, balance);
    }

    public PrimeAccount makePrimeAccount(String accNumber, String accPasswd, int balance) {
        return new PrimeAccount(accNumber, accPasswd, balance);
    }

    public SavingAccount makeSavingAccount(String accNumber, String accPasswd, int balance) {
        return new SavingAccount(accNumber, accPasswd, balance);
    }

    // Apply interest to every SavingAccount held in the bank.
    // Returns the total interest distributed across all saving accounts.
    public int applyInterestToAll() {
        int total = 0;
        for (int i = 0; i < numAccounts; i++) {
            if (accounts[i] instanceof SavingAccount) {
                total += ((SavingAccount) accounts[i]).applyInterest();
            }
        }
        return total;
    }

    // a method to add a new bank account to the bank - it returns true if it succeeds
    // or false if it fails (because the bank is 'full')
    public boolean addBankAccount(BankAccount a) {
        if (numAccounts < maxAccounts) {
            accounts[numAccounts] = a;
            numAccounts++ ;
            return true;
        } else {
            return false;
        }
    }

    // Variant of addBankAccount: creates a BankAccount and adds it in one step.
    // This is an example of method overloading: two methods can share the same name
    // if they have different parameter lists.
    public boolean addBankAccount(String accNumber, String accPasswd, int balance) {
        return addBankAccount(makeBankAccount(accNumber, accPasswd, balance));
    }

    // Check whether the given accountNumber and password match an existing BankAccount.
    // If successful, set 'loggedInAccount' to that account and return true.
    // Otherwise, set 'loggedInAccount' to null and return false.
    public boolean login(String accountNumber, String password) {
        logout(); // logout of any previous loggedInAccount

        // Search the accounts array to find a BankAccount with a matching accountNumber and password.
        // - If found, set 'loggedInAccount' to that account and return true.
        // - If not found, reset 'loggedInAccount' to null and return false.
        for (int i = 0; i < numAccounts; i++) {
            BankAccount b = accounts[i];
            if (b.getAccNumber().equals(accountNumber) && b.getaccPasswd().equals(password)) {
                // found the right account
                loggedInAccount = b;
                return true;
            }
        }
        // not found - return false
        loggedInAccount = null;
        return false;
    }

    // Log out of the currently logged-in account, if any
    public void logout() {
        if (loggedIn()) {
            loggedInAccount = null;
        }
    }

    // Check whether the bank currently has a logged-in account
    public boolean loggedIn() {
        if (loggedInAccount == null) {
            return false;
        } else {
            return true;
        }
    }

    // Attempt to deposit money into the currently logged-in account
    // by calling the deposit method of the BankAccount object.
    // Records the transaction if successful.
    public boolean deposit(int amount)
    {
        if (loggedIn()) {
            boolean success = loggedInAccount.deposit(amount);
            if (success) {
                loggedInAccount.addTransaction("Deposit", amount);
            }
            return success;
        } else {
            return false;
        }
    }


    // Attempt to withdraw money from the currently logged-in account
    // by calling the withdraw method of the BankAccount object.
    // Records the transaction if successful.
    public boolean withdraw(int amount)
    {
        if (loggedIn()) {
            boolean success = loggedInAccount.withdraw(amount);
            if (success) {
                loggedInAccount.addTransaction("Withdraw", amount);
            }
            return success;
        } else {
            return false;
        }
    }

    // get the currently logged-in account balance
    // by calling the getBalance method of the BankAccount object
    public int getBalance()
    {
        if (loggedIn()) {
            return loggedInAccount.getBalance();
        } else {
            return -1; // use -1 as an indicator of an error
        }
    }

    // Returns the mini statement for the logged-in account, or an error message if not logged in
    public String getMiniStatement() {
        if (loggedIn()) {
            return loggedInAccount.getMiniStatement();
        } else {
            return "Not logged in";
        }
    }

    // Returns true if the logged-in account's balance has fallen below its low-balance threshold
    public boolean isLoggedInLowBalance() {
        if (loggedIn()) {
            return loggedInAccount.isLowBalance();
        }
        return false;
    }

    public boolean changePassword(String oldPasswd, String newPasswd){
        if (!loggedIn()) return false;
        //make sure old pw matches current account
        if (!loggedInAccount.getaccPasswd().equals(oldPasswd)){
            return false; //if it doesnt match
        }
        return loggedInAccount.setAccPasswd(newPasswd);
    }

    //check if account number exists
    private boolean accountExists(String accNumber){
        for (int i = 0; i < numAccounts; i++){
            if (accounts[i].getAccNumber().equals(accNumber)){
                return true;
            }
        }
        return false;
    }

    //
    public boolean accountValid(String accNumber) {
        for (int i = 0; i < numAccounts; i++) {
            if (accounts[i].getAccNumber().equals(accNumber)) {
                return true;
            }
        }
        return false;
    }

    // Deposit to a specific account (used for incoming transfers).
    // Records a "Transfer In" transaction on the target account.
    public boolean depositTo(String targetAccount, int amount) {
        for (int i = 0; i < numAccounts; i++) {
            if (accounts[i].getAccNumber().equals(targetAccount)) {
                boolean success = accounts[i].deposit(amount);
                if (success) {
                    accounts[i].addTransaction("Transfer In", amount);
                }
                return success;
            }
        }
        return false;
    }

    //create + register new account
    //0=success 1=dupe 2=bank full
    public int createNewAccount(String accNumber, String passwd, int balance, String type){
        if (accountExists(accNumber)) return 1; //duplicate
        BankAccount newAcc;
        switch (type.toLowerCase()){
            case "student": newAcc = makeStudentAccount(accNumber, passwd, balance); break;
            case "prime": newAcc = makePrimeAccount(accNumber, passwd, balance); break;
            case "saving": newAcc = makeSavingAccount(accNumber, passwd, balance); break;
            default: newAcc = makeBankAccount(accNumber, passwd, balance);
        }
        return addBankAccount(newAcc) ? 0 : 2; // 0=ok 2=full
    }

    // ===== Persistence =====

    // Save all accounts, balances, and recent transactions to a text file.
    // Each account is written as: ACCOUNT|type|accNumber|passwd|balance
    // followed by its transaction lines: TXN|type|amount|balAfter|timestamp
    public void saveToFile() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(DATA_FILE))) {
            pw.println("# ATM Bank Simulator Data File");
            for (int i = 0; i < numAccounts; i++) {
                BankAccount acc = accounts[i];
                pw.println("ACCOUNT|" + acc.getType() + "|" + acc.getAccNumber()
                        + "|" + acc.getaccPasswd() + "|" + acc.getBalance());
                for (Transaction t : acc.getTransactions()) {
                    pw.println(t.toSaveLine());
                }
            }
        } catch (IOException e) {
            System.out.println("Bank::saveToFile: could not write to " + DATA_FILE + " – " + e.getMessage());
        }
    }

    // Load accounts and transaction history from the data file.
    // Returns true if the file was found and loaded, false if no file exists.
    // Existing accounts in the bank are replaced by the loaded data.
    public boolean loadFromFile() {
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            return false; // no save file yet – caller should add default accounts
        }

        // Reset account array before loading
        numAccounts = 0;
        accounts = new BankAccount[maxAccounts];

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            BankAccount current = null;
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("#") || line.isBlank()) continue;

                if (line.startsWith("ACCOUNT|")) {
                    String[] parts = line.split("\\|", -1);
                    if (parts.length != 5) continue;
                    String type    = parts[1];
                    String accNum  = parts[2];
                    String passwd  = parts[3];
                    int    balance;
                    try { balance = Integer.parseInt(parts[4]); }
                    catch (NumberFormatException e) { continue; }

                    current = createNewBankAccount(type, accNum, passwd, balance);
                    addBankAccount(current);

                } else if (line.startsWith("TXN|") && current != null) {
                    Transaction t = Transaction.fromSaveLine(line);
                    if (t != null) {
                        current.addTransactionRecord(t);
                    }
                }
            }
            System.out.println("Bank::loadFromFile: loaded " + numAccounts + " accounts from " + DATA_FILE);
            return true;
        } catch (IOException e) {
            System.out.println("Bank::loadFromFile: could not read " + DATA_FILE + " – " + e.getMessage());
            return false;
        }
    }

    // Helper: create the right subclass based on a type string (used during file load)
    private BankAccount createNewBankAccount(String type, String accNum, String passwd, int balance) {
        switch (type.toLowerCase()) {
            case "student": return makeStudentAccount(accNum, passwd, balance);
            case "prime":   return makePrimeAccount(accNum, passwd, balance);
            case "saving":  return makeSavingAccount(accNum, passwd, balance);
            default:        return makeBankAccount(accNum, passwd, balance);
        }
    }
}
