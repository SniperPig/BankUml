package bank.DB;

import java.sql.*;
import java.util.*;

/**
 * Utility class for converting {@link ResultSet} instances into
 * Java collections that are easier to work with.
 * <p>
 * This class is {@code final} and has a private constructor to prevent
 * instantiation; all functionality is exposed through static methods.
 */
public final class ResultSetUtils {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private ResultSetUtils() {}

    /**
     * Converts the given {@link ResultSet} into a {@link List} of {@link Map} objects.
     * <p>
     * Each row in the result set becomes one {@code Map<String, Object>} entry in
     * the resulting list. The map keys are the column labels (as returned by
     * {@link ResultSetMetaData#getColumnLabel(int)}), and the values are the
     * column values as returned by {@link ResultSet#getObject(int)}.
     *
     * @param rs the result set to convert; must be positioned before the first row
     * @return a list of maps representing all rows in the result set; never {@code null}
     * @throws SQLException if a database access error occurs while reading the result set
     */
    public static List<Map<String, Object>> toList(ResultSet rs) throws SQLException {
        List<Map<String, Object>> rows = new ArrayList<>();
        ResultSetMetaData md = rs.getMetaData();
        int cols = md.getColumnCount();

        while (rs.next()) {
            Map<String, Object> row = new HashMap<>();
            for (int i = 1; i <= cols; i++) {
                String colName = md.getColumnLabel(i);
                Object value   = rs.getObject(i);
                row.put(colName, value);
            }
            rows.add(row);
        }
        return rows;
    }
}
