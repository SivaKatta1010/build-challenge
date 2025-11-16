package pc001.memento;

import java.time.Instant;
import java.util.List;

public class Snapshot {
    private final String tag;
    private final Instant createdAt;
    private final List<?> bufferContents;
    private final List<?> destinationSnapshot;

    public Snapshot(String tag, List<?> bufferContents, List<?> destinationSnapshot) {
        this.tag = tag;
        this.createdAt = Instant.now();
        this.bufferContents = bufferContents;
        this.destinationSnapshot = destinationSnapshot;
    }

    public String getTag() { return tag; }
    public Instant getCreatedAt() { return createdAt; }
    public List<?> getBufferContents() { return bufferContents; }
    public List<?> getDestinationSnapshot() { return destinationSnapshot; }

    @Override
    public String toString() {
        return "Snapshot{" + "tag='" + tag + '\'' + ", createdAt=" + createdAt + ", buffer=" + bufferContents + ", dest=" + destinationSnapshot + '}';
    }
}
