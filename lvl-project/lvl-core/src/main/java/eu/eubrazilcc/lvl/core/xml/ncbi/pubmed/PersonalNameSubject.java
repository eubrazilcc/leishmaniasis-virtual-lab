//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.02.24 at 09:06:22 AM CET 
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
    "lastName",
    "foreName",
    "initials",
    "suffix"
})
@XmlRootElement(name = "PersonalNameSubject")
@Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-24T09:06:22+01:00", comments = "JAXB RI v2.2.11")
public class PersonalNameSubject {

    @XmlElement(name = "LastName", required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-24T09:06:22+01:00", comments = "JAXB RI v2.2.11")
    protected LastName lastName;
    @XmlElement(name = "ForeName")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-24T09:06:22+01:00", comments = "JAXB RI v2.2.11")
    protected ForeName foreName;
    @XmlElement(name = "Initials")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-24T09:06:22+01:00", comments = "JAXB RI v2.2.11")
    protected Initials initials;
    @XmlElement(name = "Suffix")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-24T09:06:22+01:00", comments = "JAXB RI v2.2.11")
    protected Suffix suffix;

    /**
     * Gets the value of the lastName property.
     * 
     * @return
     *     possible object is
     *     {@link LastName }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-24T09:06:22+01:00", comments = "JAXB RI v2.2.11")
    public LastName getLastName() {
        return lastName;
    }

    /**
     * Sets the value of the lastName property.
     * 
     * @param value
     *     allowed object is
     *     {@link LastName }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-24T09:06:22+01:00", comments = "JAXB RI v2.2.11")
    public void setLastName(LastName value) {
        this.lastName = value;
    }

    /**
     * Gets the value of the foreName property.
     * 
     * @return
     *     possible object is
     *     {@link ForeName }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-24T09:06:22+01:00", comments = "JAXB RI v2.2.11")
    public ForeName getForeName() {
        return foreName;
    }

    /**
     * Sets the value of the foreName property.
     * 
     * @param value
     *     allowed object is
     *     {@link ForeName }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-24T09:06:22+01:00", comments = "JAXB RI v2.2.11")
    public void setForeName(ForeName value) {
        this.foreName = value;
    }

    /**
     * Gets the value of the initials property.
     * 
     * @return
     *     possible object is
     *     {@link Initials }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-24T09:06:22+01:00", comments = "JAXB RI v2.2.11")
    public Initials getInitials() {
        return initials;
    }

    /**
     * Sets the value of the initials property.
     * 
     * @param value
     *     allowed object is
     *     {@link Initials }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-24T09:06:22+01:00", comments = "JAXB RI v2.2.11")
    public void setInitials(Initials value) {
        this.initials = value;
    }

    /**
     * Gets the value of the suffix property.
     * 
     * @return
     *     possible object is
     *     {@link Suffix }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-24T09:06:22+01:00", comments = "JAXB RI v2.2.11")
    public Suffix getSuffix() {
        return suffix;
    }

    /**
     * Sets the value of the suffix property.
     * 
     * @param value
     *     allowed object is
     *     {@link Suffix }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-24T09:06:22+01:00", comments = "JAXB RI v2.2.11")
    public void setSuffix(Suffix value) {
        this.suffix = value;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-24T09:06:22+01:00", comments = "JAXB RI v2.2.11")
    public PersonalNameSubject withLastName(LastName value) {
        setLastName(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-24T09:06:22+01:00", comments = "JAXB RI v2.2.11")
    public PersonalNameSubject withForeName(ForeName value) {
        setForeName(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-24T09:06:22+01:00", comments = "JAXB RI v2.2.11")
    public PersonalNameSubject withInitials(Initials value) {
        setInitials(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-24T09:06:22+01:00", comments = "JAXB RI v2.2.11")
    public PersonalNameSubject withSuffix(Suffix value) {
        setSuffix(value);
        return this;
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-24T09:06:22+01:00", comments = "JAXB RI v2.2.11")
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-24T09:06:22+01:00", comments = "JAXB RI v2.2.11")
    public boolean equals(java.lang.Object that) {
        return EqualsBuilder.reflectionEquals(this, that);
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-24T09:06:22+01:00", comments = "JAXB RI v2.2.11")
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

}
