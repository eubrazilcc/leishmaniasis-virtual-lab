//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.02.24 at 07:19:04 PM CET 
//


package eu.eubrazilcc.lvl.core.xml.ncbi.gb;

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
    "gbIntervalFrom",
    "gbIntervalTo",
    "gbIntervalPoint",
    "gbIntervalIscomp",
    "gbIntervalInterbp",
    "gbIntervalAccession"
})
@XmlRootElement(name = "GBInterval")
public class GBInterval {

    @XmlElement(name = "GBInterval_from")
    protected String gbIntervalFrom;
    @XmlElement(name = "GBInterval_to")
    protected String gbIntervalTo;
    @XmlElement(name = "GBInterval_point")
    protected String gbIntervalPoint;
    @XmlElement(name = "GBInterval_iscomp")
    protected GBIntervalIscomp gbIntervalIscomp;
    @XmlElement(name = "GBInterval_interbp")
    protected GBIntervalInterbp gbIntervalInterbp;
    @XmlElement(name = "GBInterval_accession", required = true)
    protected String gbIntervalAccession;

    /**
     * Gets the value of the gbIntervalFrom property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGBIntervalFrom() {
        return gbIntervalFrom;
    }

    /**
     * Sets the value of the gbIntervalFrom property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGBIntervalFrom(String value) {
        this.gbIntervalFrom = value;
    }

    /**
     * Gets the value of the gbIntervalTo property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGBIntervalTo() {
        return gbIntervalTo;
    }

    /**
     * Sets the value of the gbIntervalTo property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGBIntervalTo(String value) {
        this.gbIntervalTo = value;
    }

    /**
     * Gets the value of the gbIntervalPoint property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGBIntervalPoint() {
        return gbIntervalPoint;
    }

    /**
     * Sets the value of the gbIntervalPoint property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGBIntervalPoint(String value) {
        this.gbIntervalPoint = value;
    }

    /**
     * Gets the value of the gbIntervalIscomp property.
     * 
     * @return
     *     possible object is
     *     {@link GBIntervalIscomp }
     *     
     */
    public GBIntervalIscomp getGBIntervalIscomp() {
        return gbIntervalIscomp;
    }

    /**
     * Sets the value of the gbIntervalIscomp property.
     * 
     * @param value
     *     allowed object is
     *     {@link GBIntervalIscomp }
     *     
     */
    public void setGBIntervalIscomp(GBIntervalIscomp value) {
        this.gbIntervalIscomp = value;
    }

    /**
     * Gets the value of the gbIntervalInterbp property.
     * 
     * @return
     *     possible object is
     *     {@link GBIntervalInterbp }
     *     
     */
    public GBIntervalInterbp getGBIntervalInterbp() {
        return gbIntervalInterbp;
    }

    /**
     * Sets the value of the gbIntervalInterbp property.
     * 
     * @param value
     *     allowed object is
     *     {@link GBIntervalInterbp }
     *     
     */
    public void setGBIntervalInterbp(GBIntervalInterbp value) {
        this.gbIntervalInterbp = value;
    }

    /**
     * Gets the value of the gbIntervalAccession property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGBIntervalAccession() {
        return gbIntervalAccession;
    }

    /**
     * Sets the value of the gbIntervalAccession property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGBIntervalAccession(String value) {
        this.gbIntervalAccession = value;
    }

    public GBInterval withGBIntervalFrom(String value) {
        setGBIntervalFrom(value);
        return this;
    }

    public GBInterval withGBIntervalTo(String value) {
        setGBIntervalTo(value);
        return this;
    }

    public GBInterval withGBIntervalPoint(String value) {
        setGBIntervalPoint(value);
        return this;
    }

    public GBInterval withGBIntervalIscomp(GBIntervalIscomp value) {
        setGBIntervalIscomp(value);
        return this;
    }

    public GBInterval withGBIntervalInterbp(GBIntervalInterbp value) {
        setGBIntervalInterbp(value);
        return this;
    }

    public GBInterval withGBIntervalAccession(String value) {
        setGBIntervalAccession(value);
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    @Override
    public boolean equals(Object that) {
        return EqualsBuilder.reflectionEquals(this, that);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

}
