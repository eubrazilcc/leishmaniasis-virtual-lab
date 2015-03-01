//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.03.01 at 03:36:39 PM CET 
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
public class GBReference {

    @XmlElement(name = "GBReference_reference", required = true)
    protected String gbReferenceReference;
    @XmlElement(name = "GBReference_position")
    protected String gbReferencePosition;
    @XmlElement(name = "GBReference_authors")
    protected GBReferenceAuthors gbReferenceAuthors;
    @XmlElement(name = "GBReference_consortium")
    protected String gbReferenceConsortium;
    @XmlElement(name = "GBReference_title")
    protected String gbReferenceTitle;
    @XmlElement(name = "GBReference_journal", required = true)
    protected String gbReferenceJournal;
    @XmlElement(name = "GBReference_xref")
    protected GBReferenceXref gbReferenceXref;
    @XmlElement(name = "GBReference_pubmed")
    protected String gbReferencePubmed;
    @XmlElement(name = "GBReference_remark")
    protected String gbReferenceRemark;

    /**
     * Gets the value of the gbReferenceReference property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
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
    public void setGBReferenceRemark(String value) {
        this.gbReferenceRemark = value;
    }

    public GBReference withGBReferenceReference(String value) {
        setGBReferenceReference(value);
        return this;
    }

    public GBReference withGBReferencePosition(String value) {
        setGBReferencePosition(value);
        return this;
    }

    public GBReference withGBReferenceAuthors(GBReferenceAuthors value) {
        setGBReferenceAuthors(value);
        return this;
    }

    public GBReference withGBReferenceConsortium(String value) {
        setGBReferenceConsortium(value);
        return this;
    }

    public GBReference withGBReferenceTitle(String value) {
        setGBReferenceTitle(value);
        return this;
    }

    public GBReference withGBReferenceJournal(String value) {
        setGBReferenceJournal(value);
        return this;
    }

    public GBReference withGBReferenceXref(GBReferenceXref value) {
        setGBReferenceXref(value);
        return this;
    }

    public GBReference withGBReferencePubmed(String value) {
        setGBReferencePubmed(value);
        return this;
    }

    public GBReference withGBReferenceRemark(String value) {
        setGBReferenceRemark(value);
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
