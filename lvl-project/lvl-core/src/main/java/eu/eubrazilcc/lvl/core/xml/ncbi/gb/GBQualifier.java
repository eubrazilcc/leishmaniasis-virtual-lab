//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.04.22 at 01:25:23 PM CEST 
//


package eu.eubrazilcc.lvl.core.xml.ncbi.gb;

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
    "gbQualifierName",
    "gbQualifierValue"
})
@XmlRootElement(name = "GBQualifier")
@Generated(value = "com.sun.tools.xjc.Driver", date = "2015-04-22T01:25:23+02:00", comments = "JAXB RI v2.2.11")
public class GBQualifier {

    @XmlElement(name = "GBQualifier_name", required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-04-22T01:25:23+02:00", comments = "JAXB RI v2.2.11")
    protected String gbQualifierName;
    @XmlElement(name = "GBQualifier_value")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-04-22T01:25:23+02:00", comments = "JAXB RI v2.2.11")
    protected String gbQualifierValue;

    /**
     * Gets the value of the gbQualifierName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-04-22T01:25:23+02:00", comments = "JAXB RI v2.2.11")
    public String getGBQualifierName() {
        return gbQualifierName;
    }

    /**
     * Sets the value of the gbQualifierName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-04-22T01:25:23+02:00", comments = "JAXB RI v2.2.11")
    public void setGBQualifierName(String value) {
        this.gbQualifierName = value;
    }

    /**
     * Gets the value of the gbQualifierValue property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-04-22T01:25:23+02:00", comments = "JAXB RI v2.2.11")
    public String getGBQualifierValue() {
        return gbQualifierValue;
    }

    /**
     * Sets the value of the gbQualifierValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-04-22T01:25:23+02:00", comments = "JAXB RI v2.2.11")
    public void setGBQualifierValue(String value) {
        this.gbQualifierValue = value;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-04-22T01:25:23+02:00", comments = "JAXB RI v2.2.11")
    public GBQualifier withGBQualifierName(String value) {
        setGBQualifierName(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-04-22T01:25:23+02:00", comments = "JAXB RI v2.2.11")
    public GBQualifier withGBQualifierValue(String value) {
        setGBQualifierValue(value);
        return this;
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-04-22T01:25:23+02:00", comments = "JAXB RI v2.2.11")
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-04-22T01:25:23+02:00", comments = "JAXB RI v2.2.11")
    public boolean equals(Object that) {
        return EqualsBuilder.reflectionEquals(this, that);
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-04-22T01:25:23+02:00", comments = "JAXB RI v2.2.11")
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

}
