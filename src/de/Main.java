package de;

import java.util.concurrent.*;

public class Main {
    private int result;

    public static void main(String[] args) {

    }

    public Integer slowInit() {
        System.out.println("Started task slowInit");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return 1;
    }

    public void testFutureOldStyle() throws InterruptedException { // 1 Java
        Thread t = new Thread() {
            @Override
            public void run() {
                result = slowInit();
            }
        };
        t.start();
        t.join();
        System.out.println("testFutureOldStyle is finished " + result);
    }

    public void futureTest() throws ExecutionException, InterruptedException { // 5 Java
        Callable<Integer> r = this::slowInit;
        ExecutorService es = Executors.newFixedThreadPool(10);
        Future<Integer> future = es.submit(r);
        Integer res = future.get();
        System.out.println("futureTest is finished " + res);
    }

    public void promiseTest() throws ExecutionException, InterruptedException { // 8 Java
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(this::slowInit);
        Integer res = future.get();
        System.out.println("promiseTest " + res);
    }

    public void promiseTestNext() throws ExecutionException, InterruptedException {
        CompletableFuture<Void> future =
                CompletableFuture
                .supplyAsync(this::slowInit)
                .thenAccept(
                        (res) -> {
                            System.out.println("finished " + res);
                        }
                )
                .thenRun(
                        () -> {
                            System.out.println("look at result");
                        }
                );
        future.get();
        System.out.println("promiseTestNext is finished");
    }
}
