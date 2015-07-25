//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.07.23 at 01:04:59 PM CEST 
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
    "grantID",
    "acronym",
    "agency",
    "country"
})
@XmlRootElement(name = "Grant")
@Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:04:58+02:00", comments = "JAXB RI v2.2.11")
public class Grant {

    @XmlElement(name = "GrantID")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:04:58+02:00", comments = "JAXB RI v2.2.11")
    protected String grantID;
    @XmlElement(name = "Acronym")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:04:58+02:00", comments = "JAXB RI v2.2.11")
    protected String acronym;
    @XmlElement(name = "Agency", required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:04:58+02:00", comments = "JAXB RI v2.2.11")
    protected String agency;
    @XmlElement(name = "Country", required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:04:58+02:00", comments = "JAXB RI v2.2.11")
    protected String country;

    /**
     * Gets the value of the grantID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:04:58+02:00", comments = "JAXB RI v2.2.11")
    public String getGrantID() {
        return grantID;
    }

    /**
     * Sets the value of the grantID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:04:58+02:00", comments = "JAXB RI v2.2.11")
    public void setGrantID(String value) {
        this.grantID = value;
    }

    /**
     * Gets the value of the acronym property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:04:58+02:00", comments = "JAXB RI v2.2.11")
    public String getAcronym() {
        return acronym;
    }

    /**
     * Sets the value of the acronym property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:04:58+02:00", comments = "JAXB RI v2.2.11")
    public void setAcronym(String value) {
        this.acronym = value;
    }

    /**
     * Gets the value of the agency property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:04:58+02:00", comments = "JAXB RI v2.2.11")
    public String getAgency() {
        return agency;
    }

    /**
     * Sets the value of the agency property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:04:58+02:00", comments = "JAXB RI v2.2.11")
    public void setAgency(String value) {
        this.agency = value;
    }

    /**
     * Gets the value of the country property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:04:58+02:00", comments = "JAXB RI v2.2.11")
    public String getCountry() {
        return country;
    }

    /**
     * Sets the value of the country property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:04:58+02:00", comments = "JAXB RI v2.2.11")
    public void setCountry(String value) {
        this.country = value;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:04:58+02:00", comments = "JAXB RI v2.2.11")
    public Grant withGrantID(String value) {
        setGrantID(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:04:58+02:00", comments = "JAXB RI v2.2.11")
    public Grant withAcronym(String value) {
        setAcronym(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:04:58+02:00", comments = "JAXB RI v2.2.11")
    public Grant withAgency(String value) {
        setAgency(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:04:58+02:00", comments = "JAXB RI v2.2.11")
    public Grant withCountry(String value) {
        setCountry(value);
        return this;
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:04:58+02:00", comments = "JAXB RI v2.2.11")
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:04:58+02:00", comments = "JAXB RI v2.2.11")
    public boolean equals(java.lang.Object that) {
        return EqualsBuilder.reflectionEquals(this, that);
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:04:58+02:00", comments = "JAXB RI v2.2.11")
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

}
