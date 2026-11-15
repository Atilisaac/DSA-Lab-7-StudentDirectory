import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Workbook Practice Problem 2: library book tracker.
 *
 * <p>Key = book title, value = copies available. This reuses the same
 * key-to-count pattern as ProductInventory but with borrow/return operations.
 */
public final class LibraryBookTracker {

    private final Map<String, Integer> copiesByTitle = new HashMap<>();

    /** Adds a title with an initial number of copies. Duplicate titles are rejected. */
    public boolean addBook(String title, int copies) {
        requireTitle(title);
        if (copies < 0 || copiesByTitle.containsKey(title)) {
            return false;
        }
        copiesByTitle.put(title, copies);
        return true;
    }

    /** Removes a title entirely. Returns false when the title is absent. */
    public boolean removeBook(String title) {
        requireTitle(title);
        return copiesByTitle.remove(title) != null;
    }

    /** Borrows one copy. Returns false when the title is absent or has no copies. */
    public boolean borrow(String title) {
        requireTitle(title);
        Integer copies = copiesByTitle.get(title);
        if (copies == null || copies == 0) {
            return false;
        }
        copiesByTitle.put(title, copies - 1);
        return true;
    }

    /** Returns one copy. Returns false when the title is not tracked. */
    public boolean returnBook(String title) {
        requireTitle(title);
        Integer copies = copiesByTitle.get(title);
        if (copies == null) {
            return false;
        }
        copiesByTitle.put(title, copies + 1);
        return true;
    }

    /** Returns copies available, or -1 when the title is absent. */
    public int getCopies(String title) {
        requireTitle(title);
        Integer copies = copiesByTitle.get(title);
        return copies == null ? -1 : copies;
    }

    /** Returns an unmodifiable copy of title-to-copies entries. */
    public Map<String, Integer> snapshot() {
        return Collections.unmodifiableMap(new HashMap<>(copiesByTitle));
    }

    /** Formats all books in title order for stable console output. */
    public String describeAll() {
        List<String> titles = new ArrayList<>(copiesByTitle.keySet());
        Collections.sort(titles);

        StringBuilder result = new StringBuilder();
        for (String title : titles) {
            if (result.length() > 0) {
                result.append(System.lineSeparator());
            }
            result.append(title).append(": ").append(copiesByTitle.get(title));
        }
        return result.toString();
    }

    private static void requireTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title must not be null or blank");
        }
    }
}
