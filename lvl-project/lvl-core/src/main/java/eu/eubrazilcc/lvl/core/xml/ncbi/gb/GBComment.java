//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.03.11 at 05:46:55 PM CET 
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
    "gbCommentType",
    "gbCommentParagraphs"
})
@XmlRootElement(name = "GBComment")
@Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-11T05:46:55+01:00", comments = "JAXB RI v2.2.11")
public class GBComment {

    @XmlElement(name = "GBComment_type")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-11T05:46:55+01:00", comments = "JAXB RI v2.2.11")
    protected String gbCommentType;
    @XmlElement(name = "GBComment_paragraphs", required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-11T05:46:55+01:00", comments = "JAXB RI v2.2.11")
    protected GBCommentParagraphs gbCommentParagraphs;

    /**
     * Gets the value of the gbCommentType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-11T05:46:55+01:00", comments = "JAXB RI v2.2.11")
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
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-11T05:46:55+01:00", comments = "JAXB RI v2.2.11")
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
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-11T05:46:55+01:00", comments = "JAXB RI v2.2.11")
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
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-11T05:46:55+01:00", comments = "JAXB RI v2.2.11")
    public void setGBCommentParagraphs(GBCommentParagraphs value) {
        this.gbCommentParagraphs = value;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-11T05:46:55+01:00", comments = "JAXB RI v2.2.11")
    public GBComment withGBCommentType(String value) {
        setGBCommentType(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-11T05:46:55+01:00", comments = "JAXB RI v2.2.11")
    public GBComment withGBCommentParagraphs(GBCommentParagraphs value) {
        setGBCommentParagraphs(value);
        return this;
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-11T05:46:55+01:00", comments = "JAXB RI v2.2.11")
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-11T05:46:55+01:00", comments = "JAXB RI v2.2.11")
    public boolean equals(Object that) {
        return EqualsBuilder.reflectionEquals(this, that);
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-11T05:46:55+01:00", comments = "JAXB RI v2.2.11")
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

}
