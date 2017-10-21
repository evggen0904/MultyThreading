package com.mycompany;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

/*
* Используем ExecutorService для создания пула потоков.
* Метод get() объекта типа Future возвращает результат выполнения потока описанный
 * в методе call() класса Transfer, реализующего интрефейс callable
 * Метод get() заставляет дожидаться исполнения потока
* */

@SuppressWarnings("ALL")
public class OperationsExecutorService {
    public static void main(String[] args) {
        final Account acc1 = new Account(1000, "acc1");
        final Account acc2 = new Account(2000, "acc2");
        Random random = new Random();
        List<Transfer> transfers = new ArrayList<>();

        ScheduledExecutorService wrongMonitoring = monitoringWrongTransaction(acc1);
        ExecutorService service = Executors.newFixedThreadPool(3);
        for (int i = 0; i < 10; i++) {
            transfers.add(new Transfer(acc1, acc2, random.nextInt(600)));
        }

        try {
            List<Future<Boolean>> result = service.invokeAll(transfers);
            int success = 0;
            for (Future<Boolean> res : result) {
                try {
                    if (res.get())
                        success++;
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }

            System.out.println("Success transactions: " + success);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        shutdown(service, "MainExecutor");
        shutdown(wrongMonitoring, "MonitoringExecutor");
        System.out.println("Acc1: " + acc1.getBalance());
        System.out.println("Acc2: " + acc2.getBalance());

    }

    public static ScheduledExecutorService monitoringWrongTransaction(Account acc){
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        ScheduledFuture f = executorService.scheduleAtFixedRate(
                new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("===================================");
                        System.out.println("Wrong transactions: " + acc.getFailCounter());
                        System.out.println("===================================");
                    }
                },
                2,
                2,
                TimeUnit.SECONDS
        );

        return executorService;
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
