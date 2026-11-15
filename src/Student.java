import java.util.Objects;

/**
 * An immutable student record.
 *
 * <p>Immutability matters here: {@link StudentDirectory#snapshot()} can safely
 * share these values because no caller can change a Student after it is created.
 */
public final class Student {

    private final String id;
    private final String name;
    private final double grade;

    /**
     * Creates a validated student record.
     *
     * @param id    non-blank student ID
     * @param name  non-blank student name
     * @param grade finite grade from 0.0 through 100.0
     * @throws IllegalArgumentException if any argument is invalid
     */
    public Student(String id, String name, double grade) {
        this.id = requireText(id, "id");
        this.name = requireText(name, "name");
        this.grade = requireGrade(grade);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getGrade() {
        return grade;
    }

    /**
     * Returns a new Student with a different grade. The current object is not
     * modified.
     */
    public Student withGrade(double newGrade) {
        return new Student(id, name, newGrade);
    }

    /**
     * Returns a new Student with a different name. The current object is not
     * modified.
     */
    public Student withName(String newName) {
        return new Student(id, newName, grade);
    }

    /**
     * Tells whether a grade is acceptable: finite and within 0..100.
     * Used by StudentDirectory.updateGrade so that an invalid update can be
     * rejected without constructing a temporary Student.
     */
    public static boolean isValidGrade(double grade) {
        return Double.isFinite(grade) && grade >= 0.0 && grade <= 100.0;
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be null or blank");
        }
        return value;
    }

    private static double requireGrade(double grade) {
        if (!isValidGrade(grade)) {
            throw new IllegalArgumentException("grade must be between 0 and 100, but was " + grade);
        }
        return grade;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Student)) {
            return false;
        }
        Student student = (Student) other;
        return id.equals(student.id)
                && name.equals(student.name)
                && Double.compare(grade, student.grade) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, grade);
    }

    @Override
    public String toString() {
        return "Student{id='" + id + "', name='" + name + "', grade=" + grade + "}";
    }
}
