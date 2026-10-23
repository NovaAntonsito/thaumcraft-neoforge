package voidexpeditions.run;

/** The run lengths the terminal offers, in encounters. */
public final class RunLength {
    public static final int[] OPTIONS = {3, 5, 7};

    private RunLength() {
    }

    /** The offered length nearest to {@code n}. */
    public static int clamp(int n) {
        int best = OPTIONS[0];
        for (int o : OPTIONS) {
            if (Math.abs(o - n) < Math.abs(best - n)) best = o;
        }
        return best;
    }

    public static int next(int n) {
        for (int i = 0; i < OPTIONS.length; i++) {
            if (OPTIONS[i] == n) return OPTIONS[(i + 1) % OPTIONS.length];
        }
        return OPTIONS[0];
    }
}
