package pc001.buffer;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import pc001.observer.Manager;
import java.util.concurrent.atomic.AtomicLong;
import java.util.List;

/**
 * ArrayBlockingQueue-backed buffer implementation. This class demonstrates use of
 * java.util.concurrent.BlockingQueue (put/take) which provide built-in blocking
 * semantics for producer/consumer scenarios.
 */
public class SharedBuffer<T> implements SharedBufferInterface<T> {
    private final BlockingQueue<T> queue;
    private final AtomicLong produced = new AtomicLong(0);
    private final AtomicLong consumed = new AtomicLong(0);

    public SharedBuffer(int capacity) {
        this.queue = new ArrayBlockingQueue<>(capacity);
    }

    public void put(T item) throws InterruptedException {
        queue.put(item);
        produced.incrementAndGet();
        Manager.getInstance().notifyChange("Q", this);
    }

    public T take() throws InterruptedException {
        T item = queue.take();
        consumed.incrementAndGet();
        Manager.getInstance().notifyChange("Q", this);
        return item;
    }

    @Override
    public List<T> snapshotContents() {
        // Return a typed snapshot safely by copying the queue contents
        return new java.util.ArrayList<>(queue);
    }

    @Override
    public int size() {
        return queue.size();
    }

    @Override
    public long getProducedCount() { return produced.get(); }

    @Override
    public long getConsumedCount() { return consumed.get(); }
}
