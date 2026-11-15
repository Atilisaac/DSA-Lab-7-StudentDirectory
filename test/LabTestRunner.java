import java.util.List;
import java.util.Map;

/**
 * Dependency-free acceptance runner for Lab 7.
 *
 * <p>Run with assertions enabled:
 * <pre>java -ea -cp out LabTestRunner</pre>
 *
 * <p>The runner does not use JUnit so that the project compiles with only the
 * JDK. Each test catches its own exception, so one broken method does not hide
 * the other failures.
 */
public final class LabTestRunner {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        boolean assertionsEnabled = false;
        assert assertionsEnabled = true;
        if (!assertionsEnabled) {
            System.err.println("ERROR: assertions are disabled. Run with: java -ea -cp out LabTestRunner");
            System.exit(2);
        }

        System.out.println("Lab 7 acceptance runner");
        System.out.println("=======================");

        run("Empty Map", LabTestRunner::testEmptyDirectory);
        run("Duplicate Key", LabTestRunner::testDuplicateKeyRejected);
        run("Missing Key", LabTestRunner::testMissingKey);
        run("Record Update", LabTestRunner::testRecordUpdate);
        run("Removal", LabTestRunner::testRemoval);
        run("Repeated Characters", LabTestRunner::testRepeatedCharacters);

        run("Snapshot isolation", LabTestRunner::testSnapshotIsDetached);
        run("Sorted snapshot and ID rules", LabTestRunner::testSortedSnapshotAndIdRules);
        run("Empty frequency input", LabTestRunner::testEmptyFrequency);
        run("Counting rules", LabTestRunner::testCountingRules);
        run("Frequency map is unmodifiable", LabTestRunner::testFrequencyMapUnmodifiable);
        run("Student validation", LabTestRunner::testStudentValidation);
        run("Grade boundaries", LabTestRunner::testGradeBoundaries);
        run("Directory invalid arguments", LabTestRunner::testDirectoryInvalidArguments);
        run("Re-add after removal", LabTestRunner::testReAddAfterRemoval);
        run("Student immutability", LabTestRunner::testStudentImmutability);
        run("Product inventory", LabTestRunner::testInventory);
        run("Library tracker", LabTestRunner::testLibraryTracker);
        run("Formatting helpers", LabTestRunner::testFormatting);
        run("Student value semantics", LabTestRunner::testStudentValueSemantics);

        System.out.println();
        System.out.println("Test groups: 20, assertions: " + (passed + failed)
                + ", passed: " + passed + ", failed: " + failed);
        if (failed > 0) {
            System.out.println("RESULT: FAIL");
            System.exit(1);
        }
        System.out.println("RESULT: PASS");
    }

    // ---------------------------------------------------------------- tests

    private static void testEmptyDirectory() {
        StudentDirectory directory = new StudentDirectory();

        checkEquals("empty size is 0", 0, directory.size());
        checkEquals("empty get returns null", null, directory.getStudent("2025-001"));
        checkFalse("empty contains is false", directory.containsStudent("2025-001"));
        checkFalse("empty remove is false", directory.removeStudent("2025-001"));
        checkTrue("empty snapshot is empty", directory.snapshot().isEmpty());

        checkThrows("empty snapshot rejects put", UnsupportedOperationException.class,
                () -> directory.snapshot().put("X", new Student("X", "X", 1.0)));
    }

    /**
     * STUDENT CHECKPOINT ASSERTION.
     *
     * <p>Behaviour chosen by the student: a duplicate add must be rejected and
     * must keep the original record. The student selected this behaviour before
     * implementation; the Java syntax below was drafted collaboratively with
     * the assistant and must be reviewed by the student before submission.
     */
    private static void testDuplicateKeyRejected() {
        StudentDirectory directory = new StudentDirectory();
        Student original = new Student("2025-001", "Alice", 89.5);

        checkTrue("first add succeeds", directory.addStudent(original));
        checkFalse("duplicate add returns false",
                directory.addStudent(new Student("2025-001", "Imposter", 100.0)));

        checkEquals("duplicate does not change size", 1, directory.size());
        checkEquals("duplicate keeps original name",
                "Alice", directory.getStudent("2025-001").getName());
        checkClose("duplicate keeps original grade",
                89.5, directory.getStudent("2025-001").getGrade());
        checkTrue("duplicate keeps the exact stored object",
                original == directory.getStudent("2025-001"));
    }

    private static void testMissingKey() {
        StudentDirectory directory = new StudentDirectory();
        directory.addStudent(new Student("2025-001", "Alice", 89.5));

        checkEquals("missing get returns null", null, directory.getStudent("2025-999"));
        checkFalse("missing update returns false",
                directory.updateStudent(new Student("2025-999", "Nobody", 70.0)));
        checkFalse("missing grade update returns false",
                directory.updateGrade("2025-999", 70.0));
        checkFalse("missing remove returns false", directory.removeStudent("2025-999"));
        checkEquals("missing update did not insert", 1, directory.size());
    }

    private static void testRecordUpdate() {
        StudentDirectory directory = new StudentDirectory();
        directory.addStudent(new Student("2025-001", "Alice", 89.5));

        Student replacement = new Student("2025-001", "Alice Cruz", 95.0);
        checkTrue("update existing returns true", directory.updateStudent(replacement));

        Student stored = directory.getStudent("2025-001");
        checkEquals("updated name is visible", "Alice Cruz", stored.getName());
        checkClose("updated grade is visible", 95.0, stored.getGrade());
        checkEquals("update does not change size", 1, directory.size());

        checkTrue("grade-only update returns true", directory.updateGrade("2025-001", 97.5));
        checkClose("grade-only update is visible", 97.5,
                directory.getStudent("2025-001").getGrade());
        checkEquals("grade-only update keeps name", "Alice Cruz",
                directory.getStudent("2025-001").getName());
    }

    private static void testRemoval() {
        StudentDirectory directory = new StudentDirectory();
        directory.addStudent(new Student("2025-001", "Alice", 89.5));
        directory.addStudent(new Student("2025-002", "Ben", 91.0));

        checkTrue("remove existing returns true", directory.removeStudent("2025-001"));
        checkEquals("removed record is gone", null, directory.getStudent("2025-001"));
        checkFalse("removed id is not contained", directory.containsStudent("2025-001"));
        checkEquals("removal decrements size", 1, directory.size());
        checkFalse("second remove returns false", directory.removeStudent("2025-001"));
        checkEquals("other record survives removal", "Ben",
                directory.getStudent("2025-002").getName());
    }

    private static void testRepeatedCharacters() {
        Map<Character, Integer> counts = CharacterFrequencyCounter.count("BANANA");

        checkEquals("BANANA has three distinct characters", 3, counts.size());
        checkEquals("B count", 1, counts.get('B'));
        checkEquals("A count", 3, counts.get('A'));
        checkEquals("N count", 2, counts.get('N'));
        checkEquals("BANANA total", 6, totalCount(counts));
    }

    private static void testSnapshotIsDetached() {
        StudentDirectory directory = new StudentDirectory();
        directory.addStudent(new Student("2025-001", "Alice", 89.5));

        Map<String, Student> snapshot = directory.snapshot();
        checkEquals("snapshot size before later add", 1, snapshot.size());

        directory.addStudent(new Student("2025-002", "Ben", 91.0));
        checkEquals("later add does not change old snapshot", 1, snapshot.size());
        checkEquals("directory still has both records", 2, directory.size());

        checkThrows("snapshot rejects put", UnsupportedOperationException.class,
                () -> snapshot.put("2025-003", new Student("2025-003", "Carla", 85.5)));
        checkThrows("snapshot rejects remove", UnsupportedOperationException.class,
                () -> snapshot.remove("2025-001"));
        checkThrows("snapshot rejects clear", UnsupportedOperationException.class,
                snapshot::clear);
        checkThrows("snapshot keySet rejects remove", UnsupportedOperationException.class,
                () -> snapshot.keySet().remove("2025-001"));
        checkEquals("directory unchanged after snapshot attempts", 2, directory.size());
    }

    private static void testSortedSnapshotAndIdRules() {
        StudentDirectory directory = new StudentDirectory();
        directory.addStudent(new Student("2025-003", "Carla", 85.5));
        directory.addStudent(new Student("2025-001", "Alice", 89.5));
        directory.addStudent(new Student("2025-002", "Ben", 91.0));

        List<Student> sorted = directory.snapshotSortedById();
        checkEquals("sorted snapshot size", 3, sorted.size());
        checkEquals("first sorted id", "2025-001", sorted.get(0).getId());
        checkEquals("second sorted id", "2025-002", sorted.get(1).getId());
        checkEquals("third sorted id", "2025-003", sorted.get(2).getId());
        checkThrows("sorted snapshot is unmodifiable", UnsupportedOperationException.class,
                () -> sorted.add(new Student("2025-004", "Dina", 88.0)));

        directory.addStudent(new Student("id-lower", "Lower", 70.0));
        checkTrue("exact-case ID exists", directory.containsStudent("id-lower"));
        checkFalse("ID matching is case-sensitive",
                directory.containsStudent("ID-LOWER"));
    }

    private static void testEmptyFrequency() {
        checkTrue("empty string gives empty map",
                CharacterFrequencyCounter.count("").isEmpty());
        checkThrows("null text is rejected", IllegalArgumentException.class,
                () -> CharacterFrequencyCounter.count(null));
    }

    private static void testCountingRules() {
        Map<Character, Integer> counts = CharacterFrequencyCounter.count("Aa  a!");

        checkEquals("counting is case-sensitive", 1, counts.get('A'));
        checkEquals("lower-case count", 2, counts.get('a'));
        checkEquals("spaces are counted", 2, counts.get(' '));
        checkEquals("punctuation is counted", 1, counts.get('!'));
        checkEquals("no hidden filtering", 4, counts.size());
        checkEquals("total characters", 6, totalCount(counts));
    }

    private static void testFrequencyMapUnmodifiable() {
        Map<Character, Integer> counts = CharacterFrequencyCounter.count("A");
        checkThrows("frequency map rejects put", UnsupportedOperationException.class,
                () -> counts.put('B', 1));
        checkThrows("frequency map rejects remove", UnsupportedOperationException.class,
                () -> counts.remove('A'));
    }

    private static void testStudentValidation() {
        checkThrows("blank id rejected", IllegalArgumentException.class,
                () -> new Student("", "Alice", 89.5));
        checkThrows("null id rejected", IllegalArgumentException.class,
                () -> new Student(null, "Alice", 89.5));
        checkThrows("blank name rejected", IllegalArgumentException.class,
                () -> new Student("2025-001", "  ", 89.5));
        checkThrows("negative grade rejected", IllegalArgumentException.class,
                () -> new Student("2025-001", "Alice", -0.1));
        checkThrows("grade above 100 rejected", IllegalArgumentException.class,
                () -> new Student("2025-001", "Alice", 100.1));
        checkThrows("NaN grade rejected", IllegalArgumentException.class,
                () -> new Student("2025-001", "Alice", Double.NaN));
    }

    private static void testGradeBoundaries() {
        Student zero = new Student("2025-010", "Zero", 0.0);
        Student hundred = new Student("2025-011", "Hundred", 100.0);
        checkClose("grade 0 is accepted", 0.0, zero.getGrade());
        checkClose("grade 100 is accepted", 100.0, hundred.getGrade());

        Student alice = new Student("2025-001", "Alice", 89.5);
        checkThrows("copy rejects grade above 100", IllegalArgumentException.class,
                () -> alice.withGrade(100.1));
        checkThrows("copy rejects NaN", IllegalArgumentException.class,
                () -> alice.withGrade(Double.NaN));

        StudentDirectory directory = new StudentDirectory();
        directory.addStudent(alice);
        checkThrows("updateGrade rejects invalid grade", IllegalArgumentException.class,
                () -> directory.updateGrade("2025-001", -1.0));
        checkClose("failed invalid update keeps old grade", 89.5,
                directory.getStudent("2025-001").getGrade());
        checkFalse("updateGrade missing returns false",
                directory.updateGrade("2025-999", 90.0));
        checkThrows("updateGrade missing with invalid grade throws first",
                IllegalArgumentException.class,
                () -> directory.updateGrade("2025-999", -1.0));
    }

    private static void testDirectoryInvalidArguments() {
        StudentDirectory directory = new StudentDirectory();
        checkThrows("add null rejected", IllegalArgumentException.class,
                () -> directory.addStudent(null));
        checkThrows("get null id rejected", IllegalArgumentException.class,
                () -> directory.getStudent(null));
        checkThrows("get blank id rejected", IllegalArgumentException.class,
                () -> directory.getStudent("  "));
        checkThrows("contains null id rejected", IllegalArgumentException.class,
                () -> directory.containsStudent(null));
        checkThrows("remove null id rejected", IllegalArgumentException.class,
                () -> directory.removeStudent(null));
        checkThrows("update null student rejected", IllegalArgumentException.class,
                () -> directory.updateStudent(null));
        checkThrows("updateGrade null id rejected", IllegalArgumentException.class,
                () -> directory.updateGrade(null, 90.0));
    }

    private static void testReAddAfterRemoval() {
        StudentDirectory directory = new StudentDirectory();
        directory.addStudent(new Student("2025-001", "Alice", 89.5));
        checkTrue("remove before re-add", directory.removeStudent("2025-001"));
        checkTrue("re-add removed id", directory.addStudent(
                new Student("2025-001", "Alice Again", 92.0)));
        checkEquals("re-added record is visible", "Alice Again",
                directory.getStudent("2025-001").getName());
    }

    private static void testStudentImmutability() {
        Student original = new Student("2025-001", "Alice", 89.5);
        Student changed = original.withGrade(95.0);

        checkClose("original grade is unchanged", 89.5, original.getGrade());
        checkClose("changed copy has new grade", 95.0, changed.getGrade());
        checkEquals("changed copy keeps id", "2025-001", changed.getId());
        checkEquals("changed copy keeps name", "Alice", changed.getName());
    }

    private static void testInventory() {
        ProductInventory inventory = new ProductInventory();

        checkTrue("add product", inventory.addProduct("P-100", 10));
        checkFalse("duplicate product rejected", inventory.addProduct("P-100", 99));
        checkEquals("duplicate keeps original stock", 10, inventory.getStock("P-100"));

        checkTrue("update stock", inventory.updateStock("P-100", 7));
        checkEquals("stock updated", 7, inventory.getStock("P-100"));
        checkFalse("update missing product", inventory.updateStock("P-999", 5));
        checkFalse("negative stock rejected", inventory.updateStock("P-100", -1));
        checkEquals("negative update did not change stock", 7,
                inventory.getStock("P-100"));
        checkFalse("negative initial stock rejected",
                inventory.addProduct("P-400", -2));
        checkEquals("missing product stock is -1", -1, inventory.getStock("P-999"));
        checkEquals("inventory display is code-sorted", "P-100: 7",
                inventory.describeAll());

        Map<String, Integer> snapshot = inventory.snapshot();
        checkEquals("inventory snapshot size", 1, snapshot.size());
        checkThrows("inventory snapshot is unmodifiable",
                UnsupportedOperationException.class,
                () -> snapshot.put("P-200", 1));
    }

    private static void testLibraryTracker() {
        LibraryBookTracker library = new LibraryBookTracker();

        checkTrue("add book", library.addBook("Clean Code", 2));
        checkFalse("duplicate book rejected", library.addBook("Clean Code", 9));
        checkEquals("duplicate keeps copies", 2, library.getCopies("Clean Code"));

        checkTrue("borrow first copy", library.borrow("Clean Code"));
        checkEquals("borrow decrements", 1, library.getCopies("Clean Code"));
        checkTrue("borrow second copy", library.borrow("Clean Code"));
        checkFalse("borrow at zero fails", library.borrow("Clean Code"));
        checkTrue("return increments", library.returnBook("Clean Code"));
        checkEquals("returned copy is available", 1, library.getCopies("Clean Code"));

        checkFalse("borrow missing title", library.borrow("Missing"));
        checkFalse("return missing title", library.returnBook("Missing"));
        checkTrue("remove book", library.removeBook("Clean Code"));
        checkFalse("remove missing book", library.removeBook("Clean Code"));
        checkEquals("missing copies is -1", -1, library.getCopies("Missing"));
        checkFalse("negative initial copies rejected",
                library.addBook("Negative", -1));
    }

    private static void testFormatting() {
        Map<Character, Integer> counts = CharacterFrequencyCounter.count("Aa  a!");
        checkEquals("frequency format is char-sorted", "' '=2, !=1, A=1, a=2",
                CharacterFrequencyCounter.format(counts));
        checkEquals("empty frequency format is empty", "",
                CharacterFrequencyCounter.format(CharacterFrequencyCounter.count("")));
    }

    private static void testStudentValueSemantics() {
        Student alice = new Student("2025-001", "Alice", 89.5);
        Student same = new Student("2025-001", "Alice", 89.5);
        Student different = new Student("2025-002", "Ben", 91.0);

        checkTrue("equal students compare equal", alice.equals(same));
        checkEquals("equal students share a hash code", alice.hashCode(), same.hashCode());
        checkFalse("different students are not equal", alice.equals(different));

        Student renamed = alice.withName("Alice Cruz");
        checkEquals("withName changes name", "Alice Cruz", renamed.getName());
        checkClose("withName keeps grade", 89.5, renamed.getGrade());
        checkEquals("withName keeps id", "2025-001", renamed.getId());
    }

    // ------------------------------------------------------------- helpers

    private static int totalCount(Map<Character, Integer> counts) {
        int total = 0;
        for (int value : counts.values()) {
            total += value;
        }
        return total;
    }

    private static void run(String label, Runnable test) {
        int failuresBefore = failed;
        try {
            test.run();
        } catch (Throwable error) {
            fail(label + " threw " + error.getClass().getSimpleName()
                    + ": " + error.getMessage());
        }
        if (failed == failuresBefore) {
            System.out.println("[PASS] " + label);
        }
    }

    private static void checkTrue(String label, boolean condition) {
        if (condition) {
            passed++;
        } else {
            fail(label + " expected true but was false");
        }
    }

    private static void checkFalse(String label, boolean condition) {
        if (!condition) {
            passed++;
        } else {
            fail(label + " expected false but was true");
        }
    }

    private static void checkEquals(String label, Object expected, Object actual) {
        if (java.util.Objects.equals(expected, actual)) {
            passed++;
        } else {
            fail(label + " expected <" + expected + "> but was <" + actual + ">");
        }
    }

    private static void checkClose(String label, double expected, double actual) {
        if (Math.abs(expected - actual) < 0.000_001) {
            passed++;
        } else {
            fail(label + " expected <" + expected + "> but was <" + actual + ">");
        }
    }

    private static void checkThrows(String label,
                                    Class<? extends Throwable> expected,
                                    Runnable action) {
        try {
            action.run();
            fail(label + " expected " + expected.getSimpleName()
                    + " but no exception was thrown");
        } catch (Throwable error) {
            if (expected.isInstance(error)) {
                passed++;
            } else {
                fail(label + " expected " + expected.getSimpleName()
                        + " but got " + error.getClass().getSimpleName()
                        + ": " + error.getMessage());
            }
        }
    }

    private static void fail(String message) {
        failed++;
        System.out.println("[FAIL] " + message);
    }

    private LabTestRunner() {
        // utility class
    }
}
