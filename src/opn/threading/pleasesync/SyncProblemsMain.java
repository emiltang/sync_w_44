package opn.threading.pleasesync;

import open.threading.util.ConcurrentAccessException;
import open.threading.util.ParanoidCollections;

import java.util.ConcurrentModificationException;
import java.util.LinkedList;
import java.util.List;

/**
 * Class demonstrating various synchronization problems, for the purposes of this
 * exercise, they should *all* be solved using just the "synchronized" keyword
 *
 * @author ups
 */
public class SyncProblemsMain {

    private static final int COUNT_LIMIT = 100000; // size of problem
    private static final int LIST_MODIFY_LIMIT = 10000; // size of problem

    private static int LIST_TRAVERSE_LIMIT = 10000; // size of problem
    // Note: bodies of thread loops moved out into separate methods
    private final List<Integer> numbers = ParanoidCollections.paranoidList(new LinkedList<Integer>()); // used as a queue, must be a linked list
    private final List<Integer> data = ParanoidCollections.paranoidList(new LinkedList<Integer>()); // data shared between threads, traversed and modified
    private int value = 0; // shared between threads
    private boolean problem_2_failed = false; // set to true if failed
    private boolean problem_3_failed = false; // set to true if failed
    private int negative_hits = 0; // counter for how many modifications are found during traversal

    public static void main(String argv[]) {
        SyncProblemsMain main = new SyncProblemsMain();
        // Run each of the tests
        main.test_parallel_counting();
        main.test_parallel_modification();
        main.test_parallel_iteration();
    }

    /**
     * Helper method: start two threads and wait for them to finish
     */
    private void finish(Thread t1, Thread t2) {
        t1.start();
        t2.start();
        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            throw new Error("Internal error: interrupted!");
        }
    }


    ////////////////////////////////////////////
    // Problem 1:
    // Parallel counting without synchronization
    // Requirement: value==0
    ////////////////////////////////////////////

    private void test_parallel_counting() {
        // Define threads and run them
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < COUNT_LIMIT; i++) increment_value();
        }); // count up
        Thread t2 = new Thread(() -> {
            for (int i = 0; i < COUNT_LIMIT; i++) decrement_value();
        }); // count down
        finish(t1, t2);
        // What happened?
        System.out.println("After parallel counting to " + COUNT_LIMIT + ", value is " + value);
        if (value != 0) System.out.println("Problem 1 failed");
        else System.out.println("Problem 1 succeeded");
    }

    private synchronized void increment_value() {
        value++;
    }

    private synchronized void decrement_value() {
        value--;
    }

    //////////////////////////////////
    // Problem 2:
    // Concurrent modification of list
    // Requirement: no exceptions
    //////////////////////////////////

    private void test_parallel_modification() {
        // Define threads and run them
        Thread t1 = new Thread(() -> { // add new numbers to the list
            try {
                for (int i = 0; i < LIST_MODIFY_LIMIT; i++) {
                    synchronized (numbers) {
                        if (!numbers.contains(i)) numbers.add(i);
                    }
                }
            } catch (Exception exn) {
                System.out.println("Problem 2, thread 1 failed: " + exn);
                exn.printStackTrace();
                problem_2_failed = true;
            }
        });
        Thread t2 = new Thread(() -> { // remove numbers from the list
            try {
                for (int i = 0; i < LIST_MODIFY_LIMIT; i++) {
                    synchronized (numbers) {
                        if (numbers.size() > 0) numbers.remove(0);
                    }
                }
            } catch (Exception exn) {
                System.out.println("Problem 2, thread 2 failed: " + exn);
                exn.printStackTrace();
                problem_2_failed = true;
            }
        });
        finish(t1, t2);
        // What happened?
        System.out.println("Resulting list length: " + numbers.size() + " (max=" + LIST_MODIFY_LIMIT + ")");
        if (problem_2_failed) System.out.println("Problem 2 failed");
        else System.out.println("Problem 2 succeeded");
    }

    //////////////////////////////////////////
    // Problem 3:
    // Iteration of concurrently modified list
    // Requirement: no exceptions
    //////////////////////////////////////////

    private void test_parallel_iteration() {
        for (int i = 0; i < LIST_TRAVERSE_LIMIT; i++) data.add(i);
        // Define threads and run them
        Thread t1 = new Thread(() -> { // modify the list in just a few places
            synchronized (data) {
                for (int i = 0; i < LIST_TRAVERSE_LIMIT; i++) {
                    if (data.get(i) % (LIST_TRAVERSE_LIMIT / 3) == 0) {
                        data.add(0, -data.get(i));
                    }
                }
            }

        });
        Thread t2 = new Thread(() -> { // iterate the list
            try {
                synchronized (data) {
                    for (int i : data) {
                        if (i < 0) {
                            System.out.println("Found negative number: " + i);
                            negative_hits++;
                        }
                    }
                }
            } catch (ConcurrentModificationException | ConcurrentAccessException exn) {
                System.out.println("Problem 3, thread 2 failed: concurrent modification");
                exn.printStackTrace();
                problem_3_failed = true;
            }
        });
        finish(t1, t2);
        // What happened?
        System.out.println("#Negative hits: " + negative_hits);
        if (problem_3_failed) System.out.println("Problem 3 failed");
        else System.out.println("Problem 3 succeeded");
    }
}
