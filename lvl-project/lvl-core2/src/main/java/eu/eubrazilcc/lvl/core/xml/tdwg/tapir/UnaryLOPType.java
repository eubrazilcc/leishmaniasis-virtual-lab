//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.07.23 at 01:05:16 PM CEST 
//


package eu.eubrazilcc.lvl.core.xml.tdwg.tapir;

import javax.annotation.Generated;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;


/**
 * A complex type for unary logical operators.
 * 
 * <p>Java class for unaryLOPType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="unaryLOPType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://rs.tdwg.org/tapir/1.0}booleanOP"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "unaryLOPType", propOrder = {
    "booleanOP"
})
@XmlSeeAlso({
    Not.class
})
@Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:16+02:00", comments = "JAXB RI v2.2.11")
public class UnaryLOPType {

    @XmlElementRef(name = "booleanOP", namespace = "http://rs.tdwg.org/tapir/1.0", type = JAXBElement.class)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:16+02:00", comments = "JAXB RI v2.2.11")
    protected JAXBElement<?> booleanOP;

    /**
     * Gets the value of the booleanOP property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link BinaryCOPType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinaryCOPType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinaryCOPType }{@code >}
     *     {@link JAXBElement }{@code <}{@link Object }{@code >}
     *     {@link JAXBElement }{@code <}{@link Object }{@code >}
     *     {@link JAXBElement }{@code <}{@link MultiLOPType }{@code >}
     *     {@link JAXBElement }{@code <}{@link Object }{@code >}
     *     {@link JAXBElement }{@code <}{@link MultiLOPType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinaryCOPType }{@code >}
     *     {@link JAXBElement }{@code <}{@link UnaryCOPType }{@code >}
     *     {@link JAXBElement }{@code <}{@link Object }{@code >}
     *     {@link JAXBElement }{@code <}{@link MultiCOPType }{@code >}
     *     {@link JAXBElement }{@code <}{@link Not }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinaryCOPType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinaryCOPType }{@code >}
     *     {@link JAXBElement }{@code <}{@link Object }{@code >}
     *     {@link JAXBElement }{@code <}{@link Object }{@code >}
     *     {@link JAXBElement }{@code <}{@link Object }{@code >}
     *     {@link JAXBElement }{@code <}{@link Object }{@code >}
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:16+02:00", comments = "JAXB RI v2.2.11")
    public JAXBElement<?> getBooleanOP() {
        return booleanOP;
    }

    /**
     * Sets the value of the booleanOP property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link BinaryCOPType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinaryCOPType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinaryCOPType }{@code >}
     *     {@link JAXBElement }{@code <}{@link Object }{@code >}
     *     {@link JAXBElement }{@code <}{@link Object }{@code >}
     *     {@link JAXBElement }{@code <}{@link MultiLOPType }{@code >}
     *     {@link JAXBElement }{@code <}{@link Object }{@code >}
     *     {@link JAXBElement }{@code <}{@link MultiLOPType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinaryCOPType }{@code >}
     *     {@link JAXBElement }{@code <}{@link UnaryCOPType }{@code >}
     *     {@link JAXBElement }{@code <}{@link Object }{@code >}
     *     {@link JAXBElement }{@code <}{@link MultiCOPType }{@code >}
     *     {@link JAXBElement }{@code <}{@link Not }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinaryCOPType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinaryCOPType }{@code >}
     *     {@link JAXBElement }{@code <}{@link Object }{@code >}
     *     {@link JAXBElement }{@code <}{@link Object }{@code >}
     *     {@link JAXBElement }{@code <}{@link Object }{@code >}
     *     {@link JAXBElement }{@code <}{@link Object }{@code >}
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:16+02:00", comments = "JAXB RI v2.2.11")
    public void setBooleanOP(JAXBElement<?> value) {
        this.booleanOP = value;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:16+02:00", comments = "JAXB RI v2.2.11")
    public UnaryLOPType withBooleanOP(JAXBElement<?> value) {
        setBooleanOP(value);
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
