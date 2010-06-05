package org.gora.sql.statement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.gora.util.StringUtils;

/**
 * An SQL INSERT statement, for generating a Prepared Statement
 */
public class InsertStatement {
  
  private String tableName;
  private List<String> columnNames;
  
  public InsertStatement(String tableName) {
    this.tableName = tableName;
    this.columnNames = new ArrayList<String>();
  }
  
  public InsertStatement(String tableName, String... columnNames) {
    this.tableName = tableName;
    this.columnNames = Arrays.asList(columnNames);
  }
  
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder("INSERT INTO ");
    builder.append(tableName);
    
    StringUtils.join(builder.append(" ("), columnNames).append(" )");
    
    builder.append("VALUES (");
    for(int i=0;i<columnNames.size();i++) {
      if (i != 0) builder.append(",");
      builder.append("?");
    }
    
    builder.append(");");
    
    return builder.toString();
  }

  /**
   * @return the tableName
   */
  public String getTableName() {
    return tableName;
  }

  /**
   * @param tableName the tableName to set
   */
  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

  /**
   * @return the columnNames
   */
  public List<String> getColumnNames() {
    return columnNames;
  }

  /**
   * @param columnNames the columnNames to set
   */
  public void setColumnNames(String... columnNames) {
    this.columnNames = Arrays.asList(columnNames);
  }
  
  public void addColumnName(String columnName) {
    this.columnNames.add(columnName);
  }
  
  public void clear() {
    this.columnNames.clear();
  }
}