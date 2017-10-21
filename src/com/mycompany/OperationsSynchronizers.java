package com.mycompany;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

@SuppressWarnings("ALL")
public class OperationsSynchronizers {

    public static void main(String[] args) {
        final Account acc1 = new Account(1000, "acc1");
        final Account acc2 = new Account(2000, "acc2");
        final Random random = new Random();
        final ExecutorService service = Executors.newCachedThreadPool();
        final List<Future<Boolean>> result = new ArrayList<>();
        final CountDownLatch cdl = new CountDownLatch(8);
        final CountDownLatch cdl2 = new CountDownLatch(5);
//        long time = 0;
//        System.out.println("Prepare transactions");
        System.out.println("Start transfering acc1->acc2");
        long time = System.nanoTime();
//        CyclicBarrier cb = new CyclicBarrier(8,new Runnable() {
//            @Override
//            public void run() {
//                System.out.println("Leed time: " + (System.nanoTime()-time));
//            }
//        });
        for (int i = 0; i < 8; i++) {
            Transfer t = new Transfer(acc1, acc2, random.nextInt(200), cdl);
            result.add(service.submit(t));
        }

        try {
            cdl.await();
            System.out.println("Summary time for transfers from acc1->acc2: " + (System.nanoTime()-time)/1000000 + " ms");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Stop transfering acc1->acc2");
        System.out.println("Acc1: " + acc1.getBalance());
        System.out.println("Acc2: " + acc2.getBalance());

        System.out.println("Start transfering acc2->acc1");
        for (int i = 0; i < 5; i++) {
            Transfer t = new Transfer(acc2, acc1, random.nextInt(600),cdl2);
            result.add(service.submit(t));
        }

//        System.out.println("Transactions are ready");
//        cdl.countDown();
//        System.out.println("Start transfering a->b");
        for (Future<Boolean> f: result) {
            try {
                f.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
   /*     try {
            cdl2.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        System.out.println("Stop transfering acc2->acc1");
        System.out.println("Stop transfering");
        shutdown(service, "MainExecutor");
        System.out.println("Acc1: " + acc1.getBalance());
        System.out.println("Acc2: " + acc2.getBalance());
    }

    public static void shutdown(ExecutorService service, String executorName){
        try {
            System.out.println("attempt to shutdown executor " + executorName);
            service.shutdown();

            service.awaitTermination(5, TimeUnit.SECONDS);
        }
        catch (InterruptedException e) {
            System.err.println("tasks interrupted for " + executorName);
        }
        finally {
            if (!service.isTerminated()) {
                System.err.println("cancel non-finished tasks for " + executorName);
            }
            service.shutdownNow();
            System.out.println("shutdown finished for " + executorName);
        }
    }
}
