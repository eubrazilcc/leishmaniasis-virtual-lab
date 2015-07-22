//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.07.22 at 12:30:12 PM CEST 
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
    "country",
    "medlineTA",
    "nlmUniqueID",
    "issnLinking"
})
@XmlRootElement(name = "MedlineJournalInfo")
@Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-22T12:30:12+02:00", comments = "JAXB RI v2.2.11")
public class MedlineJournalInfo {

    @XmlElement(name = "Country")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-22T12:30:12+02:00", comments = "JAXB RI v2.2.11")
    protected String country;
    @XmlElement(name = "MedlineTA", required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-22T12:30:12+02:00", comments = "JAXB RI v2.2.11")
    protected String medlineTA;
    @XmlElement(name = "NlmUniqueID")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-22T12:30:12+02:00", comments = "JAXB RI v2.2.11")
    protected String nlmUniqueID;
    @XmlElement(name = "ISSNLinking")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-22T12:30:12+02:00", comments = "JAXB RI v2.2.11")
    protected String issnLinking;

    /**
     * Gets the value of the country property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-22T12:30:12+02:00", comments = "JAXB RI v2.2.11")
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
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-22T12:30:12+02:00", comments = "JAXB RI v2.2.11")
    public void setCountry(String value) {
        this.country = value;
    }

    /**
     * Gets the value of the medlineTA property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-22T12:30:12+02:00", comments = "JAXB RI v2.2.11")
    public String getMedlineTA() {
        return medlineTA;
    }

    /**
     * Sets the value of the medlineTA property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-22T12:30:12+02:00", comments = "JAXB RI v2.2.11")
    public void setMedlineTA(String value) {
        this.medlineTA = value;
    }

    /**
     * Gets the value of the nlmUniqueID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-22T12:30:12+02:00", comments = "JAXB RI v2.2.11")
    public String getNlmUniqueID() {
        return nlmUniqueID;
    }

    /**
     * Sets the value of the nlmUniqueID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-22T12:30:12+02:00", comments = "JAXB RI v2.2.11")
    public void setNlmUniqueID(String value) {
        this.nlmUniqueID = value;
    }

    /**
     * Gets the value of the issnLinking property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-22T12:30:12+02:00", comments = "JAXB RI v2.2.11")
    public String getISSNLinking() {
        return issnLinking;
    }

    /**
     * Sets the value of the issnLinking property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-22T12:30:12+02:00", comments = "JAXB RI v2.2.11")
    public void setISSNLinking(String value) {
        this.issnLinking = value;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-22T12:30:12+02:00", comments = "JAXB RI v2.2.11")
    public MedlineJournalInfo withCountry(String value) {
        setCountry(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-22T12:30:12+02:00", comments = "JAXB RI v2.2.11")
    public MedlineJournalInfo withMedlineTA(String value) {
        setMedlineTA(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-22T12:30:12+02:00", comments = "JAXB RI v2.2.11")
    public MedlineJournalInfo withNlmUniqueID(String value) {
        setNlmUniqueID(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-22T12:30:12+02:00", comments = "JAXB RI v2.2.11")
    public MedlineJournalInfo withISSNLinking(String value) {
        setISSNLinking(value);
        return this;
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-22T12:30:12+02:00", comments = "JAXB RI v2.2.11")
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-22T12:30:12+02:00", comments = "JAXB RI v2.2.11")
    public boolean equals(java.lang.Object that) {
        return EqualsBuilder.reflectionEquals(this, that);
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-22T12:30:12+02:00", comments = "JAXB RI v2.2.11")
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

}
