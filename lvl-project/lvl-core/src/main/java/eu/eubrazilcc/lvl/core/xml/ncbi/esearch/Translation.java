//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.03.26 at 04:30:37 PM CET 
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
    "from",
    "to"
})
@XmlRootElement(name = "Translation")
@Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-26T04:30:37+01:00", comments = "JAXB RI v2.2.11")
public class Translation {

    @XmlElement(name = "From", required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-26T04:30:37+01:00", comments = "JAXB RI v2.2.11")
    protected String from;
    @XmlElement(name = "To", required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-26T04:30:37+01:00", comments = "JAXB RI v2.2.11")
    protected String to;

    /**
     * Gets the value of the from property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-26T04:30:37+01:00", comments = "JAXB RI v2.2.11")
    public String getFrom() {
        return from;
    }

    /**
     * Sets the value of the from property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-26T04:30:37+01:00", comments = "JAXB RI v2.2.11")
    public void setFrom(String value) {
        this.from = value;
    }

    /**
     * Gets the value of the to property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-26T04:30:37+01:00", comments = "JAXB RI v2.2.11")
    public String getTo() {
        return to;
    }

    /**
     * Sets the value of the to property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-26T04:30:37+01:00", comments = "JAXB RI v2.2.11")
    public void setTo(String value) {
        this.to = value;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-26T04:30:37+01:00", comments = "JAXB RI v2.2.11")
    public Translation withFrom(String value) {
        setFrom(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-26T04:30:37+01:00", comments = "JAXB RI v2.2.11")
    public Translation withTo(String value) {
        setTo(value);
        return this;
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-26T04:30:37+01:00", comments = "JAXB RI v2.2.11")
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-26T04:30:37+01:00", comments = "JAXB RI v2.2.11")
    public boolean equals(Object that) {
        return EqualsBuilder.reflectionEquals(this, that);
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-26T04:30:37+01:00", comments = "JAXB RI v2.2.11")
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

}
