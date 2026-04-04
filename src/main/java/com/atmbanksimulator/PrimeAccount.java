package com.atmbanksimulator;

// ===== PrimeAccount (extends BankAccount) =====
//
//  Premium account with overdraft support and a higher single-transaction limit.
// Features:
//   - Overdraft allowance: balance may go negative up to OVERDRAFT_LIMIT (e.g. 500)
//   - Higher per-transaction withdrawal ceiling: WITHDRAWAL_LIMIT (e.g. 1000)
//
// Demonstrates: inheritance, method overriding, extending base-class behaviour.
public class PrimeAccount extends BankAccount {

    // How far below zero the balance is allowed to go
    private static final int OVERDRAFT_LIMIT = 500;

    // Maximum single-transaction withdrawal (higher than a standard account)
    private static final int WITHDRAWAL_LIMIT = 1000;

    public PrimeAccount(String accNumber, String accPasswd, int balance) {
        super(accNumber, accPasswd, balance);
    }

    // Returns the overdraft limit for display purposes
    public int getOverdraftLimit() {
        return OVERDRAFT_LIMIT;
    }

    // Returns the per-transaction withdrawal limit for display purposes
    public int getWithdrawalLimit() {
        return WITHDRAWAL_LIMIT;
    }

    // Override withdraw to allow overdraft up to OVERDRAFT_LIMIT and enforce
    // the higher per-transaction ceiling.
    // Returns false (and does NOT deduct) if:
    //   - amount is negative
    //   - amount exceeds WITHDRAWAL_LIMIT
    //   - the withdrawal would push the balance below -OVERDRAFT_LIMIT
    @Override
    public boolean withdraw(int amount) {
        if (amount < 0) {
            return false;
        }
        if (amount > WITHDRAWAL_LIMIT) {
            return false;   // per-transaction ceiling exceeded
        }
        // Allow balance to go down to -OVERDRAFT_LIMIT
        if (getBalance() - amount < -OVERDRAFT_LIMIT) {
            return false;   // would exceed overdraft allowance
        }
        // Use parent logic for the subtraction, but we must handle the case where
        // balance < amount (which the parent rejects). We call deposit/withdraw
        // manually to keep balance private access consistent.
        // Strategy: if within overdraft range but below zero, bypass parent check
        // by depositing the overdraft portion first, then withdrawing.
        int currentBalance = getBalance();
        if (currentBalance < amount) {
            // We are going into (or deeper into) overdraft territory.
            // Deposit the shortfall so the parent withdraw succeeds, then withdraw.
            int shortfall = amount - currentBalance;
            super.deposit(shortfall);   // temporarily top up
        }
        return super.withdraw(amount);
    }
}
