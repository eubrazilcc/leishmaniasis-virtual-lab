//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.07.15 at 10:23:54 AM CEST 
//


package eu.eubrazilcc.lvl.core.xml.ncbi.taxonomy;

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
    "gcId",
    "gcName"
})
@XmlRootElement(name = "GeneticCode")
@Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-15T10:23:54+02:00", comments = "JAXB RI v2.2.11")
public class GeneticCode {

    @XmlElement(name = "GCId", required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-15T10:23:54+02:00", comments = "JAXB RI v2.2.11")
    protected String gcId;
    @XmlElement(name = "GCName", required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-15T10:23:54+02:00", comments = "JAXB RI v2.2.11")
    protected String gcName;

    /**
     * Gets the value of the gcId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-15T10:23:54+02:00", comments = "JAXB RI v2.2.11")
    public String getGCId() {
        return gcId;
    }

    /**
     * Sets the value of the gcId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-15T10:23:54+02:00", comments = "JAXB RI v2.2.11")
    public void setGCId(String value) {
        this.gcId = value;
    }

    /**
     * Gets the value of the gcName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-15T10:23:54+02:00", comments = "JAXB RI v2.2.11")
    public String getGCName() {
        return gcName;
    }

    /**
     * Sets the value of the gcName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-15T10:23:54+02:00", comments = "JAXB RI v2.2.11")
    public void setGCName(String value) {
        this.gcName = value;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-15T10:23:54+02:00", comments = "JAXB RI v2.2.11")
    public GeneticCode withGCId(String value) {
        setGCId(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-15T10:23:54+02:00", comments = "JAXB RI v2.2.11")
    public GeneticCode withGCName(String value) {
        setGCName(value);
        return this;
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-15T10:23:54+02:00", comments = "JAXB RI v2.2.11")
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-15T10:23:54+02:00", comments = "JAXB RI v2.2.11")
    public boolean equals(Object that) {
        return EqualsBuilder.reflectionEquals(this, that);
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-15T10:23:54+02:00", comments = "JAXB RI v2.2.11")
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

}
