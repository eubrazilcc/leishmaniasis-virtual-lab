//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.11.27 at 05:37:45 PM CET 
//


package eu.eubrazilcc.lvl.core.xml.ncbi.taxonomy;

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
    "classCDE",
    "dispName",
    "uniqueName"
})
@XmlRootElement(name = "Name")
@Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:45+01:00", comments = "JAXB RI v2.2.11")
public class Name {

    @XmlElement(name = "ClassCDE", required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:45+01:00", comments = "JAXB RI v2.2.11")
    protected String classCDE;
    @XmlElement(name = "DispName", required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:45+01:00", comments = "JAXB RI v2.2.11")
    protected String dispName;
    @XmlElement(name = "UniqueName")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:45+01:00", comments = "JAXB RI v2.2.11")
    protected String uniqueName;

    /**
     * Gets the value of the classCDE property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:45+01:00", comments = "JAXB RI v2.2.11")
    public String getClassCDE() {
        return classCDE;
    }

    /**
     * Sets the value of the classCDE property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:45+01:00", comments = "JAXB RI v2.2.11")
    public void setClassCDE(String value) {
        this.classCDE = value;
    }

    /**
     * Gets the value of the dispName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:45+01:00", comments = "JAXB RI v2.2.11")
    public String getDispName() {
        return dispName;
    }

    /**
     * Sets the value of the dispName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:45+01:00", comments = "JAXB RI v2.2.11")
    public void setDispName(String value) {
        this.dispName = value;
    }

    /**
     * Gets the value of the uniqueName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:45+01:00", comments = "JAXB RI v2.2.11")
    public String getUniqueName() {
        return uniqueName;
    }

    /**
     * Sets the value of the uniqueName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:45+01:00", comments = "JAXB RI v2.2.11")
    public void setUniqueName(String value) {
        this.uniqueName = value;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:45+01:00", comments = "JAXB RI v2.2.11")
    public Name withClassCDE(String value) {
        setClassCDE(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:45+01:00", comments = "JAXB RI v2.2.11")
    public Name withDispName(String value) {
        setDispName(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:45+01:00", comments = "JAXB RI v2.2.11")
    public Name withUniqueName(String value) {
        setUniqueName(value);
        return this;
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:45+01:00", comments = "JAXB RI v2.2.11")
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:45+01:00", comments = "JAXB RI v2.2.11")
    public boolean equals(Object that) {
        return EqualsBuilder.reflectionEquals(this, that);
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:45+01:00", comments = "JAXB RI v2.2.11")
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

}
