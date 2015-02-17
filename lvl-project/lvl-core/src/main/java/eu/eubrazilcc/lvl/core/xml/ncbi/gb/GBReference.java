//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.02.17 at 06:39:18 PM CET 
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
    "gbReferenceReference",
    "gbReferencePosition",
    "gbReferenceAuthors",
    "gbReferenceConsortium",
    "gbReferenceTitle",
    "gbReferenceJournal",
    "gbReferenceXref",
    "gbReferencePubmed",
    "gbReferenceRemark"
})
@XmlRootElement(name = "GBReference")
@Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T06:39:18+01:00", comments = "JAXB RI v2.2.11")
public class GBReference {

    @XmlElement(name = "GBReference_reference", required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T06:39:18+01:00", comments = "JAXB RI v2.2.11")
    protected String gbReferenceReference;
    @XmlElement(name = "GBReference_position")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T06:39:18+01:00", comments = "JAXB RI v2.2.11")
    protected String gbReferencePosition;
    @XmlElement(name = "GBReference_authors")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T06:39:18+01:00", comments = "JAXB RI v2.2.11")
    protected GBReferenceAuthors gbReferenceAuthors;
    @XmlElement(name = "GBReference_consortium")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T06:39:18+01:00", comments = "JAXB RI v2.2.11")
    protected String gbReferenceConsortium;
    @XmlElement(name = "GBReference_title")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T06:39:18+01:00", comments = "JAXB RI v2.2.11")
    protected String gbReferenceTitle;
    @XmlElement(name = "GBReference_journal", required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T06:39:18+01:00", comments = "JAXB RI v2.2.11")
    protected String gbReferenceJournal;
    @XmlElement(name = "GBReference_xref")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T06:39:18+01:00", comments = "JAXB RI v2.2.11")
    protected GBReferenceXref gbReferenceXref;
    @XmlElement(name = "GBReference_pubmed")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T06:39:18+01:00", comments = "JAXB RI v2.2.11")
    protected String gbReferencePubmed;
    @XmlElement(name = "GBReference_remark")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T06:39:18+01:00", comments = "JAXB RI v2.2.11")
    protected String gbReferenceRemark;

    /**
     * Gets the value of the gbReferenceReference property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T06:39:18+01:00", comments = "JAXB RI v2.2.11")
    public String getGBReferenceReference() {
        return gbReferenceReference;
    }

    /**
     * Sets the value of the gbReferenceReference property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T06:39:18+01:00", comments = "JAXB RI v2.2.11")
    public void setGBReferenceReference(String value) {
        this.gbReferenceReference = value;
    }

    /**
     * Gets the value of the gbReferencePosition property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T06:39:18+01:00", comments = "JAXB RI v2.2.11")
    public String getGBReferencePosition() {
        return gbReferencePosition;
    }

    /**
     * Sets the value of the gbReferencePosition property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T06:39:18+01:00", comments = "JAXB RI v2.2.11")
    public void setGBReferencePosition(String value) {
        this.gbReferencePosition = value;
    }

    /**
     * Gets the value of the gbReferenceAuthors property.
     * 
     * @return
     *     possible object is
     *     {@link GBReferenceAuthors }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T06:39:18+01:00", comments = "JAXB RI v2.2.11")
    public GBReferenceAuthors getGBReferenceAuthors() {
        return gbReferenceAuthors;
    }

    /**
     * Sets the value of the gbReferenceAuthors property.
     * 
     * @param value
     *     allowed object is
     *     {@link GBReferenceAuthors }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T06:39:18+01:00", comments = "JAXB RI v2.2.11")
    public void setGBReferenceAuthors(GBReferenceAuthors value) {
        this.gbReferenceAuthors = value;
    }

    /**
     * Gets the value of the gbReferenceConsortium property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T06:39:18+01:00", comments = "JAXB RI v2.2.11")
    public String getGBReferenceConsortium() {
        return gbReferenceConsortium;
    }

    /**
     * Sets the value of the gbReferenceConsortium property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T06:39:18+01:00", comments = "JAXB RI v2.2.11")
    public void setGBReferenceConsortium(String value) {
        this.gbReferenceConsortium = value;
    }

    /**
     * Gets the value of the gbReferenceTitle property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T06:39:18+01:00", comments = "JAXB RI v2.2.11")
    public String getGBReferenceTitle() {
        return gbReferenceTitle;
    }

    /**
     * Sets the value of the gbReferenceTitle property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T06:39:18+01:00", comments = "JAXB RI v2.2.11")
    public void setGBReferenceTitle(String value) {
        this.gbReferenceTitle = value;
    }

    /**
     * Gets the value of the gbReferenceJournal property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T06:39:18+01:00", comments = "JAXB RI v2.2.11")
    public String getGBReferenceJournal() {
        return gbReferenceJournal;
    }

    /**
     * Sets the value of the gbReferenceJournal property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T06:39:18+01:00", comments = "JAXB RI v2.2.11")
    public void setGBReferenceJournal(String value) {
        this.gbReferenceJournal = value;
    }

    /**
     * Gets the value of the gbReferenceXref property.
     * 
     * @return
     *     possible object is
     *     {@link GBReferenceXref }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T06:39:18+01:00", comments = "JAXB RI v2.2.11")
    public GBReferenceXref getGBReferenceXref() {
        return gbReferenceXref;
    }

    /**
     * Sets the value of the gbReferenceXref property.
     * 
     * @param value
     *     allowed object is
     *     {@link GBReferenceXref }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T06:39:18+01:00", comments = "JAXB RI v2.2.11")
    public void setGBReferenceXref(GBReferenceXref value) {
        this.gbReferenceXref = value;
    }

    /**
     * Gets the value of the gbReferencePubmed property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T06:39:18+01:00", comments = "JAXB RI v2.2.11")
    public String getGBReferencePubmed() {
        return gbReferencePubmed;
    }

    /**
     * Sets the value of the gbReferencePubmed property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T06:39:18+01:00", comments = "JAXB RI v2.2.11")
    public void setGBReferencePubmed(String value) {
        this.gbReferencePubmed = value;
    }

    /**
     * Gets the value of the gbReferenceRemark property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T06:39:18+01:00", comments = "JAXB RI v2.2.11")
    public String getGBReferenceRemark() {
        return gbReferenceRemark;
    }

    /**
     * Sets the value of the gbReferenceRemark property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T06:39:18+01:00", comments = "JAXB RI v2.2.11")
    public void setGBReferenceRemark(String value) {
        this.gbReferenceRemark = value;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T06:39:18+01:00", comments = "JAXB RI v2.2.11")
    public GBReference withGBReferenceReference(String value) {
        setGBReferenceReference(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T06:39:18+01:00", comments = "JAXB RI v2.2.11")
    public GBReference withGBReferencePosition(String value) {
        setGBReferencePosition(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T06:39:18+01:00", comments = "JAXB RI v2.2.11")
    public GBReference withGBReferenceAuthors(GBReferenceAuthors value) {
        setGBReferenceAuthors(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T06:39:18+01:00", comments = "JAXB RI v2.2.11")
    public GBReference withGBReferenceConsortium(String value) {
        setGBReferenceConsortium(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T06:39:18+01:00", comments = "JAXB RI v2.2.11")
    public GBReference withGBReferenceTitle(String value) {
        setGBReferenceTitle(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T06:39:18+01:00", comments = "JAXB RI v2.2.11")
    public GBReference withGBReferenceJournal(String value) {
        setGBReferenceJournal(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T06:39:18+01:00", comments = "JAXB RI v2.2.11")
    public GBReference withGBReferenceXref(GBReferenceXref value) {
        setGBReferenceXref(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T06:39:18+01:00", comments = "JAXB RI v2.2.11")
    public GBReference withGBReferencePubmed(String value) {
        setGBReferencePubmed(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T06:39:18+01:00", comments = "JAXB RI v2.2.11")
    public GBReference withGBReferenceRemark(String value) {
        setGBReferenceRemark(value);
        return this;
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T06:39:18+01:00", comments = "JAXB RI v2.2.11")
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T06:39:18+01:00", comments = "JAXB RI v2.2.11")
    public boolean equals(Object that) {
        return EqualsBuilder.reflectionEquals(this, that);
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T06:39:18+01:00", comments = "JAXB RI v2.2.11")
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

}
