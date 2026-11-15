import java.util.Map;

/**
 * Console entry point for the Lab 7 demonstration.
 *
 * <p>Run with: {@code java -cp out Lab7Demo}
 */
public final class Lab7Demo {

    public static void main(String[] args) {
        System.out.println("Lab 7 - HashMap and Key-Value Structures");
        System.out.println("Student Directory demo");

        WorkbookActivities.task1HashMapBasics();
        WorkbookActivities.task2IterateEntries();
        WorkbookActivities.task3HashMapOfObjects();
        WorkbookActivities.task4ProductInventory();
        WorkbookActivities.practiceProblem2Library();

        System.out.println();
        System.out.println("=== Character frequency counter ===");
        Map<Character, Integer> counts = CharacterFrequencyCounter.count("BANANA");
        System.out.println("Input: BANANA");
        System.out.println("Counts: " + CharacterFrequencyCounter.format(counts));

        Map<Character, Integer> sentenceCounts =
                CharacterFrequencyCounter.count("Aa  a!");
        System.out.println();
        System.out.println("Counting rule check: all characters are counted exactly.");
        System.out.println("Input: Aa  a! (two spaces)");
        System.out.println("Counts: " + CharacterFrequencyCounter.format(sentenceCounts));
    }

    private Lab7Demo() {
        // utility class
    }
}
