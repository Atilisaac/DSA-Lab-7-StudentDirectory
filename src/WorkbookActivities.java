import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The four workbook lab activities.
 *
 * <p>Each method is self-contained so it can be run from Lab7Demo or called
 * individually during a live defense. Display order is made deterministic by
 * sorting a copy; the underlying storage is still a HashMap.
 */
public final class WorkbookActivities {

    /** Task 1: put, get, containsKey and remove on HashMap<String, Double>. */
    public static void task1HashMapBasics() {
        printHeader("Task 1 - HashMap basics");
        HashMap<String, Double> grades = sampleGrades();

        System.out.println("Initial grades: " + formatGrades(grades));
        System.out.println("Ben's grade (get): " + grades.get("Ben"));
        System.out.println("Contains Alice? " + grades.containsKey("Alice"));
        System.out.println("Contains Diana? " + grades.containsKey("Diana"));

        Double removed = grades.remove("Carla");
        System.out.println("Removed Carla's grade (remove): " + removed);
        System.out.println("After removal: " + formatGrades(grades));
    }

    /** Task 2: iterate over entrySet() with a for-each loop. */
    public static void task2IterateEntries() {
        printHeader("Task 2 - Iterating with entrySet()");
        HashMap<String, Double> grades = sampleGrades();

        // HashMap has no guaranteed order. Sorting a copy of the entries keeps
        // the console output stable while still iterating entrySet().
        List<Map.Entry<String, Double>> entries = new ArrayList<>(grades.entrySet());
        entries.sort(Map.Entry.comparingByKey());

        for (Map.Entry<String, Double> entry : entries) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }

    /**
     * Task 3 and Practice Problem 1: HashMap of Student objects, five records,
     * retrieval, update, duplicate rejection, and display.
     */
    public static void task3HashMapOfObjects() {
        printHeader("Task 3 - HashMap of Student objects");
        StudentDirectory directory = new StudentDirectory();

        directory.addStudent(new Student("2025-001", "Alice", 89.5));
        directory.addStudent(new Student("2025-002", "Ben", 91.0));
        directory.addStudent(new Student("2025-003", "Carla", 85.5));
        directory.addStudent(new Student("2025-004", "Dina", 78.0));
        directory.addStudent(new Student("2025-005", "Eli", 94.25));

        System.out.println("Added 5 students. Size = " + directory.size());
        System.out.println("Retrieved 2025-002: " + directory.getStudent("2025-002"));

        boolean updated = directory.updateGrade("2025-002", 93.25);
        System.out.println("Updated Ben's grade? " + updated
                + " -> " + directory.getStudent("2025-002").getGrade());

        boolean duplicate = directory.addStudent(
                new Student("2025-001", "Imposter", 100.0));
        System.out.println("Duplicate 2025-001 accepted? " + duplicate
                + " (original kept: " + directory.getStudent("2025-001").getName() + ")");

        System.out.println("All records:");
        for (Student student : directory.snapshotSortedById()) {
            System.out.println("  " + student);
        }
    }

    /** Task 4: product inventory with product code keys and Integer values. */
    public static void task4ProductInventory() {
        printHeader("Task 4 - Product inventory");
        ProductInventory inventory = new ProductInventory();

        inventory.addProduct("P-100", 50);
        inventory.addProduct("P-200", 20);
        inventory.addProduct("P-300", 8);

        System.out.println("Added 3 products.");
        System.out.println("Update P-200 to 25? " + inventory.updateStock("P-200", 25));
        System.out.println("Update missing P-999? " + inventory.updateStock("P-999", 10));
        System.out.println("Inventory:");
        System.out.println(inventory.describeAll());
    }

    /** Practice Problem 2: library book tracker with borrow and return. */
    public static void practiceProblem2Library() {
        printHeader("Practice Problem 2 - Library book tracker");
        LibraryBookTracker library = new LibraryBookTracker();

        library.addBook("Clean Code", 2);
        library.addBook("Effective Java", 1);
        System.out.println("Added 2 titles.");

        System.out.println("Borrow Clean Code? " + library.borrow("Clean Code")
                + " -> copies: " + library.getCopies("Clean Code"));
        System.out.println("Borrow Effective Java? " + library.borrow("Effective Java")
                + " -> copies: " + library.getCopies("Effective Java"));
        System.out.println("Borrow Effective Java again? "
                + library.borrow("Effective Java") + " (no copies left)");
        System.out.println("Return Effective Java? " + library.returnBook("Effective Java")
                + " -> copies: " + library.getCopies("Effective Java"));
        System.out.println("Remove Clean Code? " + library.removeBook("Clean Code"));
        System.out.println("Library:");
        System.out.println(library.describeAll());
    }

    /** Shared sample data from the workbook. A fresh map is returned each call. */
    private static HashMap<String, Double> sampleGrades() {
        HashMap<String, Double> grades = new HashMap<>();
        grades.put("Alice", 89.5);
        grades.put("Ben", 91.0);
        grades.put("Carla", 85.5);
        return grades;
    }

    /** Formats grade entries in name order for stable output. */
    private static String formatGrades(Map<String, Double> grades) {
        List<Map.Entry<String, Double>> entries = new ArrayList<>(grades.entrySet());
        entries.sort(Map.Entry.comparingByKey());

        StringBuilder result = new StringBuilder("{");
        for (Map.Entry<String, Double> entry : entries) {
            if (result.length() > 1) {
                result.append(", ");
            }
            result.append(entry.getKey()).append('=').append(entry.getValue());
        }
        return result.append('}').toString();
    }

    private static void printHeader(String title) {
        System.out.println();
        System.out.println("=== " + title + " ===");
    }

    private WorkbookActivities() {
        // utility class
    }
}
