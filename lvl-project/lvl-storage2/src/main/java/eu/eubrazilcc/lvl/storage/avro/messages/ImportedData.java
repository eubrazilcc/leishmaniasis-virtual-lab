/**
 * Autogenerated by Avro
 * 
 * DO NOT EDIT DIRECTLY
 */
package eu.eubrazilcc.lvl.storage.avro.messages;  
@SuppressWarnings("all")
@org.apache.avro.specific.AvroGenerated
public class ImportedData extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"ImportedData\",\"namespace\":\"eu.eubrazilcc.lvl.storage.avro.messages\",\"fields\":[{\"name\":\"datasource\",\"type\":\"string\"},{\"name\":\"count\",\"type\":\"int\",\"default\":0},{\"name\":\"hasError\",\"type\":\"boolean\",\"default\":false},{\"name\":\"errorMessage\",\"type\":\"string\",\"default\":\"\"}]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }
   private java.lang.CharSequence datasource;
   private int count;
   private boolean hasError;
   private java.lang.CharSequence errorMessage;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use <code>newBuilder()</code>. 
   */
  public ImportedData() {}

  /**
   * All-args constructor.
   */
  public ImportedData(java.lang.CharSequence datasource, java.lang.Integer count, java.lang.Boolean hasError, java.lang.CharSequence errorMessage) {
    this.datasource = datasource;
    this.count = count;
    this.hasError = hasError;
    this.errorMessage = errorMessage;
  }

  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  // Used by DatumWriter.  Applications should not call. 
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return datasource;
    case 1: return count;
    case 2: return hasError;
    case 3: return errorMessage;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }
  // Used by DatumReader.  Applications should not call. 
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: datasource = (java.lang.CharSequence)value$; break;
    case 1: count = (java.lang.Integer)value$; break;
    case 2: hasError = (java.lang.Boolean)value$; break;
    case 3: errorMessage = (java.lang.CharSequence)value$; break;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  /**
   * Gets the value of the 'datasource' field.
   */
  public java.lang.CharSequence getDatasource() {
    return datasource;
  }

  /**
   * Sets the value of the 'datasource' field.
   * @param value the value to set.
   */
  public void setDatasource(java.lang.CharSequence value) {
    this.datasource = value;
  }

  /**
   * Gets the value of the 'count' field.
   */
  public java.lang.Integer getCount() {
    return count;
  }

  /**
   * Sets the value of the 'count' field.
   * @param value the value to set.
   */
  public void setCount(java.lang.Integer value) {
    this.count = value;
  }

  /**
   * Gets the value of the 'hasError' field.
   */
  public java.lang.Boolean getHasError() {
    return hasError;
  }

  /**
   * Sets the value of the 'hasError' field.
   * @param value the value to set.
   */
  public void setHasError(java.lang.Boolean value) {
    this.hasError = value;
  }

  /**
   * Gets the value of the 'errorMessage' field.
   */
  public java.lang.CharSequence getErrorMessage() {
    return errorMessage;
  }

  /**
   * Sets the value of the 'errorMessage' field.
   * @param value the value to set.
   */
  public void setErrorMessage(java.lang.CharSequence value) {
    this.errorMessage = value;
  }

  /** Creates a new ImportedData RecordBuilder */
  public static eu.eubrazilcc.lvl.storage.avro.messages.ImportedData.Builder newBuilder() {
    return new eu.eubrazilcc.lvl.storage.avro.messages.ImportedData.Builder();
  }
  
  /** Creates a new ImportedData RecordBuilder by copying an existing Builder */
  public static eu.eubrazilcc.lvl.storage.avro.messages.ImportedData.Builder newBuilder(eu.eubrazilcc.lvl.storage.avro.messages.ImportedData.Builder other) {
    return new eu.eubrazilcc.lvl.storage.avro.messages.ImportedData.Builder(other);
  }
  
  /** Creates a new ImportedData RecordBuilder by copying an existing ImportedData instance */
  public static eu.eubrazilcc.lvl.storage.avro.messages.ImportedData.Builder newBuilder(eu.eubrazilcc.lvl.storage.avro.messages.ImportedData other) {
    return new eu.eubrazilcc.lvl.storage.avro.messages.ImportedData.Builder(other);
  }
  
  /**
   * RecordBuilder for ImportedData instances.
   */
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<ImportedData>
    implements org.apache.avro.data.RecordBuilder<ImportedData> {

    private java.lang.CharSequence datasource;
    private int count;
    private boolean hasError;
    private java.lang.CharSequence errorMessage;

    /** Creates a new Builder */
    private Builder() {
      super(eu.eubrazilcc.lvl.storage.avro.messages.ImportedData.SCHEMA$);
    }
    
    /** Creates a Builder by copying an existing Builder */
    private Builder(eu.eubrazilcc.lvl.storage.avro.messages.ImportedData.Builder other) {
      super(other);
      if (isValidValue(fields()[0], other.datasource)) {
        this.datasource = data().deepCopy(fields()[0].schema(), other.datasource);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.count)) {
        this.count = data().deepCopy(fields()[1].schema(), other.count);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.hasError)) {
        this.hasError = data().deepCopy(fields()[2].schema(), other.hasError);
        fieldSetFlags()[2] = true;
      }
      if (isValidValue(fields()[3], other.errorMessage)) {
        this.errorMessage = data().deepCopy(fields()[3].schema(), other.errorMessage);
        fieldSetFlags()[3] = true;
      }
    }
    
    /** Creates a Builder by copying an existing ImportedData instance */
    private Builder(eu.eubrazilcc.lvl.storage.avro.messages.ImportedData other) {
            super(eu.eubrazilcc.lvl.storage.avro.messages.ImportedData.SCHEMA$);
      if (isValidValue(fields()[0], other.datasource)) {
        this.datasource = data().deepCopy(fields()[0].schema(), other.datasource);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.count)) {
        this.count = data().deepCopy(fields()[1].schema(), other.count);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.hasError)) {
        this.hasError = data().deepCopy(fields()[2].schema(), other.hasError);
        fieldSetFlags()[2] = true;
      }
      if (isValidValue(fields()[3], other.errorMessage)) {
        this.errorMessage = data().deepCopy(fields()[3].schema(), other.errorMessage);
        fieldSetFlags()[3] = true;
      }
    }

    /** Gets the value of the 'datasource' field */
    public java.lang.CharSequence getDatasource() {
      return datasource;
    }
    
    /** Sets the value of the 'datasource' field */
    public eu.eubrazilcc.lvl.storage.avro.messages.ImportedData.Builder setDatasource(java.lang.CharSequence value) {
      validate(fields()[0], value);
      this.datasource = value;
      fieldSetFlags()[0] = true;
      return this; 
    }
    
    /** Checks whether the 'datasource' field has been set */
    public boolean hasDatasource() {
      return fieldSetFlags()[0];
    }
    
    /** Clears the value of the 'datasource' field */
    public eu.eubrazilcc.lvl.storage.avro.messages.ImportedData.Builder clearDatasource() {
      datasource = null;
      fieldSetFlags()[0] = false;
      return this;
    }

    /** Gets the value of the 'count' field */
    public java.lang.Integer getCount() {
      return count;
    }
    
    /** Sets the value of the 'count' field */
    public eu.eubrazilcc.lvl.storage.avro.messages.ImportedData.Builder setCount(int value) {
      validate(fields()[1], value);
      this.count = value;
      fieldSetFlags()[1] = true;
      return this; 
    }
    
    /** Checks whether the 'count' field has been set */
    public boolean hasCount() {
      return fieldSetFlags()[1];
    }
    
    /** Clears the value of the 'count' field */
    public eu.eubrazilcc.lvl.storage.avro.messages.ImportedData.Builder clearCount() {
      fieldSetFlags()[1] = false;
      return this;
    }

    /** Gets the value of the 'hasError' field */
    public java.lang.Boolean getHasError() {
      return hasError;
    }
    
    /** Sets the value of the 'hasError' field */
    public eu.eubrazilcc.lvl.storage.avro.messages.ImportedData.Builder setHasError(boolean value) {
      validate(fields()[2], value);
      this.hasError = value;
      fieldSetFlags()[2] = true;
      return this; 
    }
    
    /** Checks whether the 'hasError' field has been set */
    public boolean hasHasError() {
      return fieldSetFlags()[2];
    }
    
    /** Clears the value of the 'hasError' field */
    public eu.eubrazilcc.lvl.storage.avro.messages.ImportedData.Builder clearHasError() {
      fieldSetFlags()[2] = false;
      return this;
    }

    /** Gets the value of the 'errorMessage' field */
    public java.lang.CharSequence getErrorMessage() {
      return errorMessage;
    }
    
    /** Sets the value of the 'errorMessage' field */
    public eu.eubrazilcc.lvl.storage.avro.messages.ImportedData.Builder setErrorMessage(java.lang.CharSequence value) {
      validate(fields()[3], value);
      this.errorMessage = value;
      fieldSetFlags()[3] = true;
      return this; 
    }
    
    /** Checks whether the 'errorMessage' field has been set */
    public boolean hasErrorMessage() {
      return fieldSetFlags()[3];
    }
    
    /** Clears the value of the 'errorMessage' field */
    public eu.eubrazilcc.lvl.storage.avro.messages.ImportedData.Builder clearErrorMessage() {
      errorMessage = null;
      fieldSetFlags()[3] = false;
      return this;
    }

    @Override
    public ImportedData build() {
      try {
        ImportedData record = new ImportedData();
        record.datasource = fieldSetFlags()[0] ? this.datasource : (java.lang.CharSequence) defaultValue(fields()[0]);
        record.count = fieldSetFlags()[1] ? this.count : (java.lang.Integer) defaultValue(fields()[1]);
        record.hasError = fieldSetFlags()[2] ? this.hasError : (java.lang.Boolean) defaultValue(fields()[2]);
        record.errorMessage = fieldSetFlags()[3] ? this.errorMessage : (java.lang.CharSequence) defaultValue(fields()[3]);
        return record;
      } catch (Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }
}
