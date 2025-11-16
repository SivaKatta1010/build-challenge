package pc001.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import pc001.buffer.SharedBuffer;
import pc001.buffer.SharedBufferWaitNotify;
import pc001.buffer.SharedBufferInterface;
import pc001.observer.Manager;
import pc001.observer.QueueObserver;

/**
 * Main harness for the PC-001 producer/consumer demo. Supports two modes:
 * "blocking" (ArrayBlockingQueue) and "wait" (synchronized wait/notify).
 *
 * Usage: java -cp . pc001.core.MainPC001 [mode] [numProducers] [numConsumers]
 * [-v]
 */
public class MainPC001 {
    public static void main(String[] args) throws InterruptedException {
        // usage: [mode] [numProducers] [numConsumers]
        // parse args but allow -v/--verbose anywhere; remaining positional args are
        // mode, numProducers, numConsumers
        java.util.List<String> positional = new java.util.ArrayList<>();
        for (String a : args) {
            if ("-v".equals(a) || "--verbose".equals(a)) {
                pc001.Config.setVerbose(true);
            } else {
                positional.add(a);
            }
        }

        // mode: "blocking" (default) or "wait"
        String mode = (positional.size() > 0) ? positional.get(0) : "blocking";
        int numProducers = (positional.size() > 1) ? Integer.parseInt(positional.get(1)) : 1;
        int numConsumers = (positional.size() > 2) ? Integer.parseInt(positional.get(2)) : 1;

        List<Integer> sourceContainer = Arrays.asList(1, 2, 3, 4, 5);
        // make destination thread-safe to be explicit about concurrency
        List<Integer> destinationContainer = Collections.synchronizedList(new ArrayList<>());

        int capacity = 2;
        int sentinel = -1;

        SharedBufferInterface<Integer> buffer;
        if ("wait".equalsIgnoreCase(mode)) {
            System.out.println("Using wait/notify implementation");
            buffer = new SharedBufferWaitNotify<>(capacity);
        } else {
            System.out.println("Using BlockingQueue implementation");
            buffer = new SharedBuffer<>(capacity);
        }

        // register a simple queue observer (demonstrates Manager/Observer idea)
        if (pc001.Config.isVerbose()) {
            Manager.getInstance().addObserver("Q", new QueueObserver());
        }

        // Save initial snapshot
        Manager.getInstance().saveSnapshot("start", buffer.snapshotContents(), destinationContainer);

        // Partition source among producers
        List<List<Integer>> parts = new ArrayList<>();
        for (int i = 0; i < numProducers; i++)
            parts.add(new ArrayList<>());
        for (int i = 0; i < sourceContainer.size(); i++) {
            parts.get(i % numProducers).add(sourceContainer.get(i));
        }

        List<Thread> producers = new ArrayList<>();
        for (int i = 0; i < numProducers; i++) {
            producers.add(new Thread(new Producer(buffer, parts.get(i))));
        }

        List<Thread> consumers = new ArrayList<>();
        for (int i = 0; i < numConsumers; i++) {
            consumers.add(new Thread(new Consumer(buffer, destinationContainer, sentinel)));
        }

        // start consumers then producers
        consumers.forEach(Thread::start);
        producers.forEach(Thread::start);

        // wait for producers
        for (Thread p : producers)
            p.join();

        // signal consumers to stop: send one sentinel per consumer
        for (int i = 0; i < numConsumers; i++) {
            buffer.put(sentinel);
        }

        // wait for consumers
        for (Thread c : consumers)
            c.join();

        // Save final snapshot
        Manager.getInstance().saveSnapshot("end", buffer.snapshotContents(), destinationContainer);

        System.out.println("Destination container: " + destinationContainer);
    }
}
