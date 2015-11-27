//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.11.27 at 05:37:46 PM CET 
//


package eu.eubrazilcc.lvl.core.xml.tdwg.tapir;

import java.math.BigInteger;
import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;


/**
 * Summary about inventory and search results. The first index of a
 * 				record is 0. The number of records actually being  returned is given in totalReturned.
 * 				If counting was requested the totalMatched gives the "estimated" number of total
 * 				matching records - not necessarily the number of valid records that can possibly be
 * 				returned by paging through the entire record set.
 * 
 * <p>Java class for resultSummaryType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="resultSummaryType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="start" use="required" type="{http://www.w3.org/2001/XMLSchema}integer" /&gt;
 *       &lt;attribute name="next" type="{http://www.w3.org/2001/XMLSchema}integer" /&gt;
 *       &lt;attribute name="totalReturned" use="required" type="{http://www.w3.org/2001/XMLSchema}integer" /&gt;
 *       &lt;attribute name="totalMatched" type="{http://www.w3.org/2001/XMLSchema}integer" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "resultSummaryType")
@Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
public class ResultSummaryType {

    @XmlAttribute(name = "start", required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
    protected BigInteger start;
    @XmlAttribute(name = "next")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
    protected BigInteger next;
    @XmlAttribute(name = "totalReturned", required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
    protected BigInteger totalReturned;
    @XmlAttribute(name = "totalMatched")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
    protected BigInteger totalMatched;

    /**
     * Gets the value of the start property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
    public BigInteger getStart() {
        return start;
    }

    /**
     * Sets the value of the start property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
    public void setStart(BigInteger value) {
        this.start = value;
    }

    /**
     * Gets the value of the next property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
    public BigInteger getNext() {
        return next;
    }

    /**
     * Sets the value of the next property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
    public void setNext(BigInteger value) {
        this.next = value;
    }

    /**
     * Gets the value of the totalReturned property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
    public BigInteger getTotalReturned() {
        return totalReturned;
    }

    /**
     * Sets the value of the totalReturned property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
    public void setTotalReturned(BigInteger value) {
        this.totalReturned = value;
    }

    /**
     * Gets the value of the totalMatched property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
    public BigInteger getTotalMatched() {
        return totalMatched;
    }

    /**
     * Sets the value of the totalMatched property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
    public void setTotalMatched(BigInteger value) {
        this.totalMatched = value;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
    public ResultSummaryType withStart(BigInteger value) {
        setStart(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
    public ResultSummaryType withNext(BigInteger value) {
        setNext(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
    public ResultSummaryType withTotalReturned(BigInteger value) {
        setTotalReturned(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
    public ResultSummaryType withTotalMatched(BigInteger value) {
        setTotalMatched(value);
        return this;
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
    public boolean equals(Object that) {
        return EqualsBuilder.reflectionEquals(this, that);
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

}
