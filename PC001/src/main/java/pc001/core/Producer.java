package pc001.core;

import java.util.List;
import pc001.buffer.SharedBufferInterface;

/**
 * Producer runnable that takes a source list of integers and places them into
 * the shared buffer. The producer blocks on `put()` when the buffer is full.
 */
public class Producer implements Runnable {
    private final SharedBufferInterface<Integer> buffer;
    private final List<Integer> source;

    public Producer(SharedBufferInterface<Integer> buffer, List<Integer> source) {
        this.buffer = buffer;
        this.source = source;
    }

    @Override
    public void run() {
        try {
            for (Integer value : source) {
                buffer.put(value);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
