package org.gora.persistency.impl;

import org.apache.avro.specific.SpecificRecord;
import org.gora.persistency.Persistent;
import org.gora.persistency.StateManager;

/**
 * Base classs implementing common functionality for Persistent
 * classes.
 */
public abstract class PersistentBase implements Persistent {
  
  private StateManager stateManager;
  
  protected PersistentBase() {
    this(new StateManagerImpl());
  }
  
  protected PersistentBase(StateManager stateManager) {
    this.stateManager = stateManager;
    stateManager.setManagedPersistent(this);
  }

  @Override
  public StateManager getStateManager() {
    return stateManager;
  }
  
  @Override
  public boolean isNew() {
    return getStateManager().isNew(this);
  }
  
  @Override
  public void setNew() {
    getStateManager().setNew(this);
  }
  
  @Override
  public void clearNew() {
    getStateManager().clearNew(this);
  }
  
  @Override
  public boolean isDirty() {
    return getStateManager().isDirty(this);
  }
  
  @Override
  public boolean isDirty(int fieldNum) {
    return getStateManager().isDirty(this, fieldNum);
  }
  
  @Override
  public void setDirty() {
    getStateManager().setDirty(this);
  }
  
  @Override
  public void setDirty(int fieldNum) {
    getStateManager().setDirty(this, fieldNum);
  }
  
  @Override
  public void clearDirty() {
    getStateManager().clearDirty(this);
  }
  
  @Override
  public boolean isReadable(int fieldNum) {
    return getStateManager().isReadable(this, fieldNum);
  }
  
  @Override
  public void setReadable(int fieldNum) {
    getStateManager().setReadable(this, fieldNum);
  }
  
  @Override
  public void clearReadable() {
    getStateManager().clearReadable(this);
  }
  
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof SpecificRecord)) return false;

    SpecificRecord r2 = (SpecificRecord)o;
    if (!this.getSchema().equals(r2.getSchema())) return false;

    int end = this.getSchema().getFields().size();
    for (int i = 0; i < end; i++) {
      Object v1 = this.get(i);
      Object v2 = r2.get(i);
      if (v1 == null) {
        if (v2 != null) return false;
      } else {
        if (!v1.equals(v2)) return false;
      }
    }
    return true;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    int end = this.getSchema().getFields().size();
    for (int i = 0; i < end; i++) {
      Object o = get(i);
      result = prime * result + ((o == null) ? 0 : o.hashCode());
    }
    return result;
  }
}