package bank.DB;

import java.sql.*;
import java.util.*;

public final class ResultSetUtils {

    private ResultSetUtils() {}

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