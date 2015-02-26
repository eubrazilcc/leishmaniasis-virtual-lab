//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.02.26 at 11:41:32 PM CET 
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
    "gbFeatureKey",
    "gbFeatureLocation",
    "gbFeatureIntervals",
    "gbFeatureOperator",
    "gbFeaturePartial5",
    "gbFeaturePartial3",
    "gbFeatureQuals",
    "gbFeatureXrefs"
})
@XmlRootElement(name = "GBFeature")
public class GBFeature {

    @XmlElement(name = "GBFeature_key", required = true)
    protected String gbFeatureKey;
    @XmlElement(name = "GBFeature_location", required = true)
    protected String gbFeatureLocation;
    @XmlElement(name = "GBFeature_intervals")
    protected GBFeatureIntervals gbFeatureIntervals;
    @XmlElement(name = "GBFeature_operator")
    protected String gbFeatureOperator;
    @XmlElement(name = "GBFeature_partial5")
    protected GBFeaturePartial5 gbFeaturePartial5;
    @XmlElement(name = "GBFeature_partial3")
    protected GBFeaturePartial3 gbFeaturePartial3;
    @XmlElement(name = "GBFeature_quals")
    protected GBFeatureQuals gbFeatureQuals;
    @XmlElement(name = "GBFeature_xrefs")
    protected GBFeatureXrefs gbFeatureXrefs;

    /**
     * Gets the value of the gbFeatureKey property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGBFeatureKey() {
        return gbFeatureKey;
    }

    /**
     * Sets the value of the gbFeatureKey property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGBFeatureKey(String value) {
        this.gbFeatureKey = value;
    }

    /**
     * Gets the value of the gbFeatureLocation property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGBFeatureLocation() {
        return gbFeatureLocation;
    }

    /**
     * Sets the value of the gbFeatureLocation property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGBFeatureLocation(String value) {
        this.gbFeatureLocation = value;
    }

    /**
     * Gets the value of the gbFeatureIntervals property.
     * 
     * @return
     *     possible object is
     *     {@link GBFeatureIntervals }
     *     
     */
    public GBFeatureIntervals getGBFeatureIntervals() {
        return gbFeatureIntervals;
    }

    /**
     * Sets the value of the gbFeatureIntervals property.
     * 
     * @param value
     *     allowed object is
     *     {@link GBFeatureIntervals }
     *     
     */
    public void setGBFeatureIntervals(GBFeatureIntervals value) {
        this.gbFeatureIntervals = value;
    }

    /**
     * Gets the value of the gbFeatureOperator property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGBFeatureOperator() {
        return gbFeatureOperator;
    }

    /**
     * Sets the value of the gbFeatureOperator property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGBFeatureOperator(String value) {
        this.gbFeatureOperator = value;
    }

    /**
     * Gets the value of the gbFeaturePartial5 property.
     * 
     * @return
     *     possible object is
     *     {@link GBFeaturePartial5 }
     *     
     */
    public GBFeaturePartial5 getGBFeaturePartial5() {
        return gbFeaturePartial5;
    }

    /**
     * Sets the value of the gbFeaturePartial5 property.
     * 
     * @param value
     *     allowed object is
     *     {@link GBFeaturePartial5 }
     *     
     */
    public void setGBFeaturePartial5(GBFeaturePartial5 value) {
        this.gbFeaturePartial5 = value;
    }

    /**
     * Gets the value of the gbFeaturePartial3 property.
     * 
     * @return
     *     possible object is
     *     {@link GBFeaturePartial3 }
     *     
     */
    public GBFeaturePartial3 getGBFeaturePartial3() {
        return gbFeaturePartial3;
    }

    /**
     * Sets the value of the gbFeaturePartial3 property.
     * 
     * @param value
     *     allowed object is
     *     {@link GBFeaturePartial3 }
     *     
     */
    public void setGBFeaturePartial3(GBFeaturePartial3 value) {
        this.gbFeaturePartial3 = value;
    }

    /**
     * Gets the value of the gbFeatureQuals property.
     * 
     * @return
     *     possible object is
     *     {@link GBFeatureQuals }
     *     
     */
    public GBFeatureQuals getGBFeatureQuals() {
        return gbFeatureQuals;
    }

    /**
     * Sets the value of the gbFeatureQuals property.
     * 
     * @param value
     *     allowed object is
     *     {@link GBFeatureQuals }
     *     
     */
    public void setGBFeatureQuals(GBFeatureQuals value) {
        this.gbFeatureQuals = value;
    }

    /**
     * Gets the value of the gbFeatureXrefs property.
     * 
     * @return
     *     possible object is
     *     {@link GBFeatureXrefs }
     *     
     */
    public GBFeatureXrefs getGBFeatureXrefs() {
        return gbFeatureXrefs;
    }

    /**
     * Sets the value of the gbFeatureXrefs property.
     * 
     * @param value
     *     allowed object is
     *     {@link GBFeatureXrefs }
     *     
     */
    public void setGBFeatureXrefs(GBFeatureXrefs value) {
        this.gbFeatureXrefs = value;
    }

    public GBFeature withGBFeatureKey(String value) {
        setGBFeatureKey(value);
        return this;
    }

    public GBFeature withGBFeatureLocation(String value) {
        setGBFeatureLocation(value);
        return this;
    }

    public GBFeature withGBFeatureIntervals(GBFeatureIntervals value) {
        setGBFeatureIntervals(value);
        return this;
    }

    public GBFeature withGBFeatureOperator(String value) {
        setGBFeatureOperator(value);
        return this;
    }

    public GBFeature withGBFeaturePartial5(GBFeaturePartial5 value) {
        setGBFeaturePartial5(value);
        return this;
    }

    public GBFeature withGBFeaturePartial3(GBFeaturePartial3 value) {
        setGBFeaturePartial3(value);
        return this;
    }

    public GBFeature withGBFeatureQuals(GBFeatureQuals value) {
        setGBFeatureQuals(value);
        return this;
    }

    public GBFeature withGBFeatureXrefs(GBFeatureXrefs value) {
        setGBFeatureXrefs(value);
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
