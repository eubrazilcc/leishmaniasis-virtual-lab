//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.03.09 at 10:16:53 AM CET 
//


package eu.eubrazilcc.lvl.core.xml.ncbi.pubmed;

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
    "abstractText",
    "copyrightInformation"
})
@XmlRootElement(name = "Abstract")
@Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-09T10:16:53+01:00", comments = "JAXB RI v2.2.11")
public class Abstract {

    @XmlElement(name = "AbstractText", required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-09T10:16:53+01:00", comments = "JAXB RI v2.2.11")
    protected String abstractText;
    @XmlElement(name = "CopyrightInformation")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-09T10:16:53+01:00", comments = "JAXB RI v2.2.11")
    protected String copyrightInformation;

    /**
     * Gets the value of the abstractText property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-09T10:16:53+01:00", comments = "JAXB RI v2.2.11")
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
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-09T10:16:53+01:00", comments = "JAXB RI v2.2.11")
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
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-09T10:16:53+01:00", comments = "JAXB RI v2.2.11")
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
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-09T10:16:53+01:00", comments = "JAXB RI v2.2.11")
    public void setCopyrightInformation(String value) {
        this.copyrightInformation = value;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-09T10:16:53+01:00", comments = "JAXB RI v2.2.11")
    public Abstract withAbstractText(String value) {
        setAbstractText(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-09T10:16:53+01:00", comments = "JAXB RI v2.2.11")
    public Abstract withCopyrightInformation(String value) {
        setCopyrightInformation(value);
        return this;
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-09T10:16:53+01:00", comments = "JAXB RI v2.2.11")
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-09T10:16:53+01:00", comments = "JAXB RI v2.2.11")
    public boolean equals(java.lang.Object that) {
        return EqualsBuilder.reflectionEquals(this, that);
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-09T10:16:53+01:00", comments = "JAXB RI v2.2.11")
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

}
