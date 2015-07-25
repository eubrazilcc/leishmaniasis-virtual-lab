//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.07.23 at 01:05:16 PM CEST 
//


package eu.eubrazilcc.lvl.core.xml.tdwg.tapir;

import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;


/**
 * A URI that points to a document or schema referenced by TAPIR operations
 * 
 * <p>Java class for externalResourceType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="externalResourceType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="location" use="required" type="{http://www.w3.org/2001/XMLSchema}anyURI" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "externalResourceType")
@XmlSeeAlso({
    eu.eubrazilcc.lvl.core.xml.tdwg.tapir.CapabilitiesResultType.Operations.Search.OutputModels.KnownOutputModels.OutputModel.class,
    eu.eubrazilcc.lvl.core.xml.tdwg.tapir.TemplatesCapabilitiesType.Template.class
})
@Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:16+02:00", comments = "JAXB RI v2.2.11")
public class ExternalResourceType {

    @XmlAttribute(name = "location", required = true)
    @XmlSchemaType(name = "anyURI")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:16+02:00", comments = "JAXB RI v2.2.11")
    protected String location;

    /**
     * Gets the value of the location property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:16+02:00", comments = "JAXB RI v2.2.11")
    public String getLocation() {
        return location;
    }

    /**
     * Sets the value of the location property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:16+02:00", comments = "JAXB RI v2.2.11")
    public void setLocation(String value) {
        this.location = value;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:16+02:00", comments = "JAXB RI v2.2.11")
    public ExternalResourceType withLocation(String value) {
        setLocation(value);
        return this;
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:16+02:00", comments = "JAXB RI v2.2.11")
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:16+02:00", comments = "JAXB RI v2.2.11")
    public boolean equals(Object that) {
        return EqualsBuilder.reflectionEquals(this, that);
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:16+02:00", comments = "JAXB RI v2.2.11")
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

}
