//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.03.01 at 03:36:39 PM CET 
//


package eu.eubrazilcc.lvl.core.xml.ncbi.pubmed;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
public class Investigator {

    @XmlAttribute(name = "ValidYN")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String validYN;
    @XmlElement(name = "LastName", required = true)
    protected LastName lastName;
    @XmlElement(name = "ForeName")
    protected ForeName foreName;
    @XmlElement(name = "Initials")
    protected Initials initials;
    @XmlElement(name = "Suffix")
    protected Suffix suffix;
    @XmlElement(name = "NameID")
    protected List<NameID> nameID;
    @XmlElement(name = "Affiliation")
    protected String affiliation;

    /**
     * Gets the value of the validYN property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
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
    public void setAffiliation(String value) {
        this.affiliation = value;
    }

    public Investigator withValidYN(String value) {
        setValidYN(value);
        return this;
    }

    public Investigator withLastName(LastName value) {
        setLastName(value);
        return this;
    }

    public Investigator withForeName(ForeName value) {
        setForeName(value);
        return this;
    }

    public Investigator withInitials(Initials value) {
        setInitials(value);
        return this;
    }

    public Investigator withSuffix(Suffix value) {
        setSuffix(value);
        return this;
    }

    public Investigator withNameID(NameID... values) {
        if (values!= null) {
            for (NameID value: values) {
                getNameID().add(value);
            }
        }
        return this;
    }

    public Investigator withNameID(Collection<NameID> values) {
        if (values!= null) {
            getNameID().addAll(values);
        }
        return this;
    }

    public Investigator withAffiliation(String value) {
        setAffiliation(value);
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    @Override
    public boolean equals(java.lang.Object that) {
        return EqualsBuilder.reflectionEquals(this, that);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

}
