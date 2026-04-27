package com.atmbanksimulator;

// ===== StudentAccount (extends BankAccount) =====
//
// A specialised account for students.
// Restrictions:
//   - Maximum single withdrawal: WITHDRAWAL_LIMIT (e.g. 200)
//   - Maximum total withdrawn in one day: DAILY_CAP (e.g. 300)
//
// Demonstrates: inheritance, method overriding, adding new state.
    public class StudentAccount extends BankAccount {

    // Maximum amount that can be withdrawn in a single transaction
    private static final int WITHDRAWAL_LIMIT = 200;

    // Maximum total amount that can be withdrawn per day
    private static final int DAILY_CAP = 300;

    // How much has been withdrawn today (resets via resetDailyWithdrawn())
    private int dailyWithdrawn = 0;

    public StudentAccount(String accNumber, String accPasswd, int balance) {
        super(accNumber, accPasswd, balance);
    }

    // Returns the per-transaction withdrawal limit for display purposes
    public int getWithdrawalLimit() {
        return WITHDRAWAL_LIMIT;
    }

    // Returns how much of today's daily cap has already been used
    public int getDailyWithdrawn() {
        return dailyWithdrawn;
    }

    // Returns the remaining daily cap amount
    public int getRemainingDailyCap() {
        return DAILY_CAP - dailyWithdrawn;
    }

    // Resets the daily withdrawal counter (call at start of new day)
    public void resetDailyWithdrawn() {
        dailyWithdrawn = 0;
    }

    // Returns "student" to identify this account type when saving to file
    @Override
    public String getType() {
        return "student";
    }

    // Override withdraw to enforce per-transaction limit and daily cap.
    // Returns false (and does NOT deduct) if:
    //   - amount exceeds WITHDRAWAL_LIMIT
    //   - amount would exceed the remaining daily cap
    //   - the usual BankAccount checks fail (negative amount, insufficient funds)
    @Override
    public boolean withdraw(int amount) {
        if (amount > WITHDRAWAL_LIMIT) {
            return false;   // single transaction limit exceeded
        }
        if (dailyWithdrawn + amount > DAILY_CAP) {
            return false;   // daily cap would be exceeded
        }
        boolean success = super.withdraw(amount);
        if (success) {
            dailyWithdrawn += amount;
        }
        return success;
    }
}
