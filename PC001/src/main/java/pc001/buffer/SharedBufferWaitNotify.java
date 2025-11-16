package pc001.buffer;

import java.util.LinkedList;
import java.util.Queue;
import pc001.observer.Manager;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Wait/notify based buffer implementation.
 *
 * This class demonstrates the low-level monitor pattern using `synchronized`,
 * `wait()` and `notifyAll()` to coordinate producers and consumers.
 */
public class SharedBufferWaitNotify<T> implements SharedBufferInterface<T> {
    private final Queue<T> queue = new LinkedList<>();
    private final int capacity;
    private final AtomicLong produced = new AtomicLong(0);
    private final AtomicLong consumed = new AtomicLong(0);

    public SharedBufferWaitNotify(int capacity) {
        this.capacity = capacity;
    }

    public synchronized void put(T item) throws InterruptedException {
        while (queue.size() == capacity) {
            wait();
        }
        queue.add(item);
        produced.incrementAndGet();
        notifyAll();
        Manager.getInstance().notifyChange("Q", this);
    }

    public synchronized T take() throws InterruptedException {
        while (queue.isEmpty()) {
            wait();
        }
        T item = queue.remove();
        consumed.incrementAndGet();
        notifyAll();
        Manager.getInstance().notifyChange("Q", this);
        return item;
    }

    @Override
    public List<T> snapshotContents() {
        synchronized (this) {
            return new ArrayList<>(queue);
        }
    }

    @Override
    public int size() {
        synchronized (this) {
            return queue.size();
        }
    }

    @Override
    public long getProducedCount() { return produced.get(); }

    @Override
    public long getConsumedCount() { return consumed.get(); }
}
