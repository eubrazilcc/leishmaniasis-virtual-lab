//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.11.27 at 05:37:44 PM CET 
//


package eu.eubrazilcc.lvl.core.xml.ncbi.pubmed;

import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "value"
})
@XmlRootElement(name = "ELocationID")
@Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:44+01:00", comments = "JAXB RI v2.2.11")
public class ELocationID {

    @XmlAttribute(name = "EIdType", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:44+01:00", comments = "JAXB RI v2.2.11")
    protected String eIdType;
    @XmlAttribute(name = "ValidYN")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:44+01:00", comments = "JAXB RI v2.2.11")
    protected String validYN;
    @XmlValue
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:44+01:00", comments = "JAXB RI v2.2.11")
    protected String value;

    /**
     * Gets the value of the eIdType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:44+01:00", comments = "JAXB RI v2.2.11")
    public String getEIdType() {
        return eIdType;
    }

    /**
     * Sets the value of the eIdType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:44+01:00", comments = "JAXB RI v2.2.11")
    public void setEIdType(String value) {
        this.eIdType = value;
    }

    /**
     * Gets the value of the validYN property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:44+01:00", comments = "JAXB RI v2.2.11")
    public String getValidYN() {
        if (validYN == null) {
            return "Y";
        } else {
            return validYN;
        }
    }

    /**
     * Sets the value of the validYN property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:44+01:00", comments = "JAXB RI v2.2.11")
    public void setValidYN(String value) {
        this.validYN = value;
    }

    /**
     * Gets the value of the value property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:44+01:00", comments = "JAXB RI v2.2.11")
    public String getvalue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:44+01:00", comments = "JAXB RI v2.2.11")
    public void setvalue(String value) {
        this.value = value;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:44+01:00", comments = "JAXB RI v2.2.11")
    public ELocationID withEIdType(String value) {
        setEIdType(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:44+01:00", comments = "JAXB RI v2.2.11")
    public ELocationID withValidYN(String value) {
        setValidYN(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:44+01:00", comments = "JAXB RI v2.2.11")
    public ELocationID withvalue(String value) {
        setvalue(value);
        return this;
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:44+01:00", comments = "JAXB RI v2.2.11")
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:44+01:00", comments = "JAXB RI v2.2.11")
    public boolean equals(java.lang.Object that) {
        return EqualsBuilder.reflectionEquals(this, that);
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:44+01:00", comments = "JAXB RI v2.2.11")
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

}
