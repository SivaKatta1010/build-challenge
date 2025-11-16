package pc001.buffer;

import java.util.List;

public interface SharedBufferInterface<T> {
    /**
     * Put an item into the buffer, blocking if necessary.
     * @param item item to add
     * @throws InterruptedException if the thread is interrupted while waiting
     */
    void put(T item) throws InterruptedException;

    /**
     * Take an item from the buffer, blocking if necessary.
     * @return the taken item
     * @throws InterruptedException if the thread is interrupted while waiting
     */
    T take() throws InterruptedException;

    /**
     * Return a non-destructive snapshot of the buffer contents.
     * The ordering of elements reflects the internal ordering of the buffer
     * implementation but consumers should not rely on total ordering when
     * multiple producers/consumers are used.
     * @return a List snapshot of current contents
     */
    List<T> snapshotContents();

    /** Current buffer size. */
    int size();

    /** Total number of items that have been produced (since buffer creation). */
    long getProducedCount();

    /** Total number of items that have been consumed (since buffer creation). */
    long getConsumedCount();
}
