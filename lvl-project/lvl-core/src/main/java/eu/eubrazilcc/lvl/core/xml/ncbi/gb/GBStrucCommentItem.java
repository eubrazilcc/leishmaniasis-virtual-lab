//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.02.27 at 11:27:22 AM CET 
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
    "gbStrucCommentItemTag",
    "gbStrucCommentItemValue",
    "gbStrucCommentItemUrl"
})
@XmlRootElement(name = "GBStrucCommentItem")
public class GBStrucCommentItem {

    @XmlElement(name = "GBStrucCommentItem_tag")
    protected String gbStrucCommentItemTag;
    @XmlElement(name = "GBStrucCommentItem_value")
    protected String gbStrucCommentItemValue;
    @XmlElement(name = "GBStrucCommentItem_url")
    protected String gbStrucCommentItemUrl;

    /**
     * Gets the value of the gbStrucCommentItemTag property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGBStrucCommentItemTag() {
        return gbStrucCommentItemTag;
    }

    /**
     * Sets the value of the gbStrucCommentItemTag property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGBStrucCommentItemTag(String value) {
        this.gbStrucCommentItemTag = value;
    }

    /**
     * Gets the value of the gbStrucCommentItemValue property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGBStrucCommentItemValue() {
        return gbStrucCommentItemValue;
    }

    /**
     * Sets the value of the gbStrucCommentItemValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGBStrucCommentItemValue(String value) {
        this.gbStrucCommentItemValue = value;
    }

    /**
     * Gets the value of the gbStrucCommentItemUrl property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGBStrucCommentItemUrl() {
        return gbStrucCommentItemUrl;
    }

    /**
     * Sets the value of the gbStrucCommentItemUrl property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGBStrucCommentItemUrl(String value) {
        this.gbStrucCommentItemUrl = value;
    }

    public GBStrucCommentItem withGBStrucCommentItemTag(String value) {
        setGBStrucCommentItemTag(value);
        return this;
    }

    public GBStrucCommentItem withGBStrucCommentItemValue(String value) {
        setGBStrucCommentItemValue(value);
        return this;
    }

    public GBStrucCommentItem withGBStrucCommentItemUrl(String value) {
        setGBStrucCommentItemUrl(value);
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
