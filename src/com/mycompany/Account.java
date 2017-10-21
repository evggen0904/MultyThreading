package com.mycompany;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Account {
    private int balance;
    private String name;
    private Lock lock;
    private AtomicInteger failCounter;

    public Account(int balance, String name){
        this.balance = balance;
        this.name = name;
        lock = new ReentrantLock();
        failCounter = new AtomicInteger(0);
    }

    public void withdraw(int amount){
        balance -= amount;
    }

    public void deposit(int amount){
        balance += amount;
    }

    public int getBalance(){
        return balance;
    }

    public String getName(){
        return name;
    }

    public Lock getLock() {
        return lock;
    }

    public void incFailedTransferCounter(){
        failCounter.getAndIncrement();
    }

    public int getFailCounter() {
        return failCounter.get();
    }
}
