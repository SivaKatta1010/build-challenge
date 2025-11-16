package pc001;

/**
 * Global config used to control verbosity and other runtime flags.
 */
public final class Config {
    private static volatile boolean VERBOSE = false;

    private Config() {}

    public static boolean isVerbose() { return VERBOSE; }

    public static void setVerbose(boolean v) { VERBOSE = v; }
}
