package pc001.core;

import java.util.List;
import pc001.buffer.SharedBufferInterface;

/**
 * Consumer runnable that takes items from the shared buffer and appends them
 * to a destination list. The consumer stops when it encounters the sentinel
 * value provided by the main harness.
 */
public class Consumer implements Runnable {
    private final SharedBufferInterface<Integer> buffer;
    private final List<Integer> destination;
    private final int sentinel;

    public Consumer(SharedBufferInterface<Integer> buffer, List<Integer> destination, int sentinel) {
        this.buffer = buffer;
        this.destination = destination;
        this.sentinel = sentinel;
    }

    @Override
    public void run() {
        try {
            while (true) {
                int value = buffer.take();
                if (value == sentinel) {
                    break;
                }
                destination.add(value);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
