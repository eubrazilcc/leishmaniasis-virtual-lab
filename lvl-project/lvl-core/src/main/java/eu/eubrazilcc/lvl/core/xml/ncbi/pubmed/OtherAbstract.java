//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.02.17 at 06:39:18 PM CET 
//


package eu.eubrazilcc.lvl.core.xml.ncbi.pubmed;

import javax.annotation.Generated;
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
    "abstractText",
    "copyrightInformation"
})
@XmlRootElement(name = "OtherAbstract")
@Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T06:39:18+01:00", comments = "JAXB RI v2.2.11")
public class OtherAbstract {

    @XmlAttribute(name = "Type", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T06:39:18+01:00", comments = "JAXB RI v2.2.11")
    protected String type;
    @XmlElement(name = "AbstractText", required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T06:39:18+01:00", comments = "JAXB RI v2.2.11")
    protected String abstractText;
    @XmlElement(name = "CopyrightInformation")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T06:39:18+01:00", comments = "JAXB RI v2.2.11")
    protected String copyrightInformation;

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T06:39:18+01:00", comments = "JAXB RI v2.2.11")
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
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T06:39:18+01:00", comments = "JAXB RI v2.2.11")
    public void setType(String value) {
        this.type = value;
    }

    /**
     * Gets the value of the abstractText property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T06:39:18+01:00", comments = "JAXB RI v2.2.11")
    public String getAbstractText() {
        return abstractText;
    }

    /**
     * Sets the value of the abstractText property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T06:39:18+01:00", comments = "JAXB RI v2.2.11")
    public void setAbstractText(String value) {
        this.abstractText = value;
    }

    /**
     * Gets the value of the copyrightInformation property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T06:39:18+01:00", comments = "JAXB RI v2.2.11")
    public String getCopyrightInformation() {
        return copyrightInformation;
    }

    /**
     * Sets the value of the copyrightInformation property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T06:39:18+01:00", comments = "JAXB RI v2.2.11")
    public void setCopyrightInformation(String value) {
        this.copyrightInformation = value;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T06:39:18+01:00", comments = "JAXB RI v2.2.11")
    public OtherAbstract withType(String value) {
        setType(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T06:39:18+01:00", comments = "JAXB RI v2.2.11")
    public OtherAbstract withAbstractText(String value) {
        setAbstractText(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T06:39:18+01:00", comments = "JAXB RI v2.2.11")
    public OtherAbstract withCopyrightInformation(String value) {
        setCopyrightInformation(value);
        return this;
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T06:39:18+01:00", comments = "JAXB RI v2.2.11")
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T06:39:18+01:00", comments = "JAXB RI v2.2.11")
    public boolean equals(java.lang.Object that) {
        return EqualsBuilder.reflectionEquals(this, that);
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T06:39:18+01:00", comments = "JAXB RI v2.2.11")
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

}
