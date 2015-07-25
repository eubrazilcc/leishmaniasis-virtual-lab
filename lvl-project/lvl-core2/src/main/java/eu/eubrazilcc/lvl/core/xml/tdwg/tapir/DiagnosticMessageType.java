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
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.datatype.XMLGregorianCalendar;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;


/**
 * A diagnostic message from the service with message type and time presented as attributes
 * 				including code, level ( with enumerated types e.g. error, warning, additional
 * 				information, etc). and a time stamp
 * 
 * <p>Java class for diagnosticMessageType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="diagnosticMessageType"&gt;
 *   &lt;simpleContent&gt;
 *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema&gt;string"&gt;
 *       &lt;attribute name="code" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="level" use="required"&gt;
 *         &lt;simpleType&gt;
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *             &lt;enumeration value="debug"/&gt;
 *             &lt;enumeration value="info"/&gt;
 *             &lt;enumeration value="warn"/&gt;
 *             &lt;enumeration value="error"/&gt;
 *             &lt;enumeration value="fatal"/&gt;
 *           &lt;/restriction&gt;
 *         &lt;/simpleType&gt;
 *       &lt;/attribute&gt;
 *       &lt;attribute name="time" type="{http://www.w3.org/2001/XMLSchema}dateTime" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/simpleContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "diagnosticMessageType", propOrder = {
    "value"
})
@Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:16+02:00", comments = "JAXB RI v2.2.11")
public class DiagnosticMessageType {

    @XmlValue
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:16+02:00", comments = "JAXB RI v2.2.11")
    protected String value;
    @XmlAttribute(name = "code")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:16+02:00", comments = "JAXB RI v2.2.11")
    protected String code;
    @XmlAttribute(name = "level", required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:16+02:00", comments = "JAXB RI v2.2.11")
    protected String level;
    @XmlAttribute(name = "time")
    @XmlSchemaType(name = "dateTime")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:16+02:00", comments = "JAXB RI v2.2.11")
    protected XMLGregorianCalendar time;

    /**
     * Gets the value of the value property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:16+02:00", comments = "JAXB RI v2.2.11")
    public String getValue() {
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
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:16+02:00", comments = "JAXB RI v2.2.11")
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Gets the value of the code property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:16+02:00", comments = "JAXB RI v2.2.11")
    public String getCode() {
        return code;
    }

    /**
     * Sets the value of the code property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:16+02:00", comments = "JAXB RI v2.2.11")
    public void setCode(String value) {
        this.code = value;
    }

    /**
     * Gets the value of the level property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:16+02:00", comments = "JAXB RI v2.2.11")
    public String getLevel() {
        return level;
    }

    /**
     * Sets the value of the level property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:16+02:00", comments = "JAXB RI v2.2.11")
    public void setLevel(String value) {
        this.level = value;
    }

    /**
     * Gets the value of the time property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:16+02:00", comments = "JAXB RI v2.2.11")
    public XMLGregorianCalendar getTime() {
        return time;
    }

    /**
     * Sets the value of the time property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:16+02:00", comments = "JAXB RI v2.2.11")
    public void setTime(XMLGregorianCalendar value) {
        this.time = value;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:16+02:00", comments = "JAXB RI v2.2.11")
    public DiagnosticMessageType withValue(String value) {
        setValue(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:16+02:00", comments = "JAXB RI v2.2.11")
    public DiagnosticMessageType withCode(String value) {
        setCode(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:16+02:00", comments = "JAXB RI v2.2.11")
    public DiagnosticMessageType withLevel(String value) {
        setLevel(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:16+02:00", comments = "JAXB RI v2.2.11")
    public DiagnosticMessageType withTime(XMLGregorianCalendar value) {
        setTime(value);
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
