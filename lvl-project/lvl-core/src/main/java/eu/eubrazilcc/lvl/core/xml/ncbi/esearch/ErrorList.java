//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.02.17 at 10:57:46 PM CET 
//


package eu.eubrazilcc.lvl.core.xml.ncbi.esearch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "phraseNotFound",
    "fieldNotFound"
})
@XmlRootElement(name = "ErrorList")
@Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T10:57:46+01:00", comments = "JAXB RI v2.2.11")
public class ErrorList {

    @XmlElement(name = "PhraseNotFound")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T10:57:46+01:00", comments = "JAXB RI v2.2.11")
    protected List<PhraseNotFound> phraseNotFound;
    @XmlElement(name = "FieldNotFound")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T10:57:46+01:00", comments = "JAXB RI v2.2.11")
    protected List<FieldNotFound> fieldNotFound;

    /**
     * Gets the value of the phraseNotFound property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the phraseNotFound property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPhraseNotFound().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PhraseNotFound }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T10:57:46+01:00", comments = "JAXB RI v2.2.11")
    public List<PhraseNotFound> getPhraseNotFound() {
        if (phraseNotFound == null) {
            phraseNotFound = new ArrayList<PhraseNotFound>();
        }
        return this.phraseNotFound;
    }

    /**
     * Gets the value of the fieldNotFound property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the fieldNotFound property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFieldNotFound().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link FieldNotFound }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T10:57:46+01:00", comments = "JAXB RI v2.2.11")
    public List<FieldNotFound> getFieldNotFound() {
        if (fieldNotFound == null) {
            fieldNotFound = new ArrayList<FieldNotFound>();
        }
        return this.fieldNotFound;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T10:57:46+01:00", comments = "JAXB RI v2.2.11")
    public ErrorList withPhraseNotFound(PhraseNotFound... values) {
        if (values!= null) {
            for (PhraseNotFound value: values) {
                getPhraseNotFound().add(value);
            }
        }
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T10:57:46+01:00", comments = "JAXB RI v2.2.11")
    public ErrorList withPhraseNotFound(Collection<PhraseNotFound> values) {
        if (values!= null) {
            getPhraseNotFound().addAll(values);
        }
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T10:57:46+01:00", comments = "JAXB RI v2.2.11")
    public ErrorList withFieldNotFound(FieldNotFound... values) {
        if (values!= null) {
            for (FieldNotFound value: values) {
                getFieldNotFound().add(value);
            }
        }
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T10:57:46+01:00", comments = "JAXB RI v2.2.11")
    public ErrorList withFieldNotFound(Collection<FieldNotFound> values) {
        if (values!= null) {
            getFieldNotFound().addAll(values);
        }
        return this;
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T10:57:46+01:00", comments = "JAXB RI v2.2.11")
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T10:57:46+01:00", comments = "JAXB RI v2.2.11")
    public boolean equals(Object that) {
        return EqualsBuilder.reflectionEquals(this, that);
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T10:57:46+01:00", comments = "JAXB RI v2.2.11")
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

}
