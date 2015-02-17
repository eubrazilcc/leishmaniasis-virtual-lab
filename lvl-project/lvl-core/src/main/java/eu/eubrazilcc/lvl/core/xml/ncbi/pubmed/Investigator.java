//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.02.17 at 10:57:46 PM CET 
//


package eu.eubrazilcc.lvl.core.xml.ncbi.pubmed;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
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
    "lastName",
    "foreName",
    "initials",
    "suffix",
    "nameID",
    "affiliation"
})
@XmlRootElement(name = "Investigator")
@Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T10:57:46+01:00", comments = "JAXB RI v2.2.11")
public class Investigator {

    @XmlAttribute(name = "ValidYN")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T10:57:46+01:00", comments = "JAXB RI v2.2.11")
    protected String validYN;
    @XmlElement(name = "LastName", required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T10:57:46+01:00", comments = "JAXB RI v2.2.11")
    protected LastName lastName;
    @XmlElement(name = "ForeName")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T10:57:46+01:00", comments = "JAXB RI v2.2.11")
    protected ForeName foreName;
    @XmlElement(name = "Initials")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T10:57:46+01:00", comments = "JAXB RI v2.2.11")
    protected Initials initials;
    @XmlElement(name = "Suffix")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T10:57:46+01:00", comments = "JAXB RI v2.2.11")
    protected Suffix suffix;
    @XmlElement(name = "NameID")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T10:57:46+01:00", comments = "JAXB RI v2.2.11")
    protected List<NameID> nameID;
    @XmlElement(name = "Affiliation")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T10:57:46+01:00", comments = "JAXB RI v2.2.11")
    protected String affiliation;

    /**
     * Gets the value of the validYN property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T10:57:46+01:00", comments = "JAXB RI v2.2.11")
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
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T10:57:46+01:00", comments = "JAXB RI v2.2.11")
    public void setValidYN(String value) {
        this.validYN = value;
    }

    /**
     * Gets the value of the lastName property.
     * 
     * @return
     *     possible object is
     *     {@link LastName }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T10:57:46+01:00", comments = "JAXB RI v2.2.11")
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
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T10:57:46+01:00", comments = "JAXB RI v2.2.11")
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
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T10:57:46+01:00", comments = "JAXB RI v2.2.11")
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
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T10:57:46+01:00", comments = "JAXB RI v2.2.11")
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
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T10:57:46+01:00", comments = "JAXB RI v2.2.11")
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
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T10:57:46+01:00", comments = "JAXB RI v2.2.11")
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
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T10:57:46+01:00", comments = "JAXB RI v2.2.11")
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
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T10:57:46+01:00", comments = "JAXB RI v2.2.11")
    public void setSuffix(Suffix value) {
        this.suffix = value;
    }

    /**
     * Gets the value of the nameID property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the nameID property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getNameID().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link NameID }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T10:57:46+01:00", comments = "JAXB RI v2.2.11")
    public List<NameID> getNameID() {
        if (nameID == null) {
            nameID = new ArrayList<NameID>();
        }
        return this.nameID;
    }

    /**
     * Gets the value of the affiliation property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T10:57:46+01:00", comments = "JAXB RI v2.2.11")
    public String getAffiliation() {
        return affiliation;
    }

    /**
     * Sets the value of the affiliation property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T10:57:46+01:00", comments = "JAXB RI v2.2.11")
    public void setAffiliation(String value) {
        this.affiliation = value;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T10:57:46+01:00", comments = "JAXB RI v2.2.11")
    public Investigator withValidYN(String value) {
        setValidYN(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T10:57:46+01:00", comments = "JAXB RI v2.2.11")
    public Investigator withLastName(LastName value) {
        setLastName(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T10:57:46+01:00", comments = "JAXB RI v2.2.11")
    public Investigator withForeName(ForeName value) {
        setForeName(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T10:57:46+01:00", comments = "JAXB RI v2.2.11")
    public Investigator withInitials(Initials value) {
        setInitials(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T10:57:46+01:00", comments = "JAXB RI v2.2.11")
    public Investigator withSuffix(Suffix value) {
        setSuffix(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T10:57:46+01:00", comments = "JAXB RI v2.2.11")
    public Investigator withNameID(NameID... values) {
        if (values!= null) {
            for (NameID value: values) {
                getNameID().add(value);
            }
        }
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T10:57:46+01:00", comments = "JAXB RI v2.2.11")
    public Investigator withNameID(Collection<NameID> values) {
        if (values!= null) {
            getNameID().addAll(values);
        }
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T10:57:46+01:00", comments = "JAXB RI v2.2.11")
    public Investigator withAffiliation(String value) {
        setAffiliation(value);
        return this;
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T10:57:46+01:00", comments = "JAXB RI v2.2.11")
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T10:57:46+01:00", comments = "JAXB RI v2.2.11")
    public boolean equals(java.lang.Object that) {
        return EqualsBuilder.reflectionEquals(this, that);
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T10:57:46+01:00", comments = "JAXB RI v2.2.11")
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

}
