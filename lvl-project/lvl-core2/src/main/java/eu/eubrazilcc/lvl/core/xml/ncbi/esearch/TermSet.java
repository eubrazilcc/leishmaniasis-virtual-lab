//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.07.23 at 01:05:13 PM CEST 
//


package eu.eubrazilcc.lvl.core.xml.ncbi.esearch;

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
    "term",
    "field",
    "count",
    "explode"
})
@XmlRootElement(name = "TermSet")
@Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:13+02:00", comments = "JAXB RI v2.2.11")
public class TermSet {

    @XmlElement(name = "Term", required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:13+02:00", comments = "JAXB RI v2.2.11")
    protected String term;
    @XmlElement(name = "Field", required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:13+02:00", comments = "JAXB RI v2.2.11")
    protected String field;
    @XmlElement(name = "Count", required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:13+02:00", comments = "JAXB RI v2.2.11")
    protected Count count;
    @XmlElement(name = "Explode", required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:13+02:00", comments = "JAXB RI v2.2.11")
    protected String explode;

    /**
     * Gets the value of the term property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:13+02:00", comments = "JAXB RI v2.2.11")
    public String getTerm() {
        return term;
    }

    /**
     * Sets the value of the term property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:13+02:00", comments = "JAXB RI v2.2.11")
    public void setTerm(String value) {
        this.term = value;
    }

    /**
     * Gets the value of the field property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:13+02:00", comments = "JAXB RI v2.2.11")
    public String getField() {
        return field;
    }

    /**
     * Sets the value of the field property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:13+02:00", comments = "JAXB RI v2.2.11")
    public void setField(String value) {
        this.field = value;
    }

    /**
     * Gets the value of the count property.
     * 
     * @return
     *     possible object is
     *     {@link Count }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:13+02:00", comments = "JAXB RI v2.2.11")
    public Count getCount() {
        return count;
    }

    /**
     * Sets the value of the count property.
     * 
     * @param value
     *     allowed object is
     *     {@link Count }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:13+02:00", comments = "JAXB RI v2.2.11")
    public void setCount(Count value) {
        this.count = value;
    }

    /**
     * Gets the value of the explode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:13+02:00", comments = "JAXB RI v2.2.11")
    public String getExplode() {
        return explode;
    }

    /**
     * Sets the value of the explode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:13+02:00", comments = "JAXB RI v2.2.11")
    public void setExplode(String value) {
        this.explode = value;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:13+02:00", comments = "JAXB RI v2.2.11")
    public TermSet withTerm(String value) {
        setTerm(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:13+02:00", comments = "JAXB RI v2.2.11")
    public TermSet withField(String value) {
        setField(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:13+02:00", comments = "JAXB RI v2.2.11")
    public TermSet withCount(Count value) {
        setCount(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:13+02:00", comments = "JAXB RI v2.2.11")
    public TermSet withExplode(String value) {
        setExplode(value);
        return this;
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:13+02:00", comments = "JAXB RI v2.2.11")
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:13+02:00", comments = "JAXB RI v2.2.11")
    public boolean equals(Object that) {
        return EqualsBuilder.reflectionEquals(this, that);
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:13+02:00", comments = "JAXB RI v2.2.11")
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

}
