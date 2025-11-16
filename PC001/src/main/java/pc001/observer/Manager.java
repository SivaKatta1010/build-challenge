package pc001.observer;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import pc001.memento.Caretaker;
import pc001.memento.Snapshot;

/**
 * Manager supports multiple observers per concern, records simple event log,
 * and can create/replay snapshots via Caretaker.
 */
public class Manager {
    private static Manager instance;
    private final Map<String, List<IObserver>> observers = new HashMap<>();
    private final List<String> eventLog = new ArrayList<>();
    private final Caretaker caretaker = new Caretaker();

    private Manager() {}

    public static synchronized Manager getInstance() {
        if (instance == null) {
            instance = new Manager();
        }
        return instance;
    }

    public synchronized void addObserver(String concern, IObserver observer) {
        observers.computeIfAbsent(concern, k -> new ArrayList<>()).add(observer);
    }

    public synchronized void removeObserver(String concern, IObserver observer) {
        List<IObserver> list = observers.get(concern);
        if (list != null) list.remove(observer);
    }

    public synchronized void notifyChange(String concern, Object subject) {
        String entry = Instant.now() + " " + concern + " " + subject.getClass().getSimpleName();
        eventLog.add(entry);
        List<IObserver> list = observers.get(concern);
        if (list != null) {
            for (IObserver obs : new ArrayList<>(list)) {
                try {
                    obs.update(concern, subject);
                } catch (Exception e) {
                    // swallow observer exceptions to keep manager robust
                    e.printStackTrace();
                }
            }
        }
    }

    public synchronized List<String> getEventLog() {
        return new ArrayList<>(eventLog);
    }

    public synchronized void saveSnapshot(String tag, java.util.List<?> bufferContents, java.util.List<?> destination) {
        Snapshot s = new Snapshot(tag, bufferContents, destination);
        caretaker.addSnapshot(s);
    }

    public synchronized java.util.List<Snapshot> replay() {
        return caretaker.getSnapshots();
    }
}
