package com.mycompany;


import java.util.concurrent.TimeUnit;

/*
* Использование интерфейса Lock для синхронизации трасфера между аккаунтами
* Здесь мы можем избежать дэдлока за счет использования таймаута, т.е времени
* в течение которого, будет осуществляться возможность захватить замок
* и в случае неудачной попытки будем отваливаться по таймауту.
* */

public class OperationsTryLock {
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

        System.out.println("trying to lock " + acc1.getName() + " " + Thread.currentThread().getName());
        try {
            if (acc1.getLock().tryLock(10, TimeUnit.SECONDS)){
                if (acc1.getBalance() < amount){
                    throw new InsufficientFundsException("Insufficient funds at " + acc1.getName() + " to transfer " + acc2.getName());
                }
                System.out.println("locked: " + acc1.getName() + " " + Thread.currentThread().getName());
                try {
//                    Thread.sleep(1000);
                    System.out.println("trying to lock " + acc2.getName() + " " + Thread.currentThread().getName());
                    if (acc2.getLock().tryLock(10, TimeUnit.SECONDS)) {
                        try {
                            System.out.println("locked: " + acc2.getName() + " " + Thread.currentThread().getName());
                            acc1.withdraw(amount);
                            acc2.deposit(amount);
                            System.out.println("transfer " + amount + "successful.");
                        }
                        finally {
                            acc2.getLock().unlock();
                            System.out.println("unlocked: " + acc2.getName() + " " + Thread.currentThread().getName());
                        }
                    } else{
                        acc2.incFailedTransferCounter();
                        System.err.println("Can't do transfer " + Thread.currentThread().getName());
                    }

                } finally {
                    acc1.getLock().unlock();
                    System.out.println("unlocked: " + acc1.getName() + " " + Thread.currentThread().getName());
                }
            } else{
                acc1.incFailedTransferCounter();
                System.err.println("Can't do transfer " + Thread.currentThread().getName());
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}


