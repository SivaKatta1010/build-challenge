package pc001.memento;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Caretaker {
    private final List<Snapshot> snapshots = new ArrayList<>();

    public synchronized void addSnapshot(Snapshot s) {
        snapshots.add(s);
    }

    public synchronized List<Snapshot> getSnapshots() {
        return Collections.unmodifiableList(new ArrayList<>(snapshots));
    }
}
