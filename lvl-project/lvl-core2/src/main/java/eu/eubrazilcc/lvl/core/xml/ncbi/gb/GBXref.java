//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.07.15 at 10:23:53 AM CEST 
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
    "gbXrefDbname",
    "gbXrefId"
})
@XmlRootElement(name = "GBXref")
@Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-15T10:23:53+02:00", comments = "JAXB RI v2.2.11")
public class GBXref {

    @XmlElement(name = "GBXref_dbname", required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-15T10:23:53+02:00", comments = "JAXB RI v2.2.11")
    protected String gbXrefDbname;
    @XmlElement(name = "GBXref_id", required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-15T10:23:53+02:00", comments = "JAXB RI v2.2.11")
    protected String gbXrefId;

    /**
     * Gets the value of the gbXrefDbname property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-15T10:23:53+02:00", comments = "JAXB RI v2.2.11")
    public String getGBXrefDbname() {
        return gbXrefDbname;
    }

    /**
     * Sets the value of the gbXrefDbname property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-15T10:23:53+02:00", comments = "JAXB RI v2.2.11")
    public void setGBXrefDbname(String value) {
        this.gbXrefDbname = value;
    }

    /**
     * Gets the value of the gbXrefId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-15T10:23:53+02:00", comments = "JAXB RI v2.2.11")
    public String getGBXrefId() {
        return gbXrefId;
    }

    /**
     * Sets the value of the gbXrefId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-15T10:23:53+02:00", comments = "JAXB RI v2.2.11")
    public void setGBXrefId(String value) {
        this.gbXrefId = value;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-15T10:23:53+02:00", comments = "JAXB RI v2.2.11")
    public GBXref withGBXrefDbname(String value) {
        setGBXrefDbname(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-15T10:23:53+02:00", comments = "JAXB RI v2.2.11")
    public GBXref withGBXrefId(String value) {
        setGBXrefId(value);
        return this;
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-15T10:23:53+02:00", comments = "JAXB RI v2.2.11")
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-15T10:23:53+02:00", comments = "JAXB RI v2.2.11")
    public boolean equals(Object that) {
        return EqualsBuilder.reflectionEquals(this, that);
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-15T10:23:53+02:00", comments = "JAXB RI v2.2.11")
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

}
