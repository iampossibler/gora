package org.gora.sql.statement;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map.Entry;

import org.gora.persistency.Persistent;
import org.gora.sql.store.Column;
import org.gora.sql.store.SqlMapping;
import org.gora.sql.store.SqlStore;

public class HSqlInsertUpdateStatement<K, T extends Persistent>
extends InsertUpdateStatement<K, T> {

  public HSqlInsertUpdateStatement(SqlStore<K, T> store, SqlMapping mapping,
      String tableName) {
    super(store, mapping, tableName);
  }

  private String getVariable(String columnName) {
    return "v_" + columnName;
  }

  @Override
  public PreparedStatement toStatement(Connection connection)
  throws SQLException {
    int i;

    StringBuilder buf = new StringBuilder("MERGE INTO ");
    buf.append(tableName).append(" USING (VALUES(");

    i = 0;
    for (Entry<String, ColumnData> e : columnMap.entrySet()) {
      Column column = e.getValue().column;
      if (i != 0) buf.append(",");
      buf.append("CAST(? AS ");
      buf.append(column.getJdbcType().toString());
      if (column.getScaleOrLength() > 0) {
        buf.append("(").append(column.getScaleOrLength()).append(")");
      }
      buf.append(")");
      i++;
    }
    buf.append(")) AS vals(");

    i = 0;
    for (String columnName : columnMap.keySet()) {
      if (i != 0) buf.append(",");
      buf.append(getVariable(columnName));
      i++;
    }

    buf.append(") ON ").append(tableName).append(".").append(mapping.getPrimaryColumnName()).append("=vals.");
    buf.append(getVariable(mapping.getPrimaryColumnName()));

    buf.append(" WHEN MATCHED THEN UPDATE SET ");
    i = 0;
    for (String columnName : columnMap.keySet()) {
      if (columnName.equals(mapping.getPrimaryColumnName())) {
        continue;
      }
      if (i != 0) { buf.append(","); }
      buf.append(tableName).append(".").append(columnName).append("=vals.");
      buf.append(getVariable(columnName));
      i++;
    }

    buf.append(" WHEN NOT MATCHED THEN INSERT (");
    i = 0;
    for (String columnName : columnMap.keySet()) {
      if (i != 0) { buf.append(","); }
      buf.append(columnName);
      i++;
    }
    i = 0;
    buf.append(") VALUES ");
    for (String columnName : columnMap.keySet()) {
      if (i != 0) { buf.append(","); }
      buf.append("vals.").append(getVariable(columnName));
      i++;
    }

    Column primaryColumn = mapping.getPrimaryColumn();
    PreparedStatement insert = connection.prepareStatement(buf.toString());
    int psIndex = 1;
    for (Entry<String, ColumnData> e : columnMap.entrySet()) {
      ColumnData cd = e.getValue();
      Column column = cd.column;
      if (column.getName().equals(primaryColumn.getName())) {
        Object key = columnMap.get(primaryColumn.getName()).object;
        if (primaryColumn.getScaleOrLength() > 0) {
          insert.setObject(psIndex++, key,
              primaryColumn.getJdbcType().getOrder(), primaryColumn.getScaleOrLength());
        } else {
          insert.setObject(psIndex++, key, primaryColumn.getJdbcType().getOrder());
        }
        continue;
      }
      try {
        store.setObject(insert, psIndex++, cd.object, cd.schema, cd.column);
      } catch (IOException ex) {
        throw new SQLException(ex);
      }
    }

    return insert;
  }
}
