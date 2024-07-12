import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class App {
    public static void main(String[] args) throws InterruptedException {
        ArrayList<Integer> digits = new ArrayList<>();
        int NUM = 1000;

        for(int i = 0; i < NUM; i++){
            digits.add(i);
        }
        java.util.Collections.shuffle(digits);

        TaskQueue taskQueue = new TaskQueue();

        for (int i = 0; i < digits.size(); i++){
            taskQueue.add(digits.get(i));
        }
        ResultTable resultTable = new ResultTable();
        ArrayList<Thread> threads = new ArrayList<>();
        int cores = Runtime.getRuntime().availableProcessors();

        long startTime = java.lang.System.currentTimeMillis();

        for (int i = 0; i < cores; i++){
            Thread thread = new Thread(new Worker(taskQueue, resultTable));
            thread.start();
            threads.add(thread);
        }

        for (int i = 0; i < threads.size(); i++){
            threads.get(i).join();
        }
        long endTime = java.lang.System.currentTimeMillis();

        String message = "";

        for (int i = 0; i < NUM; i++){
            message += resultTable.get(i);

            if (i == 0){
                message+= ".";
            }
        }
        System.out.println();
        System.out.println(message);

        Long deltaTime = endTime - startTime;

        System.out.println("Pi calculations took " + deltaTime + " ms.");
    }
}

class TaskQueue {
    private LinkedList<Integer> storage;

    TaskQueue(){
        storage = new LinkedList<>();
    }

    public void add(int digitPlace){
        storage.addLast(digitPlace);
    }

    public int take(){
        return storage.pollFirst();
    }

    public boolean isEmpty(){
        return storage.isEmpty();
    }
}

class ResultTable {
    private HashMap<Integer, Integer> storage;

    ResultTable() {
        storage = new HashMap<>();
    }

    public void add(int digPlace, int digValue) {
        storage.put(digPlace, digValue);
    }

    public int get(int key) {
        return storage.get(key);
    }
    public int size(){
        return storage.size();
    }
}

/**
 * This class was orginally written by feltocarig on GitHub:
 * https://github.com/feltocraig/BBP-Bellard/blob/master/Bpp.java
 *
 * I did this because I was too dumb to figure this out on my own so I improvised it to only return the nth digit.
 */
class Worker implements Runnable {
    private TaskQueue queue;
    private ResultTable results;

    public Worker(TaskQueue taskQueue, ResultTable resultTable){
        queue = taskQueue;
        results = resultTable;
    }

    public void run(){
        try {
            while(!queue.isEmpty()){
                int n = queue.take();
                results.add(n, getDecimal(n));

                if (results.size() % 10 == 0){
                    System.out.print(".");
                }
            }
        }
        catch (Exception e) {
            // Throwing an exception
            System.out.println ("Exception is caught");
        }
    }


    /**
     * Returns the nth digit of pi
     * @param n - nth number of pi to return
     * @return returns an integer value
     */
    private int getDecimal(long n) {
        if (n == 0){
            return 3;
        }
        long av, a, vmax, N, num, den, k, kq, kq2, t, v, s, i;
        double sum;

        N = (long) ((n + 20) * Math.log(10) / Math.log(2));

        sum = 0;

        for (a = 3; a <= (2 * N); a = nextPrime(a)) {
            vmax = (long) (Math.log(2 * N) / Math.log(a));
            av = 1;

            for (i = 0; i < vmax; i++) {
                av = av * a;
            }

            s = 0;
            num = 1;
            den = 1;
            v = 0;
            kq = 1;
            kq2 = 1;

            for (k = 1; k <= N; k++) {

                t = k;
                if (kq >= a) {
                    do {
                        t = t / a;
                        v--;
                    } while ((t % a) == 0);
                    kq = 0;
                }
                kq++;
                num = mulMod(num, t, av);

                t = (2 * k - 1);

                if (kq2 >= a) {
                    if (kq2 == a) {
                        do {
                            t = t / a;
                            v++;
                        } while ((t % a) == 0);
                    }
                    kq2 -= a;
                }
                den = mulMod(den, t, av);
                kq2 += 2;

                if (v > 0) {
                    t = modInverse(den, av);
                    t = mulMod(t, num, av);
                    t = mulMod(t, k, av);

                    for (i = v; i < vmax; i++) {
                        t = mulMod(t, a, av);
                    }
                    s += t;

                    if (s >= av) {
                        s -= av;
                    }
                }

            }

            t = powMod(10, n - 1, av);
            s = mulMod(s, t, av);
            sum = (sum + (double) s / (double) av) % 1;
        }
        return (int) (sum * 10); // 1e9 is 9 decimal places
    }

    private long mulMod(long a, long b, long m) {
        return (a * b) % m;
    }

    private long modInverse(long a, long n) {
        long i = n, v = 0, d = 1;
        while (a > 0) {
            long t = i / a, x = a;
            a = i % x;
            i = x;
            x = d;
            d = v - t * x;
            v = x;
        }
        v %= n;

        if (v < 0) {
            v = (v + n) % n;
        }
        return v;
    }

    private long powMod(long a, long b, long m) {
        long tempo;

        if (b == 0) {
            tempo = 1;
        }
        else if (b == 1) {
            tempo = a;
        }

        else {
            long temp = powMod(a, b / 2, m);
            if (b % 2 == 0) {
                tempo = (temp * temp) % m;
            }
            else {
                tempo = ((temp * temp) % m) * a % m;
            }
        }
        return tempo;
    }

    private boolean isPrime(long n) {
        if (n == 2 || n == 3) {
            return true;
        }
        if (n % 2 == 0 || n % 3 == 0 || n < 2) {
            return false;
        }

        long sqrt = (long) Math.sqrt(n) + 1;

        for (long i = 6; i <= sqrt; i += 6) {
            if (n % (i - 1) == 0) {
                return false;
            }
            else if (n % (i + 1) == 0) {
                return false;
            }
        }
        return true;
    }

    private long nextPrime(long n) {
        if (n < 2) {
            return 2;
        }
        if (n == 9223372036854775783L) {
            System.err.println("Next prime number exceeds Long.MAX_VALUE: " + Long.MAX_VALUE);
            return -1;
        }

        for (long i = n + 1;; i++) {
            if (isPrime(i))
                return i;
        }
    }
}
