//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.03.11 at 05:46:55 PM CET 
//


package eu.eubrazilcc.lvl.core.xml.ncbi.pubmed;

import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
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
    "value"
})
@XmlRootElement(name = "URL")
@Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-11T05:46:55+01:00", comments = "JAXB RI v2.2.11")
public class URL {

    @XmlAttribute(name = "lang")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-11T05:46:55+01:00", comments = "JAXB RI v2.2.11")
    protected String lang;
    @XmlAttribute(name = "Type")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-11T05:46:55+01:00", comments = "JAXB RI v2.2.11")
    protected String type;
    @XmlValue
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-11T05:46:55+01:00", comments = "JAXB RI v2.2.11")
    protected String value;

    /**
     * Gets the value of the lang property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-11T05:46:55+01:00", comments = "JAXB RI v2.2.11")
    public String getLang() {
        return lang;
    }

    /**
     * Sets the value of the lang property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-11T05:46:55+01:00", comments = "JAXB RI v2.2.11")
    public void setLang(String value) {
        this.lang = value;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-11T05:46:55+01:00", comments = "JAXB RI v2.2.11")
    public String getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-11T05:46:55+01:00", comments = "JAXB RI v2.2.11")
    public void setType(String value) {
        this.type = value;
    }

    /**
     * Gets the value of the value property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-11T05:46:55+01:00", comments = "JAXB RI v2.2.11")
    public String getvalue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-11T05:46:55+01:00", comments = "JAXB RI v2.2.11")
    public void setvalue(String value) {
        this.value = value;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-11T05:46:55+01:00", comments = "JAXB RI v2.2.11")
    public URL withLang(String value) {
        setLang(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-11T05:46:55+01:00", comments = "JAXB RI v2.2.11")
    public URL withType(String value) {
        setType(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-11T05:46:55+01:00", comments = "JAXB RI v2.2.11")
    public URL withvalue(String value) {
        setvalue(value);
        return this;
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-11T05:46:55+01:00", comments = "JAXB RI v2.2.11")
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-11T05:46:55+01:00", comments = "JAXB RI v2.2.11")
    public boolean equals(java.lang.Object that) {
        return EqualsBuilder.reflectionEquals(this, that);
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-11T05:46:55+01:00", comments = "JAXB RI v2.2.11")
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

}
