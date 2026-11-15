import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A keyed student directory.
 *
 * <p>Keys are student IDs; values are immutable {@link Student} records. The
 * backing HashMap is private. Callers receive copies through
 * {@link #snapshot()} and {@link #snapshotSortedById()}, so they cannot change
 * the directory by accident.
 *
 * <p>This class is not thread-safe. That is acceptable for the lab, which is a
 * single-threaded console application.
 */
public final class StudentDirectory {

    private final Map<String, Student> students = new HashMap<>();

    /**
     * Adds a student only when the ID is not already present.
     *
     * @return true if added; false if the ID already exists (the existing
     *         record is left unchanged)
     * @throws IllegalArgumentException if student is null
     */
    public boolean addStudent(Student student) {
        requireStudent(student);
        String id = student.getId();

        // The explicit containsKey check is the duplicate-key guard. A plain
        // put() would silently overwrite the old record, which the lab forbids.
        if (students.containsKey(id)) {
            return false;
        }
        students.put(id, student);
        return true;
    }

    /**
     * Returns the stored student, or null when the ID is absent.
     * The returned Student is immutable.
     */
    public Student getStudent(String id) {
        requireId(id);
        return students.get(id);
    }

    /** Returns true only when the ID is present. */
    public boolean containsStudent(String id) {
        requireId(id);
        return students.containsKey(id);
    }

    /**
     * Replaces an existing record. This method never inserts a new ID.
     *
     * @return true if replaced; false if the ID is absent
     */
    public boolean updateStudent(Student student) {
        requireStudent(student);
        return replaceExisting(student);
    }

    /**
     * Replaces only the grade of an existing student.
     *
     * <p>Contract: a missing ID is an expected condition and returns false.
     * An invalid grade is a programmer error and throws
     * IllegalArgumentException, exactly like the Student constructor.
     *
     * @return true if updated; false if the ID is absent
     * @throws IllegalArgumentException if the ID is null/blank or the grade is
     *         not finite and within 0..100
     */
    public boolean updateGrade(String id, double grade) {
        requireId(id);
        if (!Student.isValidGrade(grade)) {
            throw new IllegalArgumentException(
                    "grade must be between 0 and 100, but was " + grade);
        }
        Student existing = students.get(id);
        if (existing == null) {
            return false;
        }
        return replaceExisting(existing.withGrade(grade));
    }

    /**
     * Removes a record by ID.
     *
     * @return true if a record was removed; false if the ID was absent
     */
    public boolean removeStudent(String id) {
        requireId(id);
        return students.remove(id) != null;
    }

    /** Number of records currently stored. */
    public int size() {
        return students.size();
    }

    /**
     * Returns an unmodifiable copy of the directory.
     *
     * <p>The copy is the important part: later changes to the directory do not
     * appear in an old snapshot. The unmodifiable wrapper prevents callers from
     * using the snapshot to modify the copy, and the copy prevents them from
     * reaching the real backing map.
     */
    public Map<String, Student> snapshot() {
        Map<String, Student> copy = new HashMap<>(students);
        return Collections.unmodifiableMap(copy);
    }

    /**
     * Returns an unmodifiable list of records sorted by ID. Useful for stable
     * display because HashMap iteration order is not guaranteed.
     */
    public List<Student> snapshotSortedById() {
        List<Student> copy = new ArrayList<>(students.values());
        copy.sort(Comparator.comparing(Student::getId));
        return Collections.unmodifiableList(copy);
    }

    /**
     * Shared replacement step for updateStudent and updateGrade. The ID must
     * already exist; this method never inserts.
     */
    private boolean replaceExisting(Student student) {
        String id = student.getId();
        if (!students.containsKey(id)) {
            return false;
        }
        students.put(id, student);
        return true;
    }

    private static void requireStudent(Student student) {
        if (student == null) {
            throw new IllegalArgumentException("student must not be null");
        }
    }

    private static void requireId(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("student id must not be null or blank");
        }
    }
}
