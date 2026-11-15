import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Measures the Lab 7 operations. This is evidence, not a proof: timings are
 * wall-clock observations from the machine that ran the program.
 *
 * <p>What is measured:
 * <ul>
 *   <li>elapsed nanoseconds from System.nanoTime, median of 3 trials;</li>
 *   <li>ProbeKey.equals calls, incremented inside equals, as a proxy for
 *       scanning a bucket chain;</li>
 *   <li>ProbeKey.hashCode calls, incremented inside hashCode;</li>
 *   <li>map operations performed (one put/get/remove per element).</li>
 * </ul>
 *
 * <p>Run with: {@code java -cp out ComplexityMeasurement}
 */
public final class ComplexityMeasurement {

    private static final int TRIALS = 3;
    private static final int WARM_UP_SIZE = 10_000;
    private static final int SNAPSHOT_REPEATS = 20;
    private static final int[] DIRECTORY_SIZES = {10_000, 100_000, 300_000};
    private static final int[] COLLISION_SIZES = {1_000, 4_000, 16_000};
    private static final int[] FREQUENCY_SIZES = {10_000, 100_000, 1_000_000};
    private static final int[] LINEAR_SIZES = {10_000, 100_000, 300_000};

    public static void main(String[] args) {
        System.out.println("Lab 7 complexity measurements");
        System.out.println("Environment: Java " + System.getProperty("java.version")
                + ", " + System.getProperty("os.name") + " "
                + System.getProperty("os.arch") + ", "
                + Runtime.getRuntime().availableProcessors() + " processors, max heap "
                + (Runtime.getRuntime().maxMemory() / (1024 * 1024)) + " MiB");
        System.out.println("Median of " + TRIALS + " trials after one fixed "
                + WARM_UP_SIZE + "-element warm-up per shape.");
        System.out.println("Times are machine-specific observations, not guarantees.");
        System.out.println("Counter increments and Integer boxing occur inside timed loops,");
        System.out.println("so ratios are more trustworthy than absolute nanoseconds.");
        System.out.println();

        printCollisionTable();
        System.out.println();
        printDirectoryTable();
        System.out.println();
        printFrequencyTable();
        System.out.println();
        printLinearComparison();
    }

    // ------------------------------------------------------ collision probe

    private static void printCollisionTable() {
        System.out.println("Table 1 - HashMap get/put/remove with different hash shapes");
        System.out.println("Workload: n distinct keys, successful get using fresh equal key objects.");
        System.out.println("\"identical-Comparable\" is the control: same constant hash, but the key");
        System.out.println("implements Comparable, so a tree bin can navigate instead of scanning.");
        System.out.println();
        System.out.println("| shape | n | put ns/op | get ns/op | remove ns/op | equals/get | hashCode/get |");
        System.out.println("|---|---:|---:|---:|---:|---:|---:|");

        String[] shapes = {"good", "clustered", "identical"};
        for (String shape : shapes) {
            runCollisionTrial(WARM_UP_SIZE, shape); // fixed warm-up, not scaled with n
            for (int n : COLLISION_SIZES) {
                CollisionResult result = measureCollision(n, shape);
                System.out.printf(Locale.ROOT,
                        "| %s | %d | %.1f | %.1f | %.1f | %.2f | %.2f |%n",
                        shape, n, result.putNsPerOp, result.getNsPerOp,
                        result.removeNsPerOp, result.equalsPerGet,
                        result.hashCodesPerGet);
            }
        }

        runComparableTrial(WARM_UP_SIZE);
        for (int n : COLLISION_SIZES) {
            CollisionResult result = measureComparableIdentical(n);
            System.out.printf(Locale.ROOT,
                    "| identical-Comparable | %d | %.1f | %.1f | %.1f | %.2f | %.2f |%n",
                    n, result.putNsPerOp, result.getNsPerOp,
                    result.removeNsPerOp, result.equalsPerGet,
                    result.hashCodesPerGet);
        }
    }

    private static CollisionResult measureCollision(int n, String shape) {
        double[] putTimes = new double[TRIALS];
        double[] getTimes = new double[TRIALS];
        double[] removeTimes = new double[TRIALS];
        double equalsPerGet = 0;
        double hashesPerGet = 0;

        for (int trial = 0; trial < TRIALS; trial++) {
            CollisionTrial run = runCollisionTrial(n, shape);
            putTimes[trial] = run.putNsPerOp;
            getTimes[trial] = run.getNsPerOp;
            removeTimes[trial] = run.removeNsPerOp;
            equalsPerGet = run.equalsPerGet;
            hashesPerGet = run.hashCodesPerGet;
        }

        return new CollisionResult(median(putTimes), median(getTimes),
                median(removeTimes), equalsPerGet, hashesPerGet);
    }

    private static CollisionTrial runCollisionTrial(int n, String shape) {
        List<ProbeKey> keys = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            keys.add(new ProbeKey("K" + i, forcedHash(shape, i, "K" + i)));
        }

        Map<ProbeKey, Integer> map = new HashMap<>();
        ProbeKey.resetCounters();
        long putStart = System.nanoTime();
        for (int i = 0; i < n; i++) {
            map.put(keys.get(i), 1); // cached Integer, so no per-put boxing allocation
        }
        double putNsPerOp = (System.nanoTime() - putStart) / (double) n;

        // Look up fresh but equal key objects. If we reused the stored objects,
        // HashMap would match by identity and never call equals, hiding the
        // bucket scan we are trying to measure.
        List<ProbeKey> lookupKeys = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            lookupKeys.add(new ProbeKey("K" + i, forcedHash(shape, i, "K" + i)));
        }

        // Untimed warm-up of get/remove code paths.
        for (ProbeKey key : lookupKeys) {
            map.get(key);
        }

        ProbeKey.resetCounters();
        long getStart = System.nanoTime();
        int sink = 0;
        for (ProbeKey key : lookupKeys) {
            sink += map.get(key);
        }
        double getNsPerOp = (System.nanoTime() - getStart) / (double) n;
        double equalsPerGet = ProbeKey.equalsCalls / (double) n;
        double hashesPerGet = ProbeKey.hashCodeCalls / (double) n;

        ProbeKey.resetCounters();
        long removeStart = System.nanoTime();
        for (ProbeKey key : lookupKeys) {
            map.remove(key);
        }
        double removeNsPerOp = (System.nanoTime() - removeStart) / (double) n;

        if (sink == Integer.MIN_VALUE) {
            System.out.print(""); // keep the result observable to the JIT
        }
        return new CollisionTrial(putNsPerOp, getNsPerOp, removeNsPerOp,
                equalsPerGet, hashesPerGet);
    }

    private static int forcedHash(String shape, int index, String id) {
        if ("identical".equals(shape)) {
            return 7;
        }
        if ("clustered".equals(shape)) {
            return index % 16;
        }
        return id.hashCode();
    }

    /**
     * Control experiment: identical hashes, but Comparable keys. Java's tree bin
     * can navigate with compareTo, so equals/get should grow like log n rather
     * than like the linear scan seen for non-Comparable identical keys.
     */
    private static CollisionResult measureComparableIdentical(int n) {
        double[] putTimes = new double[TRIALS];
        double[] getTimes = new double[TRIALS];
        double[] removeTimes = new double[TRIALS];
        double equalsPerGet = 0;
        double hashesPerGet = 0;

        for (int trial = 0; trial < TRIALS; trial++) {
            CollisionTrial run = runComparableTrial(n);
            putTimes[trial] = run.putNsPerOp;
            getTimes[trial] = run.getNsPerOp;
            removeTimes[trial] = run.removeNsPerOp;
            equalsPerGet = run.equalsPerGet;
            hashesPerGet = run.hashCodesPerGet;
        }

        return new CollisionResult(median(putTimes), median(getTimes),
                median(removeTimes), equalsPerGet, hashesPerGet);
    }

    private static CollisionTrial runComparableTrial(int n) {
        List<ComparableProbeKey> keys = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            keys.add(new ComparableProbeKey("K" + i));
        }

        Map<ComparableProbeKey, Integer> map = new HashMap<>();
        ComparableProbeKey.resetCounters();
        long putStart = System.nanoTime();
        for (int i = 0; i < n; i++) {
            map.put(keys.get(i), 1);
        }
        double putNsPerOp = (System.nanoTime() - putStart) / (double) n;

        List<ComparableProbeKey> lookupKeys = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            lookupKeys.add(new ComparableProbeKey("K" + i));
        }
        for (ComparableProbeKey key : lookupKeys) {
            map.get(key);
        }

        ComparableProbeKey.resetCounters();
        long getStart = System.nanoTime();
        int sink = 0;
        for (ComparableProbeKey key : lookupKeys) {
            sink += map.get(key);
        }
        double getNsPerOp = (System.nanoTime() - getStart) / (double) n;
        double equalsPerGet = ComparableProbeKey.equalsCalls / (double) n;
        double hashesPerGet = ComparableProbeKey.hashCodeCalls / (double) n;

        long removeStart = System.nanoTime();
        for (ComparableProbeKey key : lookupKeys) {
            map.remove(key);
        }
        double removeNsPerOp = (System.nanoTime() - removeStart) / (double) n;

        if (sink == Integer.MIN_VALUE) {
            System.out.print("");
        }
        return new CollisionTrial(putNsPerOp, getNsPerOp, removeNsPerOp,
                equalsPerGet, hashesPerGet);
    }

    // --------------------------------------------------------- directory

    private static void printDirectoryTable() {
        System.out.println("Table 2 - StudentDirectory snapshot cost");
        System.out.println("Workload: n immutable Student records; " + SNAPSHOT_REPEATS
                + " snapshots timed per trial to average out GC pauses.");
        System.out.println();
        System.out.println("| n | snapshot ns | snapshot ns/entry | sorted snapshot ns/entry |");
        System.out.println("|---:|---:|---:|---:|");

        for (int n : DIRECTORY_SIZES) {
            double snapshot = measureSnapshot(n, false);
            double sorted = measureSnapshot(n, true);
            System.out.printf(Locale.ROOT, "| %d | %.0f | %.1f | %.1f |%n",
                    n, snapshot, snapshot / n, sorted / n);
        }
    }

    private static double measureSnapshot(int n, boolean sorted) {
        // Fixed-size warm-up: compile the code path without scaling the warm-up.
        StudentDirectory warmUp = makeDirectory(WARM_UP_SIZE);
        if (sorted) {
            warmUp.snapshotSortedById();
        } else {
            warmUp.snapshot();
        }

        double[] times = new double[TRIALS];
        for (int trial = 0; trial < TRIALS; trial++) {
            StudentDirectory directory = makeDirectory(n);
            // Warm the copy path for this directory, then time repeated calls.
            if (sorted) {
                directory.snapshotSortedById();
            } else {
                directory.snapshot();
            }

            long start = System.nanoTime();
            for (int repeat = 0; repeat < SNAPSHOT_REPEATS; repeat++) {
                Object result = sorted
                        ? directory.snapshotSortedById()
                        : directory.snapshot();
                if (result == null) {
                    throw new IllegalStateException("snapshot returned null");
                }
            }
            times[trial] = (System.nanoTime() - start) / (double) SNAPSHOT_REPEATS;
        }
        return median(times);
    }

    private static StudentDirectory makeDirectory(int n) {
        StudentDirectory directory = new StudentDirectory();
        for (int i = 0; i < n; i++) {
            String id = String.format(Locale.ROOT, "ID%07d", i);
            directory.addStudent(new Student(id, "Name" + i, i % 101));
        }
        return directory;
    }

    // -------------------------------------------------------- frequency

    private static void printFrequencyTable() {
        System.out.println("Table 3 - CharacterFrequencyCounter");
        System.out.println("Workload: two map operations per char (getOrDefault + put).");
        System.out.println("The last row exercises every possible char value (65,536 distinct).");
        System.out.println();
        System.out.println("| n | ns/char | map operations (2n) | distinct characters |");
        System.out.println("|---:|---:|---:|---:|");

        CharacterFrequencyCounter.count(repeatingLetters(WARM_UP_SIZE));

        for (int n : FREQUENCY_SIZES) {
            String text = repeatingLetters(n);
            printFrequencyRow(n, text);
        }

        int allCharLength = 65_536 * 16;
        printFrequencyRow(allCharLength, allCharactersText(16));
    }

    private static void printFrequencyRow(int n, String text) {
        double[] times = new double[TRIALS];
        int distinct = 0;
        for (int trial = 0; trial < TRIALS; trial++) {
            long start = System.nanoTime();
            Map<Character, Integer> counts = CharacterFrequencyCounter.count(text);
            times[trial] = System.nanoTime() - start;
            distinct = counts.size();
        }
        System.out.printf(Locale.ROOT, "| %d | %.1f | %d | %d |%n",
                n, median(times) / n, 2L * n, distinct);
    }

    private static String allCharactersText(int repeats) {
        StringBuilder text = new StringBuilder(65_536 * repeats);
        for (int repeat = 0; repeat < repeats; repeat++) {
            for (int value = Character.MIN_VALUE; value <= Character.MAX_VALUE; value++) {
                text.append((char) value);
            }
        }
        return text.toString();
    }

    private static String repeatingLetters(int n) {
        StringBuilder text = new StringBuilder(n);
        for (int i = 0; i < n; i++) {
            text.append((char) ('A' + (i % 26)));
        }
        return text.toString();
    }

    // ------------------------------------------------- linear comparison

    private static void printLinearComparison() {
        System.out.println("Table 4 - HashMap lookup versus ArrayList linear search");
        System.out.println("Workload: 100 searches for precomputed IDs near the end of the list");
        System.out.println("(worst case). Target strings are built before the timer starts.");
        System.out.println();
        System.out.println("| n | ArrayList linear search ns/search | HashMap get ns/search |");
        System.out.println("|---:|---:|---:|");

        for (int n : LINEAR_SIZES) {
            List<String> ids = new ArrayList<>(n);
            for (int i = 0; i < n; i++) {
                ids.add("ID" + i);
            }
            Map<String, Integer> map = new HashMap<>();
            for (int i = 0; i < n; i++) {
                map.put(ids.get(i), i);
            }

            // Build the target strings once, outside the timed loops.
            String[] targets = new String[100];
            for (int j = 0; j < targets.length; j++) {
                targets[j] = "ID" + (n - 1 - j);
            }

            // Warm-up.
            linearSearch(ids, targets[0]);
            map.get(targets[0]);

            double[] linearTimes = new double[TRIALS];
            double[] mapTimes = new double[TRIALS];
            for (int trial = 0; trial < TRIALS; trial++) {
                long linearStart = System.nanoTime();
                for (String target : targets) {
                    linearSearch(ids, target);
                }
                linearTimes[trial] = (System.nanoTime() - linearStart) / (double) targets.length;

                long mapStart = System.nanoTime();
                for (String target : targets) {
                    map.get(target);
                }
                mapTimes[trial] = (System.nanoTime() - mapStart) / (double) targets.length;
            }

            System.out.printf(Locale.ROOT, "| %d | %.1f | %.1f |%n",
                    n, median(linearTimes), median(mapTimes));
        }
    }

    private static void linearSearch(List<String> ids, String target) {
        for (int i = 0; i < ids.size(); i++) {
            if (ids.get(i).equals(target)) {
                return;
            }
        }
    }

    // ------------------------------------------------------------ helpers

    private static double median(double[] values) {
        double[] copy = Arrays.copyOf(values, values.length);
        Arrays.sort(copy);
        return copy[copy.length / 2];
    }

    private static final class ProbeKey {
        private final String id;
        private final int hash;
        private static long equalsCalls;
        private static long hashCodeCalls;

        private ProbeKey(String id, int hash) {
            this.id = id;
            this.hash = hash;
        }

        private static void resetCounters() {
            equalsCalls = 0;
            hashCodeCalls = 0;
        }

        @Override
        public int hashCode() {
            hashCodeCalls++;
            return hash;
        }

        @Override
        public boolean equals(Object other) {
            equalsCalls++;
            if (this == other) {
                return true;
            }
            if (!(other instanceof ProbeKey)) {
                return false;
            }
            ProbeKey key = (ProbeKey) other;
            return id.equals(key.id);
        }
    }

    /**
     * Comparable control key. It also returns the constant hash 7, but now a
     * tree bin can use compareTo to navigate. equals is still counted because
     * HashMap checks equality at each tree node it visits.
     */
    private static final class ComparableProbeKey
            implements Comparable<ComparableProbeKey> {
        private final String id;
        private static long equalsCalls;
        private static long hashCodeCalls;

        private ComparableProbeKey(String id) {
            this.id = id;
        }

        private static void resetCounters() {
            equalsCalls = 0;
            hashCodeCalls = 0;
        }

        @Override
        public int hashCode() {
            hashCodeCalls++;
            return 7;
        }

        @Override
        public boolean equals(Object other) {
            equalsCalls++;
            if (this == other) {
                return true;
            }
            if (!(other instanceof ComparableProbeKey)) {
                return false;
            }
            ComparableProbeKey key = (ComparableProbeKey) other;
            return id.equals(key.id);
        }

        @Override
        public int compareTo(ComparableProbeKey other) {
            return id.compareTo(other.id);
        }
    }

    private static final class CollisionTrial {
        private final double putNsPerOp;
        private final double getNsPerOp;
        private final double removeNsPerOp;
        private final double equalsPerGet;
        private final double hashCodesPerGet;

        private CollisionTrial(double putNsPerOp, double getNsPerOp,
                               double removeNsPerOp, double equalsPerGet,
                               double hashCodesPerGet) {
            this.putNsPerOp = putNsPerOp;
            this.getNsPerOp = getNsPerOp;
            this.removeNsPerOp = removeNsPerOp;
            this.equalsPerGet = equalsPerGet;
            this.hashCodesPerGet = hashCodesPerGet;
        }
    }

    private static final class CollisionResult {
        private final double putNsPerOp;
        private final double getNsPerOp;
        private final double removeNsPerOp;
        private final double equalsPerGet;
        private final double hashCodesPerGet;

        private CollisionResult(double putNsPerOp, double getNsPerOp,
                                double removeNsPerOp, double equalsPerGet,
                                double hashCodesPerGet) {
            this.putNsPerOp = putNsPerOp;
            this.getNsPerOp = getNsPerOp;
            this.removeNsPerOp = removeNsPerOp;
            this.equalsPerGet = equalsPerGet;
            this.hashCodesPerGet = hashCodesPerGet;
        }
    }

    private ComplexityMeasurement() {
        // utility class
    }
}
