//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.03.09 at 10:16:53 AM CET 
//


package eu.eubrazilcc.lvl.core.xml.ncbi.pubmed;

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
    "registryNumber",
    "nameOfSubstance"
})
@XmlRootElement(name = "Chemical")
@Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-09T10:16:53+01:00", comments = "JAXB RI v2.2.11")
public class Chemical {

    @XmlElement(name = "RegistryNumber", required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-09T10:16:53+01:00", comments = "JAXB RI v2.2.11")
    protected String registryNumber;
    @XmlElement(name = "NameOfSubstance", required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-09T10:16:53+01:00", comments = "JAXB RI v2.2.11")
    protected String nameOfSubstance;

    /**
     * Gets the value of the registryNumber property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-09T10:16:53+01:00", comments = "JAXB RI v2.2.11")
    public String getRegistryNumber() {
        return registryNumber;
    }

    /**
     * Sets the value of the registryNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-09T10:16:53+01:00", comments = "JAXB RI v2.2.11")
    public void setRegistryNumber(String value) {
        this.registryNumber = value;
    }

    /**
     * Gets the value of the nameOfSubstance property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-09T10:16:53+01:00", comments = "JAXB RI v2.2.11")
    public String getNameOfSubstance() {
        return nameOfSubstance;
    }

    /**
     * Sets the value of the nameOfSubstance property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-09T10:16:53+01:00", comments = "JAXB RI v2.2.11")
    public void setNameOfSubstance(String value) {
        this.nameOfSubstance = value;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-09T10:16:53+01:00", comments = "JAXB RI v2.2.11")
    public Chemical withRegistryNumber(String value) {
        setRegistryNumber(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-09T10:16:53+01:00", comments = "JAXB RI v2.2.11")
    public Chemical withNameOfSubstance(String value) {
        setNameOfSubstance(value);
        return this;
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-09T10:16:53+01:00", comments = "JAXB RI v2.2.11")
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-09T10:16:53+01:00", comments = "JAXB RI v2.2.11")
    public boolean equals(java.lang.Object that) {
        return EqualsBuilder.reflectionEquals(this, that);
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-09T10:16:53+01:00", comments = "JAXB RI v2.2.11")
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

}
