package com.alan.empiresOfAlan.model.bank;

import java.util.UUID;

public class BankAccount {
    private final UUID ownerId;
    private double balance;

    public BankAccount(UUID ownerId) {
        this.ownerId = ownerId;
        this.balance = 0.0;
    }

    public BankAccount(UUID ownerId, double initialBalance) {
        this.ownerId = ownerId;
        this.balance = initialBalance;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public double getBalance() {
        return balance;
    }

    /**
     * Deposits money into the account
     * @param amount Amount to deposit
     * @return true if successful, false if amount is invalid
     */
    public boolean deposit(double amount) {
        if (amount <= 0) {
            return false;
        }

        this.balance += amount;
        return true;
    }

    /**
     * Withdraws money from the account
     * @param amount Amount to withdraw
     * @return true if successful, false if insufficient funds or invalid amount
     */
    public boolean withdraw(double amount) {
        if (amount <= 0) {
            return false;
        }

        if (balance < amount) {
            return false; // Insufficient funds
        }

        this.balance -= amount;
        return true;
    }

    /**
     * Transfers money to another account
     * @param destination The destination account
     * @param amount The amount to transfer
     * @return true if successful, false otherwise
     */
    public boolean transfer(BankAccount destination, double amount) {
        if (withdraw(amount)) {
            destination.deposit(amount);
            return true;
        }
        return false;
    }

    /**
     * Sets the balance to a specific amount
     * @param balance The new balance
     */
    public void setBalance(double balance) {
        this.balance = Math.max(0, balance);
    }

    /**
     * Checks if account has sufficient funds
     * @param amount Amount to check
     * @return true if sufficient, false otherwise
     */
    public boolean hasFunds(double amount) {
        return balance >= amount;
    }
}