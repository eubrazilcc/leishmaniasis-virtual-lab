//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.07.23 at 01:05:16 PM CEST 
//


package eu.eubrazilcc.lvl.core.xml.tdwg.tapir;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Generated;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;


/**
 * A complex type for multiple comparison operators.
 * 
 * <p>Java class for multiCOPType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="multiCOPType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://rs.tdwg.org/tapir/1.0}concept"/&gt;
 *         &lt;element name="values"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element ref="{http://rs.tdwg.org/tapir/1.0}simpleExpression" maxOccurs="unbounded"/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "multiCOPType", propOrder = {
    "concept",
    "values"
})
@Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:16+02:00", comments = "JAXB RI v2.2.11")
public class MultiCOPType {

    @XmlElement(required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:16+02:00", comments = "JAXB RI v2.2.11")
    protected ConceptType concept;
    @XmlElement(required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:16+02:00", comments = "JAXB RI v2.2.11")
    protected MultiCOPType.Values values;

    /**
     * Gets the value of the concept property.
     * 
     * @return
     *     possible object is
     *     {@link ConceptType }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:16+02:00", comments = "JAXB RI v2.2.11")
    public ConceptType getConcept() {
        return concept;
    }

    /**
     * Sets the value of the concept property.
     * 
     * @param value
     *     allowed object is
     *     {@link ConceptType }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:16+02:00", comments = "JAXB RI v2.2.11")
    public void setConcept(ConceptType value) {
        this.concept = value;
    }

    /**
     * Gets the value of the values property.
     * 
     * @return
     *     possible object is
     *     {@link MultiCOPType.Values }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:16+02:00", comments = "JAXB RI v2.2.11")
    public MultiCOPType.Values getValues() {
        return values;
    }

    /**
     * Sets the value of the values property.
     * 
     * @param value
     *     allowed object is
     *     {@link MultiCOPType.Values }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:16+02:00", comments = "JAXB RI v2.2.11")
    public void setValues(MultiCOPType.Values value) {
        this.values = value;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:16+02:00", comments = "JAXB RI v2.2.11")
    public MultiCOPType withConcept(ConceptType value) {
        setConcept(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:16+02:00", comments = "JAXB RI v2.2.11")
    public MultiCOPType withValues(MultiCOPType.Values value) {
        setValues(value);
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


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;sequence&gt;
     *         &lt;element ref="{http://rs.tdwg.org/tapir/1.0}simpleExpression" maxOccurs="unbounded"/&gt;
     *       &lt;/sequence&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "simpleExpression"
    })
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:16+02:00", comments = "JAXB RI v2.2.11")
    public static class Values {

        @XmlElementRef(name = "simpleExpression", namespace = "http://rs.tdwg.org/tapir/1.0", type = JAXBElement.class)
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:16+02:00", comments = "JAXB RI v2.2.11")
        protected List<JAXBElement<?>> simpleExpression;

        /**
         * Gets the value of the simpleExpression property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the simpleExpression property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getSimpleExpression().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link JAXBElement }{@code <}{@link Literal }{@code >}
         * {@link JAXBElement }{@code <}{@link Parameter }{@code >}
         * {@link JAXBElement }{@code <}{@link Object }{@code >}
         * {@link JAXBElement }{@code <}{@link ConceptType }{@code >}
         * 
         * 
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:16+02:00", comments = "JAXB RI v2.2.11")
        public List<JAXBElement<?>> getSimpleExpression() {
            if (simpleExpression == null) {
                simpleExpression = new ArrayList<JAXBElement<?>>();
            }
            return this.simpleExpression;
        }

        @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:16+02:00", comments = "JAXB RI v2.2.11")
        public MultiCOPType.Values withSimpleExpression(JAXBElement<?> ... values) {
            if (values!= null) {
                for (JAXBElement<?> value: values) {
                    getSimpleExpression().add(value);
                }
            }
            return this;
        }

        @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:16+02:00", comments = "JAXB RI v2.2.11")
        public MultiCOPType.Values withSimpleExpression(Collection<JAXBElement<?>> values) {
            if (values!= null) {
                getSimpleExpression().addAll(values);
            }
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

}
