package pc001;

import pc001.buffer.SharedBuffer;
import pc001.buffer.SharedBufferWaitNotify;
import pc001.buffer.SharedBufferInterface;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * JUnit 5 port of the original TestSharedBufferSuite.
 */
public class TestSharedBufferJUnit {
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
                for (Integer v : items) buffer.put(v);
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
                    if (v != null && v.equals(SENTINEL)) break;
                    consumed.add(v);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private static void runAndAssertCommon(Supplier<SharedBufferInterface<Integer>> supplier,
                                           int numProducers,
                                           int numConsumers,
                                           int maxValue) throws InterruptedException {
        SharedBufferInterface<Integer> buffer = supplier.get();

        Collection<Integer> consumed = new ConcurrentLinkedQueue<>();

        List<Thread> producers = new ArrayList<>();
        List<Thread> consumers = new ArrayList<>();

        List<Integer> all = IntStream.rangeClosed(1, maxValue).boxed().collect(Collectors.toList());
        int per = (maxValue + numProducers - 1) / numProducers;
        for (int i = 0; i < numProducers; i++) {
            int start = i * per;
            int end = Math.min(start + per, all.size());
            List<Integer> part = all.subList(start, end);
            producers.add(new Thread(new Producer(buffer, new ArrayList<>(part)), "P-" + i));
        }

        for (int i = 0; i < numConsumers; i++) consumers.add(new Thread(new Consumer(buffer, consumed), "C-" + i));

        consumers.forEach(Thread::start);
        producers.forEach(Thread::start);

        for (Thread p : producers) p.join();

        for (int i = 0; i < numConsumers; i++) buffer.put(SENTINEL);

        for (Thread c : consumers) c.join();

        Set<Integer> producedSet = IntStream.rangeClosed(1, maxValue).boxed().collect(Collectors.toSet());
        Set<Integer> consumedSet = new HashSet<>(consumed);

        assertEquals(producedSet, consumedSet, "Produced and consumed sets must match");
    }

    @Test
    public void testSharedBufferImplementations() throws Exception {
        int maxValue = 50;
        int p = 2, c = 2;

        runAndAssertCommon(() -> new SharedBuffer<>(16), p, c, maxValue);
        runAndAssertCommon(() -> new SharedBufferWaitNotify<>(16), p, c, maxValue);
    }

    @Test
    public void testEmptyBuffer() throws Exception {
        SharedBufferInterface<Integer> buffer = new SharedBuffer<>(1);
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
        Thread.sleep(200);
        buffer.put(SENTINEL);
        consumer.join(1000);

        assertFalse(consumer.isAlive(), "Consumer should have terminated");
        assertTrue(consumed.isEmpty(), "No items should be consumed in emptyBuffer test");
    }

    @Test
    public void testFullBufferBlocking() throws Exception {
        SharedBufferInterface<Integer> buffer = new SharedBuffer<>(1);
        Collection<Integer> consumed = new ConcurrentLinkedQueue<>();

        Thread producer = new Thread(() -> {
            try {
                buffer.put(1);
                buffer.put(2);
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
        Thread.sleep(100);
        consumer.start();
        producer.join(2000);
        consumer.join(2000);

        assertTrue(consumed.contains(1) && consumed.contains(2), "Consumer should receive both values");
    }

    @Test
    public void testInterruptedProducer() throws Exception {
        SharedBufferInterface<Integer> buffer = new SharedBuffer<>(1);

        Thread producer = new Thread(() -> {
            try {
                buffer.put(1);
                buffer.put(2);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        producer.start();
        Thread.sleep(100);
        producer.interrupt();
        producer.join(1000);

        assertFalse(producer.isAlive(), "Producer should not be alive after interruption");
    }
}
