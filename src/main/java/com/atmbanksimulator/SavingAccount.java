package com.atmbanksimulator;

// ===== SavingAccount (extends BankAccount) =====
//
// A savings account that accumulates interest over time.
// Features:
//   - Fixed annual interest rate: INTEREST_RATE (e.g. 3 %)
//   - applyInterest() computes and deposits interest on the current balance.
//   - Withdrawal restriction: a minimum balance (MIN_BALANCE, e.g. 50) must be
//     maintained after any withdrawal.
//
// Demonstrates: inheritance, method overriding, adding new behaviour.
public class    SavingAccount extends BankAccount {

    // Annual interest rate as a percentage (e.g. 3 means 3 %)
    private static final int INTEREST_RATE = 3;

    // Minimum balance that must remain in the account after a withdrawal
    private static final int MIN_BALANCE = 50;

    // Running total of interest that has been applied so far (informational)
    private int totalInterestEarned = 0;

    public SavingAccount(String accNumber, String accPasswd, int balance) {
        super(accNumber, accPasswd, balance);
    }

    // Returns the annual interest rate for display purposes
    public int getInterestRate() {
        return INTEREST_RATE;
    }

    // Returns the minimum balance requirement for display purposes
    public int getMinBalance() {
        return MIN_BALANCE;
    }

    // Returns the total interest credited to this account so far
    public int getTotalInterestEarned() {
        return totalInterestEarned;
    }

    // Applies the fixed annual interest to the current balance.
    // Interest = floor(balance * INTEREST_RATE / 100).
    // The computed interest is deposited using the parent deposit() method.
    // Returns the amount of interest that was applied.
    public int applyInterest() {
        int interest = (getBalance() * INTEREST_RATE) / 100;
        if (interest > 0) {
            super.deposit(interest);
            totalInterestEarned += interest;
        }
        return interest;
    }

    // Override withdraw to enforce the minimum balance requirement.
    // Returns false (and does NOT deduct) if the withdrawal would leave
    // the balance below MIN_BALANCE, in addition to the usual BankAccount checks.
    @Override
    public boolean withdraw(int amount) {
        if (getBalance() - amount < MIN_BALANCE) {
            return false;   // would breach minimum balance
        }
        return super.withdraw(amount);
    }
}
