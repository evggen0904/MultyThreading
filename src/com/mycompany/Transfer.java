package com.mycompany;

import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("ALL")
public class Transfer implements Callable<Boolean> {
    private Account acc1;
    private Account acc2;
    private int amount;
    private final int WAIT_SEC = 2;
    private CountDownLatch cdl;
    private Random random;

    public Transfer(Account accountFrom, Account accountTo, int amount){
        this.acc1 = accountFrom;
        this.acc2 = accountTo;
        this.amount = amount;
        this.random = new Random();
    }

    public Transfer(Account accountFrom, Account accountTo, int amount, CountDownLatch cdl){
        this.acc1 = accountFrom;
        this.acc2 = accountTo;
        this.amount = amount;
        this.cdl = cdl;
        this.random = new Random();
    }

    @Override
    public Boolean call() throws Exception {
//        if (cdl != null)
//            cdl.await();
        while(true) {
            if (acc1.getLock().tryLock(WAIT_SEC, TimeUnit.SECONDS)) {
                try {
                    if (acc1.getBalance() < amount) {
                        acc1.incFailedTransferCounter();
                        cdl.countDown();
//                        System.out.println("Insufficient funds at " + acc1.getName() +
//                                " to transfer " + amount + " to " + acc2.getName());
                        throw new InsufficientFundsException("Insufficient funds at " + acc1.getName() +
                                " to transfer " + amount + " to " + acc2.getName());
                    }
                    if (acc2.getLock().tryLock(WAIT_SEC, TimeUnit.SECONDS)) {
                        try {
                            acc1.withdraw(amount);
                            acc2.deposit(amount);
                            //        рандомная задержка на трансфер для того, чтобы была приближенность к реальной системе
                            int sleep = (0 + random.nextInt(2)) * 1000;
                            try {
                                Thread.sleep(sleep);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            System.out.println("transfer " + amount + " successful from " +
                                    acc1.getName() + " to " + acc2.getName());
                            cdl.countDown();

                            return true;
                        } finally {
                            acc2.getLock().unlock();
                        }
                    } else {
                        acc2.incFailedTransferCounter();
//                        return false;
                    }

                } finally {
                    acc1.getLock().unlock();
                }
            } else {
                acc1.incFailedTransferCounter();
//                return false;
            }
        }
    }
}
