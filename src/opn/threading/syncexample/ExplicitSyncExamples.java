package opn.threading.syncexample;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Class that collects a few examples of using explicit synchronization:
 * (1) Starting threads (thread objects and data streams)
 * (2) Synchronized methods (method-level object locking)
 * (3) Synchronized blocks (fine-grained locking)
 * (4) Thread coordination (wait/notify)
 *
 * @author ups
 */
@SuppressWarnings("WeakerAccess")
public class ExplicitSyncExamples {

    //
    // (1) Starting threads
    //

    // private member that only is accessed from synchronized methods
    private final List<Integer> sharedData1 = new ArrayList<>();
    // private member that is only accessed locally and only from synchronized blocks
    private final List<Integer> sharedData2 = new ArrayList<>();
    // private member that is only accessed locally and only in a synchronized fashion
    private final List<Integer> sharedData3 = new ArrayList<>();

    //
    // (2) Protect shared data using synchronized methods
    //

    public static void main(String argv[]) throws InterruptedException {
        ExplicitSyncExamples self = new ExplicitSyncExamples();
        List<String> whos = Arrays.asList("John", "Jack", "Jill", "Jane", "Bill", "Bob");
        System.out.println("start_one_thread_and_wait_for_finish");
        self.start_one_thread_and_wait_for_finish("John");
        System.out.println("example_do_things_sequentially");
        self.example_do_things_sequentially(whos);
        System.out.println("example_potentially_start_many_threads");
        self.example_potentially_start_many_threads(whos);
        System.out.println("work_using_protected_accessor");
        self.work_using_protected_accessor(10000);
        System.out.println("work_using_synchronized_block");
        self.work_using_synchronized_block(10000);
        System.out.println("work_using_wait_and_notify");
        self.work_using_wait_and_notify(10000);
    }

    // Start one thread, let it work, and (in this case) wait for it to finish
    public void start_one_thread_and_wait_for_finish(String who) throws InterruptedException {
        Thread t = new Thread(() -> System.out.println("Hello, " + who));
        t.start();
        System.out.println("Hello, anyone!"); // in parallel with other print statement
        t.join(); // wait for it to finish (not required, just for this example)
    }

    // Streaming of data with sequential processing (no new threads are started)
    public void example_do_things_sequentially(Collection<String> whos) {
        whos.forEach(who -> System.out.println("Hello, " + who));
    }

    // Streaming of data with parallel processing (as many threads as the JVM wants)
    public void example_potentially_start_many_threads(Collection<String> whos) {
        whos.parallelStream().forEach(who -> System.out.println("Hello, " + who));
    }

    //
    // (3) Protect data using a synchronized block (synchronizing on data, not "this")
    //

    // one or more synchronized methods for manipulating shared data
    public synchronized void addData(int x) {
        sharedData1.add(x);
    }

    public synchronized void removeData(int x) {
        int index = sharedData1.indexOf(x);
        if (index > -1) sharedData1.remove(index);
    }

    //
    // (4) Coordinate threads using wait/notifyAll
    //

    // methods in this class and outside this class always use accessor methods
    public void work_using_protected_accessor(int max) throws InterruptedException {
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < max; i++) addData(i);
        });
        Thread t2 = new Thread(() -> {
            for (int i = 0; i < max; i++) removeData(i);
        });
        t2.start();
        t1.start();
        t1.join();
        t2.join();
        System.out.println("(2) = " + sharedData1.size());
    }

    // working with shared data always done using synchronized blocks
    public void work_using_synchronized_block(int max) throws InterruptedException {
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < max; i++)
                synchronized (sharedData2) {
                    sharedData2.add(i);
                }
        });
        Thread t2 = new Thread(() -> {
            for (int i = 0; i < max; i++)
                synchronized (sharedData2) {
                    int index = sharedData2.indexOf(i);
                    if (index > -1) sharedData2.remove(index);
                }
        });
        t2.start();
        t1.start();
        t1.join();
        t2.join();
        System.out.println("(3) = " + sharedData2.size());
    }


    //
    // Main method for running
    //

    // working with shared data always done using synchronized blocks
    public void work_using_wait_and_notify(int max) throws InterruptedException {
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < max; i++)
                synchronized (sharedData3) {
                    sharedData3.add(i);
                    sharedData3.notifyAll();
                }
        });
        Thread t2 = new Thread(() -> {
            for (int i = 0; i < max; i++)
                synchronized (sharedData3) {
                    int index;
                    while (true) {
                        index = sharedData3.indexOf(i);
                        if (index > -1) break;
                        try {
                            sharedData3.wait();
                        } catch (InterruptedException e) {
                            throw new Error("Unexpected interruption");
                        }
                    }
                    sharedData3.remove(index);
                }
        });
        t2.start();
        t1.start();
        t1.join();
        synchronized (sharedData3) {
            System.out.println("(4) = " + sharedData3.size());
        }
    }
}
