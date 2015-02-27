//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.02.27 at 09:52:53 AM CET 
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
    "gbCommentType",
    "gbCommentParagraphs"
})
@XmlRootElement(name = "GBComment")
public class GBComment {

    @XmlElement(name = "GBComment_type")
    protected String gbCommentType;
    @XmlElement(name = "GBComment_paragraphs", required = true)
    protected GBCommentParagraphs gbCommentParagraphs;

    /**
     * Gets the value of the gbCommentType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGBCommentType() {
        return gbCommentType;
    }

    /**
     * Sets the value of the gbCommentType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGBCommentType(String value) {
        this.gbCommentType = value;
    }

    /**
     * Gets the value of the gbCommentParagraphs property.
     * 
     * @return
     *     possible object is
     *     {@link GBCommentParagraphs }
     *     
     */
    public GBCommentParagraphs getGBCommentParagraphs() {
        return gbCommentParagraphs;
    }

    /**
     * Sets the value of the gbCommentParagraphs property.
     * 
     * @param value
     *     allowed object is
     *     {@link GBCommentParagraphs }
     *     
     */
    public void setGBCommentParagraphs(GBCommentParagraphs value) {
        this.gbCommentParagraphs = value;
    }

    public GBComment withGBCommentType(String value) {
        setGBCommentType(value);
        return this;
    }

    public GBComment withGBCommentParagraphs(GBCommentParagraphs value) {
        setGBCommentParagraphs(value);
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
