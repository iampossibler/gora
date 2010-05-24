package org.gora.mapreduce;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.OutputCommitter;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.gora.persistency.Persistent;
import org.gora.store.DataStore;
import org.gora.store.DataStoreFactory;
import org.gora.store.FileBackedDataStore;

public class GoraOutputFormat<K, T extends Persistent>
  extends OutputFormat<K, T> {

  public static final String DATA_STORE_CLASS = "gora.outputformat.datastore.class";
  
  public static final String OUTPUT_KEY_CLASS   = "gora.outputformat.key.class";

  public static final String OUTPUT_VALUE_CLASS = "gora.outputformat.value.class";

  @Override
  public void checkOutputSpecs(JobContext context)
  throws IOException, InterruptedException { }

  @Override
  public OutputCommitter getOutputCommitter(TaskAttemptContext context)
  throws IOException, InterruptedException {
    return new NullOutputCommitter();
  }

  @SuppressWarnings("unchecked")
  private void setOutputPath(DataStore<K,T> store, TaskAttemptContext context) {
    if(store instanceof FileBackedDataStore) {
      FileBackedDataStore<K, T> fileStore = (FileBackedDataStore<K, T>) store;
      String uniqueName = FileOutputFormat.getUniqueFile(context, "part", "");
      
      //if file store output is not set, then get the output from FileOutputFormat 
      if(fileStore.getOutputPath() == null) {
        fileStore.setOutputPath(FileOutputFormat.getOutputPath(context).toString());
      }
      
      //set the unique name of the data file
      String path = fileStore.getOutputPath();
      fileStore.setOutputPath( path + Path.SEPARATOR  + uniqueName);
    } 
  }
  
  @Override
  @SuppressWarnings("unchecked")
  public RecordWriter<K, T> getRecordWriter(TaskAttemptContext context)
      throws IOException, InterruptedException {
    Configuration conf = context.getConfiguration();
    Class<? extends DataStore<K,T>> dataStoreClass 
      = (Class<? extends DataStore<K,T>>) conf.getClass(DATA_STORE_CLASS, null);
    Class<K> keyClass = (Class<K>) conf.getClass(OUTPUT_KEY_CLASS, null);
    Class<T> rowClass = (Class<T>) conf.getClass(OUTPUT_VALUE_CLASS, null);
    final DataStore<K, T> store =
      DataStoreFactory.getDataStore(dataStoreClass, keyClass, rowClass);
    
    setOutputPath(store, context);
    
    return new RecordWriter<K, T>() {
      @Override
      public void close(TaskAttemptContext context) throws IOException,
          InterruptedException {
        store.close();
      }

      @Override
      public void write(K key, T value)
      throws IOException, InterruptedException {
        store.put(key, value);
      }
    };
  }

  /**
   * Sets the output parameters for the job 
   * @param job the job to set the properties for
   * @param dataStore the datastore as the output
   * @param reuseObjects whether to reuse objects in serialization
   */
  public static <K2, V2 extends Persistent> void setOutput(Job job, 
      DataStore<K2,V2> dataStore, boolean reuseObjects) {
    
    Configuration conf = job.getConfiguration();
    
    GoraMapReduceUtils.setIOSerializations(conf, reuseObjects);
    
    job.setOutputFormatClass(GoraOutputFormat.class);
    conf.setClass(GoraOutputFormat.DATA_STORE_CLASS
        , dataStore.getClass(), DataStore.class);
    conf.setClass(GoraOutputFormat.OUTPUT_KEY_CLASS,
        dataStore.getKeyClass(), Object.class);
    conf.setClass(GoraOutputFormat.OUTPUT_VALUE_CLASS,
        dataStore.getPersistentClass(), Persistent.class);
  }
}
