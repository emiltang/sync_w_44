package opn.threading.pleasesync;

import open.threading.util.ParanoidCollections;

import java.util.LinkedList;
import java.util.List;

/**
 * Simple example of a deadlock, solve by changing the way synchronization is done
 * in the program.  Any solution that works is acceptable, but a solution that does
 * not use busy-waiting is clearly preferable.
 *
 * @author ups
 */
public class DeadlockingMain {

    private static int PROBLEM_SIZE = 1000;
    /**
     * Shared data structure, always accessed within code that synchronizes on the queue object
     */
    private final List<Integer> queue = ParanoidCollections.paranoidList(new LinkedList<Integer>());

    public static void main(String argv[]) {
        DeadlockingMain main = new DeadlockingMain();
        main.test_deadlock();
    }

    /**
     * Method that tests whether we can write deadlock-free code.
     * The method as-is does not work, it will almost always lock up before completing
     */
    private void test_deadlock() {

        // Thread 1: print any numbers in queue
        Thread t1 = new Thread(() -> {
            System.out.println("Started queue printer");
            for (int i = 0; i < PROBLEM_SIZE * 2; i++) {
                int number;
                synchronized (queue) { // Wait until queue contains a number, then print the first one
                    try {
                        while (queue.size() == 0)
                            queue.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    number = queue.remove(0);
                }
                System.out.println("Next number: " + number);
            }
        });

        // Thread 2: produce 100 numbers
        Thread t2 = new Thread(() -> {
            System.out.println("Started i*2 producer");
            for (int i = 0; i < PROBLEM_SIZE; i++) {
                int number = i * 2;
                synchronized (queue) {
                    queue.add(number);
                    queue.notifyAll();
                } // Insert number into queue
                System.out.println("Added " + number);
            }
        });

        // Thread 3: produce 100 more numbers
        Thread t3 = new Thread(() -> {
            System.out.println("Started i*i producer");
            for (int i = 0; i < PROBLEM_SIZE; i++) {
                int number = i * i;
                synchronized (queue) {
                    queue.add(i * i);
                    queue.notifyAll();
                } // Insert number into queue
                System.out.println("Added " + number);
            }
        });

        // Start all threads and wait for them to complete
        t1.start();
        t2.start();
        t3.start();
        try {
            t1.join();
            t2.join();
            t3.join();
        } catch (InterruptedException e) {
            throw new Error("Internal error: interrupted!");
        }

        // If we got here, we succeeded!
        System.out.println("Success: all threads completed");
    }

}
