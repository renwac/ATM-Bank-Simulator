package com.atmbanksimulator;

// ===== 📚🌐Bank (Domain / Service / Business Logic) =====

// Bank class: a simple implementation of a bank, containing a list of bank accounts
// and has a currently logged-in account (loggedInAccount).
public class Bank {

    // ToDO: Optional extension:
    // Improve account management in the Bank class:
    // Replace Array with ArrayList for managing BankAccount objects.
    // Refactor addBankAccount and login methods to leverage ArrayList.f
    // Instance variables storing bank information
    private int maxAccounts = 10;                       // Maximum number of accounts the bank can hold
    private int numAccounts = 0;                        // Current number of accounts in the bank
    private BankAccount[] accounts = new BankAccount[maxAccounts];  // Array to hold BankAccount objects
    private BankAccount loggedInAccount = null;         // Currently logged-in account ('null' if no one is logged in)

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
    // by calling the deposit method of the BankAccount object
    public boolean deposit(int amount)
    {
        if (loggedIn()) {
            return loggedInAccount.deposit(amount);
        } else {
            return false;
        }
    }


    // Attempt to withdraw money from the currently logged-in account
    // by calling the withdraw method of the BankAccount object
    public boolean withdraw(int amount)
    {
        if (loggedIn()) {
            return loggedInAccount.withdraw(amount);
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

    // 检查账户是否存在
    public boolean accountValid(String accNumber) {
        for (int i = 0; i < numAccounts; i++) {
            if (accounts[i].getAccNumber().equals(accNumber)) {
                return true;
            }
        }
        return false;
    }

    // 向指定账户存款
    public boolean depositTo(String targetAccount, int amount) {
        for (int i = 0; i < numAccounts; i++) {
            if (accounts[i].getAccNumber().equals(targetAccount)) {
                return accounts[i].deposit(amount);
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


}