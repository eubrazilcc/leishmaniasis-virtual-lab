//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.07.23 at 01:05:16 PM CEST 
//


package eu.eubrazilcc.lvl.core.xml.tdwg.tapir;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.w3c.dom.Element;


/**
 * Format of inventory operation result.
 * 
 * <p>Java class for inventoryResultType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="inventoryResultType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="concepts"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element ref="{http://rs.tdwg.org/tapir/1.0}concept" maxOccurs="unbounded"/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="record" maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;any processContents='skip' namespace='http://rs.tdwg.org/tapir/1.0' maxOccurs="unbounded"/&gt;
 *                 &lt;/sequence&gt;
 *                 &lt;attribute name="count" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" /&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="summary" type="{http://rs.tdwg.org/tapir/1.0}resultSummaryType"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "inventoryResultType", propOrder = {
    "concepts",
    "record",
    "summary"
})
@Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:16+02:00", comments = "JAXB RI v2.2.11")
public class InventoryResultType {

    @XmlElement(required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:16+02:00", comments = "JAXB RI v2.2.11")
    protected InventoryResultType.Concepts concepts;
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:16+02:00", comments = "JAXB RI v2.2.11")
    protected List<InventoryResultType.Record> record;
    @XmlElement(required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:16+02:00", comments = "JAXB RI v2.2.11")
    protected ResultSummaryType summary;

    /**
     * Gets the value of the concepts property.
     * 
     * @return
     *     possible object is
     *     {@link InventoryResultType.Concepts }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:16+02:00", comments = "JAXB RI v2.2.11")
    public InventoryResultType.Concepts getConcepts() {
        return concepts;
    }

    /**
     * Sets the value of the concepts property.
     * 
     * @param value
     *     allowed object is
     *     {@link InventoryResultType.Concepts }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:16+02:00", comments = "JAXB RI v2.2.11")
    public void setConcepts(InventoryResultType.Concepts value) {
        this.concepts = value;
    }

    /**
     * Gets the value of the record property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the record property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRecord().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link InventoryResultType.Record }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:16+02:00", comments = "JAXB RI v2.2.11")
    public List<InventoryResultType.Record> getRecord() {
        if (record == null) {
            record = new ArrayList<InventoryResultType.Record>();
        }
        return this.record;
    }

    /**
     * Gets the value of the summary property.
     * 
     * @return
     *     possible object is
     *     {@link ResultSummaryType }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:16+02:00", comments = "JAXB RI v2.2.11")
    public ResultSummaryType getSummary() {
        return summary;
    }

    /**
     * Sets the value of the summary property.
     * 
     * @param value
     *     allowed object is
     *     {@link ResultSummaryType }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:16+02:00", comments = "JAXB RI v2.2.11")
    public void setSummary(ResultSummaryType value) {
        this.summary = value;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:16+02:00", comments = "JAXB RI v2.2.11")
    public InventoryResultType withConcepts(InventoryResultType.Concepts value) {
        setConcepts(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:16+02:00", comments = "JAXB RI v2.2.11")
    public InventoryResultType withRecord(InventoryResultType.Record... values) {
        if (values!= null) {
            for (InventoryResultType.Record value: values) {
                getRecord().add(value);
            }
        }
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:16+02:00", comments = "JAXB RI v2.2.11")
    public InventoryResultType withRecord(Collection<InventoryResultType.Record> values) {
        if (values!= null) {
            getRecord().addAll(values);
        }
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:16+02:00", comments = "JAXB RI v2.2.11")
    public InventoryResultType withSummary(ResultSummaryType value) {
        setSummary(value);
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
     *         &lt;element ref="{http://rs.tdwg.org/tapir/1.0}concept" maxOccurs="unbounded"/&gt;
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
        "concept"
    })
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:16+02:00", comments = "JAXB RI v2.2.11")
    public static class Concepts {

        @XmlElement(required = true)
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:16+02:00", comments = "JAXB RI v2.2.11")
        protected List<ConceptType> concept;

        /**
         * Gets the value of the concept property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the concept property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getConcept().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link ConceptType }
         * 
         * 
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:16+02:00", comments = "JAXB RI v2.2.11")
        public List<ConceptType> getConcept() {
            if (concept == null) {
                concept = new ArrayList<ConceptType>();
            }
            return this.concept;
        }

        @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:16+02:00", comments = "JAXB RI v2.2.11")
        public InventoryResultType.Concepts withConcept(ConceptType... values) {
            if (values!= null) {
                for (ConceptType value: values) {
                    getConcept().add(value);
                }
            }
            return this;
        }

        @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:16+02:00", comments = "JAXB RI v2.2.11")
        public InventoryResultType.Concepts withConcept(Collection<ConceptType> values) {
            if (values!= null) {
                getConcept().addAll(values);
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
     *         &lt;any processContents='skip' namespace='http://rs.tdwg.org/tapir/1.0' maxOccurs="unbounded"/&gt;
     *       &lt;/sequence&gt;
     *       &lt;attribute name="count" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" /&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "any"
    })
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:16+02:00", comments = "JAXB RI v2.2.11")
    public static class Record {

        @XmlAnyElement
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:16+02:00", comments = "JAXB RI v2.2.11")
        protected List<Element> any;
        @XmlAttribute(name = "count")
        @XmlSchemaType(name = "positiveInteger")
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:16+02:00", comments = "JAXB RI v2.2.11")
        protected BigInteger count;

        /**
         * Gets the value of the any property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the any property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getAny().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link Element }
         * 
         * 
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:16+02:00", comments = "JAXB RI v2.2.11")
        public List<Element> getAny() {
            if (any == null) {
                any = new ArrayList<Element>();
            }
            return this.any;
        }

        /**
         * Gets the value of the count property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:16+02:00", comments = "JAXB RI v2.2.11")
        public BigInteger getCount() {
            return count;
        }

        /**
         * Sets the value of the count property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:16+02:00", comments = "JAXB RI v2.2.11")
        public void setCount(BigInteger value) {
            this.count = value;
        }

        @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:16+02:00", comments = "JAXB RI v2.2.11")
        public InventoryResultType.Record withAny(Element... values) {
            if (values!= null) {
                for (Element value: values) {
                    getAny().add(value);
                }
            }
            return this;
        }

        @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:16+02:00", comments = "JAXB RI v2.2.11")
        public InventoryResultType.Record withAny(Collection<Element> values) {
            if (values!= null) {
                getAny().addAll(values);
            }
            return this;
        }

        @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:16+02:00", comments = "JAXB RI v2.2.11")
        public InventoryResultType.Record withCount(BigInteger value) {
            setCount(value);
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
