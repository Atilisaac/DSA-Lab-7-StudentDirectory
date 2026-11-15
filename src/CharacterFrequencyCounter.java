import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Counts character occurrences with HashMap<Character, Integer>.
 *
 * <p>Counting rule: every UTF-16 char is counted exactly as it appears. The
 * count is case-sensitive, and spaces, punctuation, and digits are included.
 * There is no trimming, lower-casing, or letter-only filtering. An emoji
 * represented by a surrogate pair is two char values and is counted as two
 * entries.
 */
public final class CharacterFrequencyCounter {

    /**
     * Counts each character in text.
     *
     * @param text text to inspect; must not be null (empty is allowed)
     * @return an unmodifiable map of character to occurrence count
     * @throws IllegalArgumentException if text is null
     */
    public static Map<Character, Integer> count(String text) {
        if (text == null) {
            throw new IllegalArgumentException("text must not be null");
        }

        Map<Character, Integer> frequencies = new HashMap<>();
        for (int i = 0; i < text.length(); i++) {
            char character = text.charAt(i);
            int previousCount = frequencies.getOrDefault(character, 0);
            frequencies.put(character, previousCount + 1);
        }
        return Collections.unmodifiableMap(frequencies);
    }

    /**
     * Formats a frequency map in ascending character order, for example
     * {@code "A=3, B=1, N=2"}. Sorting is only for readable output; lookup in
     * the original HashMap is unaffected.
     */
    public static String format(Map<Character, Integer> frequencies) {
        if (frequencies == null) {
            throw new IllegalArgumentException("frequencies must not be null");
        }

        List<Character> characters = new ArrayList<>(frequencies.keySet());
        Collections.sort(characters);

        StringBuilder result = new StringBuilder();
        for (Character character : characters) {
            if (result.length() > 0) {
                result.append(", ");
            }
            result.append(displayCharacter(character))
                    .append('=')
                    .append(frequencies.get(character));
        }
        return result.toString();
    }

    /** Makes spaces visible in formatted output without changing the count. */
    private static String displayCharacter(char character) {
        if (character == ' ') {
            return "' '";
        }
        if (character == '\n') {
            return "\\n";
        }
        if (character == '\t') {
            return "\\t";
        }
        return String.valueOf(character);
    }

    private CharacterFrequencyCounter() {
        // utility class
    }
}
