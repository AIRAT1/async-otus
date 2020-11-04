package de;

import java.util.concurrent.*;

import static java.lang.Thread.sleep;

public class Main {
    private int result;

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Main main = new Main();
//        main.promiseTestInc();
//        main.twoFutures();
//        main.testThenCombine();
        main.getSlow()
                .thenApply(r -> incSlow(r))
                .thenAccept(System.out::println);
        main.getSlow()
                .thenCompose(r -> this.incSlow(r))
                .thenAccept(System.out::println);
    }

    public Integer slowInit() {
        System.out.println("Started task slowInit");
        try {
            sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return 1;
    }

    public Integer slowIncrement(Integer i) {
        try {
            sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("finished slowIncrement with result " + i++);
        return i++;
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

    public void promiseTestInc() throws ExecutionException, InterruptedException {
        long start = System.nanoTime();
        CompletableFuture<Void> future =
                CompletableFuture
                .supplyAsync(this::slowInit)
                .thenApply(this::slowIncrement)
                .thenApply(this::slowIncrement)
                .thenAccept(res -> System.out.println("async result " + res));
        future.get();
        long elapsedTime = System.nanoTime() - start;
        System.out.printf("%d sec passed", TimeUnit.NANOSECONDS.toSeconds(elapsedTime));
    }

    public void twoFutures() throws ExecutionException, InterruptedException {
        CompletableFuture<Integer> future1 =
                CompletableFuture
                        .supplyAsync(this::slowInit)
                        .thenApply(this::slowIncrement);
        CompletableFuture<Integer> future2 =
                CompletableFuture
                .supplyAsync(this::slowInit);
        Integer res1 = future1.get();
        Integer res2 = future2.get();
        Integer res0 = slowInit();
        System.out.println("tasks are finished with results: " + res0 + ", " + res1 + ", " + res2);
    }

    public void testThenCombine() throws ExecutionException, InterruptedException {
        CompletableFuture<Integer> future1 =
                CompletableFuture.supplyAsync(this::slowInit)
                .thenApply(this::slowIncrement);
        CompletableFuture<Integer> future2 =
                CompletableFuture.supplyAsync(this::slowInit)
                .thenApply(this::slowIncrement);
        CompletableFuture<?> future3 = future1
                .thenCombine(future2, (x, y) -> x + y);
        System.out.println("result " + future3.get());
    }

    public CompletableFuture<Integer> getSlow() throws InterruptedException {
        sleep(1000);
        return CompletableFuture.completedFuture(1);
    }

    public CompletableFuture<Integer> incSlow(int i) throws InterruptedException {
        sleep(1000);
        return CompletableFuture.completedFuture(i++);
    }
}
