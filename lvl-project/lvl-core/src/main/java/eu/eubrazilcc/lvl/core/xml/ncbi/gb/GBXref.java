//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.02.25 at 11:43:40 PM CET 
//


package eu.eubrazilcc.lvl.core.xml.ncbi.gb;

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
public class GBXref {

    @XmlElement(name = "GBXref_dbname", required = true)
    protected String gbXrefDbname;
    @XmlElement(name = "GBXref_id", required = true)
    protected String gbXrefId;

    /**
     * Gets the value of the gbXrefDbname property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
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
    public void setGBXrefId(String value) {
        this.gbXrefId = value;
    }

    public GBXref withGBXrefDbname(String value) {
        setGBXrefDbname(value);
        return this;
    }

    public GBXref withGBXrefId(String value) {
        setGBXrefId(value);
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    @Override
    public boolean equals(Object that) {
        return EqualsBuilder.reflectionEquals(this, that);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

}
