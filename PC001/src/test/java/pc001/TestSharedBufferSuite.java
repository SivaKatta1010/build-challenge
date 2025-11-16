package pc001;

import pc001.buffer.SharedBuffer;
import pc001.buffer.SharedBufferWaitNotify;
import pc001.buffer.SharedBufferInterface;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Small test suite that exercises both buffer implementations without JUnit.
 * This lives in a separate test folder so it can be compiled/run independently.
 */
public class TestSharedBufferSuite {
    private static final int SENTINEL = -1;

    static class Producer implements Runnable {
        private final SharedBufferInterface<Integer> buffer;
        private final List<Integer> items;

        Producer(SharedBufferInterface<Integer> buffer, List<Integer> items) {
            this.buffer = buffer;
            this.items = items;
        }

        @Override
        public void run() {
            try {
                for (Integer v : items) {
                    buffer.put(v);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    static class Consumer implements Runnable {
        private final SharedBufferInterface<Integer> buffer;
        private final Collection<Integer> consumed;

        Consumer(SharedBufferInterface<Integer> buffer, Collection<Integer> consumed) {
            this.buffer = buffer;
            this.consumed = consumed;
        }

        @Override
        public void run() {
            try {
                while (true) {
                    Integer v = buffer.take();
                    if (v != null && v.equals(SENTINEL)) {
                        break;
                    }
                    consumed.add(v);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private static boolean runTest(Supplier<SharedBufferInterface<Integer>> supplier,
                                   String name,
                                   int numProducers,
                                   int numConsumers,
                                   int maxValue,
                                   int capacity) throws InterruptedException {
        System.out.printf("Running test for %s (producers=%d consumers=%d)\n", name, numProducers, numConsumers);

        SharedBufferInterface<Integer> buffer = supplier.get();

        Collection<Integer> consumed = new ConcurrentLinkedQueue<>();

        List<Thread> producers = new ArrayList<>();
        List<Thread> consumers = new ArrayList<>();

        // partition values 1..maxValue across producers
        List<Integer> all = IntStream.rangeClosed(1, maxValue).boxed().collect(Collectors.toList());
        int per = (maxValue + numProducers - 1) / numProducers;
        for (int i = 0; i < numProducers; i++) {
            int start = i * per;
            int end = Math.min(start + per, all.size());
            List<Integer> part = all.subList(start, end);
            Thread t = new Thread(new Producer(buffer, new ArrayList<>(part)), "P-" + i);
            producers.add(t);
        }

        for (int i = 0; i < numConsumers; i++) {
            Thread t = new Thread(new Consumer(buffer, consumed), "C-" + i);
            consumers.add(t);
        }

        // start
        consumers.forEach(Thread::start);
        producers.forEach(Thread::start);

        // wait for producers
        for (Thread p : producers) p.join();

        // send sentinels to stop consumers
        for (int i = 0; i < numConsumers; i++) {
            buffer.put(SENTINEL);
        }

        // wait for consumers
        for (Thread c : consumers) c.join();

        Set<Integer> producedSet = IntStream.rangeClosed(1, maxValue).boxed().collect(Collectors.toSet());
        Set<Integer> consumedSet = new HashSet<>(consumed);

        boolean ok = producedSet.equals(consumedSet);
        System.out.printf("%s => produced=%d consumed=%d result=%s\n", name, producedSet.size(), consumedSet.size(), ok ? "PASS" : "FAIL");
        if (!ok) {
            // print differences
            Set<Integer> missing = new HashSet<>(producedSet);
            missing.removeAll(consumedSet);
            Set<Integer> extra = new HashSet<>(consumedSet);
            extra.removeAll(producedSet);
            System.err.printf("Missing: %s\nExtra: %s\n", missing, extra);
        }
        return ok;
    }

    public static void main(String[] args) throws Exception {
        boolean allOk = true;

        // test sizes and threads -- chosen small so this runs quickly
        int maxValue = 50;
        int capacity = 16;
        int p = 2, c = 2;

        boolean s1 = runTest(() -> new SharedBuffer<>(capacity), "SharedBuffer", p, c, maxValue, capacity);
        boolean s2 = runTest(() -> new SharedBufferWaitNotify<>(capacity), "SharedBufferWaitNotify", p, c, maxValue, capacity);

        // run edge-case checks with small capacities
        boolean e1 = emptyBufferTest(() -> new SharedBuffer<>(1));
        boolean e2 = fullBufferBlockingTest(() -> new SharedBuffer<>(1));
        boolean e3 = interruptedProducerTest(() -> new SharedBuffer<>(1));

        boolean e4 = emptyBufferTest(() -> new SharedBufferWaitNotify<>(1));
        boolean e5 = fullBufferBlockingTest(() -> new SharedBufferWaitNotify<>(1));
        boolean e6 = interruptedProducerTest(() -> new SharedBufferWaitNotify<>(1));

        allOk = s1 && s2 && e1 && e2 && e3 && e4 && e5 && e6;

        if (allOk) {
            System.out.println("ALL TESTS PASSED");
            System.exit(0);
        } else {
            System.err.println("SOME TESTS FAILED");
            System.exit(2);
        }
    }

    // Additional edge-case tests
    private static boolean emptyBufferTest(Supplier<SharedBufferInterface<Integer>> supplier) throws InterruptedException {
        System.out.println("Running emptyBufferTest");
        SharedBufferInterface<Integer> buffer = supplier.get();
        Collection<Integer> consumed = new ConcurrentLinkedQueue<>();

        Thread consumer = new Thread(() -> {
            try {
                Integer v = buffer.take();
                if (v != null && v.equals(SENTINEL)) return;
                consumed.add(v);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        consumer.start();
        // allow consumer to block on empty buffer
        Thread.sleep(200);
        // now send sentinel to unblock
        buffer.put(SENTINEL);
        consumer.join(1000);
        boolean ok = !consumer.isAlive() && consumed.isEmpty();
        System.out.println("emptyBufferTest => " + (ok ? "PASS" : "FAIL"));
        return ok;
    }

    private static boolean fullBufferBlockingTest(Supplier<SharedBufferInterface<Integer>> supplier) throws InterruptedException {
        System.out.println("Running fullBufferBlockingTest");
        SharedBufferInterface<Integer> buffer = supplier.get();
        Collection<Integer> consumed = new ConcurrentLinkedQueue<>();

        Thread producer = new Thread(() -> {
            try {
                buffer.put(1);
                buffer.put(2); // should block until consumer takes
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        Thread consumer = new Thread(() -> {
            try {
                Thread.sleep(200);
                Integer a = buffer.take();
                Integer b = buffer.take();
                consumed.add(a);
                consumed.add(b);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        producer.start();
        // give producer time to fill buffer and block
        Thread.sleep(100);
        consumer.start();
        producer.join(2000);
        consumer.join(2000);
        boolean ok = consumed.contains(1) && consumed.contains(2);
        System.out.println("fullBufferBlockingTest => " + (ok ? "PASS" : "FAIL"));
        return ok;
    }

    private static boolean interruptedProducerTest(Supplier<SharedBufferInterface<Integer>> supplier) throws InterruptedException {
        System.out.println("Running interruptedProducerTest");
        SharedBufferInterface<Integer> buffer = supplier.get();

        Thread producer = new Thread(() -> {
            try {
                buffer.put(1);
                buffer.put(2); // will block on full buffer
            } catch (InterruptedException e) {
                // expected when interrupted
                Thread.currentThread().interrupt();
            }
        });

        // fill buffer to capacity if possible
        // try putting one item to cause the second put to block for small capacities
        producer.start();
        Thread.sleep(100);
        producer.interrupt();
        producer.join(1000);
        boolean ok = !producer.isAlive();
        System.out.println("interruptedProducerTest => " + (ok ? "PASS" : "FAIL"));
        return ok;
    }

}
