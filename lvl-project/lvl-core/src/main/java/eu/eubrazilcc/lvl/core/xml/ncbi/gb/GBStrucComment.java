//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.02.18 at 04:55:33 PM CET 
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
    "gbStrucCommentName",
    "gbStrucCommentItems"
})
@XmlRootElement(name = "GBStrucComment")
@Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-18T04:55:33+01:00", comments = "JAXB RI v2.2.11")
public class GBStrucComment {

    @XmlElement(name = "GBStrucComment_name")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-18T04:55:33+01:00", comments = "JAXB RI v2.2.11")
    protected String gbStrucCommentName;
    @XmlElement(name = "GBStrucComment_items", required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-18T04:55:33+01:00", comments = "JAXB RI v2.2.11")
    protected GBStrucCommentItems gbStrucCommentItems;

    /**
     * Gets the value of the gbStrucCommentName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-18T04:55:33+01:00", comments = "JAXB RI v2.2.11")
    public String getGBStrucCommentName() {
        return gbStrucCommentName;
    }

    /**
     * Sets the value of the gbStrucCommentName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-18T04:55:33+01:00", comments = "JAXB RI v2.2.11")
    public void setGBStrucCommentName(String value) {
        this.gbStrucCommentName = value;
    }

    /**
     * Gets the value of the gbStrucCommentItems property.
     * 
     * @return
     *     possible object is
     *     {@link GBStrucCommentItems }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-18T04:55:33+01:00", comments = "JAXB RI v2.2.11")
    public GBStrucCommentItems getGBStrucCommentItems() {
        return gbStrucCommentItems;
    }

    /**
     * Sets the value of the gbStrucCommentItems property.
     * 
     * @param value
     *     allowed object is
     *     {@link GBStrucCommentItems }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-18T04:55:33+01:00", comments = "JAXB RI v2.2.11")
    public void setGBStrucCommentItems(GBStrucCommentItems value) {
        this.gbStrucCommentItems = value;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-18T04:55:33+01:00", comments = "JAXB RI v2.2.11")
    public GBStrucComment withGBStrucCommentName(String value) {
        setGBStrucCommentName(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-18T04:55:33+01:00", comments = "JAXB RI v2.2.11")
    public GBStrucComment withGBStrucCommentItems(GBStrucCommentItems value) {
        setGBStrucCommentItems(value);
        return this;
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-18T04:55:33+01:00", comments = "JAXB RI v2.2.11")
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-18T04:55:33+01:00", comments = "JAXB RI v2.2.11")
    public boolean equals(Object that) {
        return EqualsBuilder.reflectionEquals(this, that);
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-18T04:55:33+01:00", comments = "JAXB RI v2.2.11")
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

}
