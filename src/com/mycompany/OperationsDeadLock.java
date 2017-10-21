package com.mycompany;
/*
* Реализация DeadLock
* */

public class OperationsDeadLock {
    public static void main(String[] args) {
        final Account a = new Account(1000, "acc1");
        final Account b = new Account(2000, "acc2");

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    transfer(a,b,500);
                } catch (InsufficientFundsException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        try {
            transfer(b,a,300);
        } catch (InsufficientFundsException e) {
            e.printStackTrace();
        }
    }

    static void transfer(Account acc1, Account acc2, int amount) throws InsufficientFundsException{

        System.out.println("trying to lock " + acc1.getName());
        synchronized (acc1) {
            System.out.println("locked: " + acc1.getName());
            try {
                Thread.sleep(1000);

                System.out.println("trying to lock " + acc2.getName());
                synchronized (acc2) {
                    System.out.println("locked: " + acc2.getName());
                    acc1.withdraw(amount);
                    acc2.deposit(amount);
                }
                System.out.println("unlocked: " + acc2.getName());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("unlocked: " + acc1.getName());
        System.out.println("transfer " + amount + "successful.");
    }
}
