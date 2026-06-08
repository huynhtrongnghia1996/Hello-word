package com.logtechub.automation.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class QueryExecutor {
    public List<Map<String, Object>> queryForList(String sql, Object... params) throws SQLException {
        try (Connection connection = DatabaseConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bindParams(statement, params);
            try (ResultSet resultSet = statement.executeQuery()) {
                return toList(resultSet);
            }
        }
    }

    public Map<String, Object> queryForOne(String sql, Object... params) throws SQLException {
        List<Map<String, Object>> rows = queryForList(sql, params);
        return rows.isEmpty() ? Map.of() : rows.get(0);
    }

    public int executeUpdate(String sql, Object... params) throws SQLException {
        try (Connection connection = DatabaseConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bindParams(statement, params);
            return statement.executeUpdate();
        }
    }

    private void bindParams(PreparedStatement statement, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            statement.setObject(i + 1, params[i]);
        }
    }

    private List<Map<String, Object>> toList(ResultSet resultSet) throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();
        List<Map<String, Object>> rows = new ArrayList<>();

        while (resultSet.next()) {
            Map<String, Object> row = new LinkedHashMap<>();
            for (int column = 1; column <= columnCount; column++) {
                row.put(metaData.getColumnLabel(column), resultSet.getObject(column));
            }
            rows.add(row);
        }

        return rows;
    }
}
