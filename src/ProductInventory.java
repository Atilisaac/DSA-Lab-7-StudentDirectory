import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Product code to stock quantity, backed by HashMap<String, Integer>.
 *
 * <p>This is the workbook's Task 4 mini application.
 */
public final class ProductInventory {

    private final Map<String, Integer> stockByCode = new HashMap<>();

    /**
     * Adds a new product with its initial stock. Existing codes are rejected so
     * that a typo cannot silently erase stock. Use updateStock to change an
     * existing quantity.
     *
     * @return true if added; false if the code exists or the quantity is negative
     * @throws IllegalArgumentException if code is null or blank
     */
    public boolean addProduct(String code, int quantity) {
        requireCode(code);
        if (quantity < 0 || stockByCode.containsKey(code)) {
            return false;
        }
        stockByCode.put(code, quantity);
        return true;
    }

    /**
     * Sets the absolute stock level of an existing product.
     *
     * @return true if updated; false if the code is absent or quantity is negative
     */
    public boolean updateStock(String code, int quantity) {
        requireCode(code);
        if (quantity < 0 || !stockByCode.containsKey(code)) {
            return false;
        }
        stockByCode.put(code, quantity);
        return true;
    }

    /**
     * Returns the stock for a code, or -1 when the code is absent. The sentinel
     * is documented so callers do not treat it as a real quantity.
     */
    public int getStock(String code) {
        requireCode(code);
        Integer quantity = stockByCode.get(code);
        return quantity == null ? -1 : quantity;
    }

    /** Returns an unmodifiable copy of code-to-quantity entries. */
    public Map<String, Integer> snapshot() {
        return Collections.unmodifiableMap(new HashMap<>(stockByCode));
    }

    /** Formats all products in code order for stable console output. */
    public String describeAll() {
        List<String> codes = new ArrayList<>(stockByCode.keySet());
        Collections.sort(codes);

        StringBuilder result = new StringBuilder();
        for (String code : codes) {
            if (result.length() > 0) {
                result.append(System.lineSeparator());
            }
            result.append(code).append(": ").append(stockByCode.get(code));
        }
        return result.toString();
    }

    private static void requireCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("product code must not be null or blank");
        }
    }
}
