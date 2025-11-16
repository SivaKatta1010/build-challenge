package pc001.observer;

import pc001.Config;

public class QueueObserver implements IObserver {
    @Override
    public void update(String concern, Object subject) {
        // Only print per-change notifications when verbose mode is enabled to avoid terminal clutter.
        if (Config.isVerbose()) {
            System.out.println("[QueueObserver] concern=" + concern + " subject=" + subject.getClass().getSimpleName());
        }
    }
}
