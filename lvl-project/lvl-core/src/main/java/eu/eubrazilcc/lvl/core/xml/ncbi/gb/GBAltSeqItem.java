//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.07.10 at 02:13:39 PM CEST 
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
@Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-10T02:13:39+02:00", comments = "JAXB RI v2.2.11")
public class GBAltSeqItem {

    @XmlElement(name = "GBAltSeqItem_interval")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-10T02:13:39+02:00", comments = "JAXB RI v2.2.11")
    protected GBAltSeqItemInterval gbAltSeqItemInterval;
    @XmlElement(name = "GBAltSeqItem_isgap")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-10T02:13:39+02:00", comments = "JAXB RI v2.2.11")
    protected GBAltSeqItemIsgap gbAltSeqItemIsgap;
    @XmlElement(name = "GBAltSeqItem_gap-length")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-10T02:13:39+02:00", comments = "JAXB RI v2.2.11")
    protected String gbAltSeqItemGapLength;
    @XmlElement(name = "GBAltSeqItem_gap-type")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-10T02:13:39+02:00", comments = "JAXB RI v2.2.11")
    protected String gbAltSeqItemGapType;
    @XmlElement(name = "GBAltSeqItem_gap-linkage")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-10T02:13:39+02:00", comments = "JAXB RI v2.2.11")
    protected String gbAltSeqItemGapLinkage;
    @XmlElement(name = "GBAltSeqItem_gap-comment")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-10T02:13:39+02:00", comments = "JAXB RI v2.2.11")
    protected String gbAltSeqItemGapComment;
    @XmlElement(name = "GBAltSeqItem_first-accn")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-10T02:13:39+02:00", comments = "JAXB RI v2.2.11")
    protected String gbAltSeqItemFirstAccn;
    @XmlElement(name = "GBAltSeqItem_last-accn")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-10T02:13:39+02:00", comments = "JAXB RI v2.2.11")
    protected String gbAltSeqItemLastAccn;
    @XmlElement(name = "GBAltSeqItem_value")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-10T02:13:39+02:00", comments = "JAXB RI v2.2.11")
    protected String gbAltSeqItemValue;

    /**
     * Gets the value of the gbAltSeqItemInterval property.
     * 
     * @return
     *     possible object is
     *     {@link GBAltSeqItemInterval }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-10T02:13:39+02:00", comments = "JAXB RI v2.2.11")
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
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-10T02:13:39+02:00", comments = "JAXB RI v2.2.11")
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
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-10T02:13:39+02:00", comments = "JAXB RI v2.2.11")
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
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-10T02:13:39+02:00", comments = "JAXB RI v2.2.11")
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
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-10T02:13:39+02:00", comments = "JAXB RI v2.2.11")
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
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-10T02:13:39+02:00", comments = "JAXB RI v2.2.11")
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
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-10T02:13:39+02:00", comments = "JAXB RI v2.2.11")
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
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-10T02:13:39+02:00", comments = "JAXB RI v2.2.11")
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
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-10T02:13:39+02:00", comments = "JAXB RI v2.2.11")
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
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-10T02:13:39+02:00", comments = "JAXB RI v2.2.11")
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
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-10T02:13:39+02:00", comments = "JAXB RI v2.2.11")
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
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-10T02:13:39+02:00", comments = "JAXB RI v2.2.11")
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
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-10T02:13:39+02:00", comments = "JAXB RI v2.2.11")
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
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-10T02:13:39+02:00", comments = "JAXB RI v2.2.11")
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
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-10T02:13:39+02:00", comments = "JAXB RI v2.2.11")
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
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-10T02:13:39+02:00", comments = "JAXB RI v2.2.11")
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
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-10T02:13:39+02:00", comments = "JAXB RI v2.2.11")
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
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-10T02:13:39+02:00", comments = "JAXB RI v2.2.11")
    public void setGBAltSeqItemValue(String value) {
        this.gbAltSeqItemValue = value;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-10T02:13:39+02:00", comments = "JAXB RI v2.2.11")
    public GBAltSeqItem withGBAltSeqItemInterval(GBAltSeqItemInterval value) {
        setGBAltSeqItemInterval(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-10T02:13:39+02:00", comments = "JAXB RI v2.2.11")
    public GBAltSeqItem withGBAltSeqItemIsgap(GBAltSeqItemIsgap value) {
        setGBAltSeqItemIsgap(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-10T02:13:39+02:00", comments = "JAXB RI v2.2.11")
    public GBAltSeqItem withGBAltSeqItemGapLength(String value) {
        setGBAltSeqItemGapLength(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-10T02:13:39+02:00", comments = "JAXB RI v2.2.11")
    public GBAltSeqItem withGBAltSeqItemGapType(String value) {
        setGBAltSeqItemGapType(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-10T02:13:39+02:00", comments = "JAXB RI v2.2.11")
    public GBAltSeqItem withGBAltSeqItemGapLinkage(String value) {
        setGBAltSeqItemGapLinkage(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-10T02:13:39+02:00", comments = "JAXB RI v2.2.11")
    public GBAltSeqItem withGBAltSeqItemGapComment(String value) {
        setGBAltSeqItemGapComment(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-10T02:13:39+02:00", comments = "JAXB RI v2.2.11")
    public GBAltSeqItem withGBAltSeqItemFirstAccn(String value) {
        setGBAltSeqItemFirstAccn(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-10T02:13:39+02:00", comments = "JAXB RI v2.2.11")
    public GBAltSeqItem withGBAltSeqItemLastAccn(String value) {
        setGBAltSeqItemLastAccn(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-10T02:13:39+02:00", comments = "JAXB RI v2.2.11")
    public GBAltSeqItem withGBAltSeqItemValue(String value) {
        setGBAltSeqItemValue(value);
        return this;
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-10T02:13:39+02:00", comments = "JAXB RI v2.2.11")
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-10T02:13:39+02:00", comments = "JAXB RI v2.2.11")
    public boolean equals(Object that) {
        return EqualsBuilder.reflectionEquals(this, that);
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-10T02:13:39+02:00", comments = "JAXB RI v2.2.11")
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

}
