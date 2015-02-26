//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.02.26 at 11:41:32 PM CET 
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
    "gbAltSeqItemInterval",
    "gbAltSeqItemIsgap",
    "gbAltSeqItemGapLength",
    "gbAltSeqItemGapType",
    "gbAltSeqItemGapLinkage",
    "gbAltSeqItemGapComment",
    "gbAltSeqItemFirstAccn",
    "gbAltSeqItemLastAccn",
    "gbAltSeqItemValue"
})
@XmlRootElement(name = "GBAltSeqItem")
public class GBAltSeqItem {

    @XmlElement(name = "GBAltSeqItem_interval")
    protected GBAltSeqItemInterval gbAltSeqItemInterval;
    @XmlElement(name = "GBAltSeqItem_isgap")
    protected GBAltSeqItemIsgap gbAltSeqItemIsgap;
    @XmlElement(name = "GBAltSeqItem_gap-length")
    protected String gbAltSeqItemGapLength;
    @XmlElement(name = "GBAltSeqItem_gap-type")
    protected String gbAltSeqItemGapType;
    @XmlElement(name = "GBAltSeqItem_gap-linkage")
    protected String gbAltSeqItemGapLinkage;
    @XmlElement(name = "GBAltSeqItem_gap-comment")
    protected String gbAltSeqItemGapComment;
    @XmlElement(name = "GBAltSeqItem_first-accn")
    protected String gbAltSeqItemFirstAccn;
    @XmlElement(name = "GBAltSeqItem_last-accn")
    protected String gbAltSeqItemLastAccn;
    @XmlElement(name = "GBAltSeqItem_value")
    protected String gbAltSeqItemValue;

    /**
     * Gets the value of the gbAltSeqItemInterval property.
     * 
     * @return
     *     possible object is
     *     {@link GBAltSeqItemInterval }
     *     
     */
    public GBAltSeqItemInterval getGBAltSeqItemInterval() {
        return gbAltSeqItemInterval;
    }

    /**
     * Sets the value of the gbAltSeqItemInterval property.
     * 
     * @param value
     *     allowed object is
     *     {@link GBAltSeqItemInterval }
     *     
     */
    public void setGBAltSeqItemInterval(GBAltSeqItemInterval value) {
        this.gbAltSeqItemInterval = value;
    }

    /**
     * Gets the value of the gbAltSeqItemIsgap property.
     * 
     * @return
     *     possible object is
     *     {@link GBAltSeqItemIsgap }
     *     
     */
    public GBAltSeqItemIsgap getGBAltSeqItemIsgap() {
        return gbAltSeqItemIsgap;
    }

    /**
     * Sets the value of the gbAltSeqItemIsgap property.
     * 
     * @param value
     *     allowed object is
     *     {@link GBAltSeqItemIsgap }
     *     
     */
    public void setGBAltSeqItemIsgap(GBAltSeqItemIsgap value) {
        this.gbAltSeqItemIsgap = value;
    }

    /**
     * Gets the value of the gbAltSeqItemGapLength property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGBAltSeqItemGapLength() {
        return gbAltSeqItemGapLength;
    }

    /**
     * Sets the value of the gbAltSeqItemGapLength property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGBAltSeqItemGapLength(String value) {
        this.gbAltSeqItemGapLength = value;
    }

    /**
     * Gets the value of the gbAltSeqItemGapType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGBAltSeqItemGapType() {
        return gbAltSeqItemGapType;
    }

    /**
     * Sets the value of the gbAltSeqItemGapType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGBAltSeqItemGapType(String value) {
        this.gbAltSeqItemGapType = value;
    }

    /**
     * Gets the value of the gbAltSeqItemGapLinkage property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGBAltSeqItemGapLinkage() {
        return gbAltSeqItemGapLinkage;
    }

    /**
     * Sets the value of the gbAltSeqItemGapLinkage property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGBAltSeqItemGapLinkage(String value) {
        this.gbAltSeqItemGapLinkage = value;
    }

    /**
     * Gets the value of the gbAltSeqItemGapComment property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGBAltSeqItemGapComment() {
        return gbAltSeqItemGapComment;
    }

    /**
     * Sets the value of the gbAltSeqItemGapComment property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGBAltSeqItemGapComment(String value) {
        this.gbAltSeqItemGapComment = value;
    }

    /**
     * Gets the value of the gbAltSeqItemFirstAccn property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGBAltSeqItemFirstAccn() {
        return gbAltSeqItemFirstAccn;
    }

    /**
     * Sets the value of the gbAltSeqItemFirstAccn property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGBAltSeqItemFirstAccn(String value) {
        this.gbAltSeqItemFirstAccn = value;
    }

    /**
     * Gets the value of the gbAltSeqItemLastAccn property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGBAltSeqItemLastAccn() {
        return gbAltSeqItemLastAccn;
    }

    /**
     * Sets the value of the gbAltSeqItemLastAccn property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGBAltSeqItemLastAccn(String value) {
        this.gbAltSeqItemLastAccn = value;
    }

    /**
     * Gets the value of the gbAltSeqItemValue property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGBAltSeqItemValue() {
        return gbAltSeqItemValue;
    }

    /**
     * Sets the value of the gbAltSeqItemValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGBAltSeqItemValue(String value) {
        this.gbAltSeqItemValue = value;
    }

    public GBAltSeqItem withGBAltSeqItemInterval(GBAltSeqItemInterval value) {
        setGBAltSeqItemInterval(value);
        return this;
    }

    public GBAltSeqItem withGBAltSeqItemIsgap(GBAltSeqItemIsgap value) {
        setGBAltSeqItemIsgap(value);
        return this;
    }

    public GBAltSeqItem withGBAltSeqItemGapLength(String value) {
        setGBAltSeqItemGapLength(value);
        return this;
    }

    public GBAltSeqItem withGBAltSeqItemGapType(String value) {
        setGBAltSeqItemGapType(value);
        return this;
    }

    public GBAltSeqItem withGBAltSeqItemGapLinkage(String value) {
        setGBAltSeqItemGapLinkage(value);
        return this;
    }

    public GBAltSeqItem withGBAltSeqItemGapComment(String value) {
        setGBAltSeqItemGapComment(value);
        return this;
    }

    public GBAltSeqItem withGBAltSeqItemFirstAccn(String value) {
        setGBAltSeqItemFirstAccn(value);
        return this;
    }

    public GBAltSeqItem withGBAltSeqItemLastAccn(String value) {
        setGBAltSeqItemLastAccn(value);
        return this;
    }

    public GBAltSeqItem withGBAltSeqItemValue(String value) {
        setGBAltSeqItemValue(value);
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
