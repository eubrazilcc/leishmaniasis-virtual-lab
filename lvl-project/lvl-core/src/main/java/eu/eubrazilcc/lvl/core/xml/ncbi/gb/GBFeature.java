//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.02.20 at 12:24:39 PM CET 
//


package eu.eubrazilcc.lvl.core.xml.ncbi.gb;

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
@Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-20T12:24:39+01:00", comments = "JAXB RI v2.2.11")
public class GBFeature {

    @XmlElement(name = "GBFeature_key", required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-20T12:24:39+01:00", comments = "JAXB RI v2.2.11")
    protected String gbFeatureKey;
    @XmlElement(name = "GBFeature_location", required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-20T12:24:39+01:00", comments = "JAXB RI v2.2.11")
    protected String gbFeatureLocation;
    @XmlElement(name = "GBFeature_intervals")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-20T12:24:39+01:00", comments = "JAXB RI v2.2.11")
    protected GBFeatureIntervals gbFeatureIntervals;
    @XmlElement(name = "GBFeature_operator")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-20T12:24:39+01:00", comments = "JAXB RI v2.2.11")
    protected String gbFeatureOperator;
    @XmlElement(name = "GBFeature_partial5")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-20T12:24:39+01:00", comments = "JAXB RI v2.2.11")
    protected GBFeaturePartial5 gbFeaturePartial5;
    @XmlElement(name = "GBFeature_partial3")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-20T12:24:39+01:00", comments = "JAXB RI v2.2.11")
    protected GBFeaturePartial3 gbFeaturePartial3;
    @XmlElement(name = "GBFeature_quals")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-20T12:24:39+01:00", comments = "JAXB RI v2.2.11")
    protected GBFeatureQuals gbFeatureQuals;
    @XmlElement(name = "GBFeature_xrefs")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-20T12:24:39+01:00", comments = "JAXB RI v2.2.11")
    protected GBFeatureXrefs gbFeatureXrefs;

    /**
     * Gets the value of the gbFeatureKey property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-20T12:24:39+01:00", comments = "JAXB RI v2.2.11")
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
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-20T12:24:39+01:00", comments = "JAXB RI v2.2.11")
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
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-20T12:24:39+01:00", comments = "JAXB RI v2.2.11")
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
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-20T12:24:39+01:00", comments = "JAXB RI v2.2.11")
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
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-20T12:24:39+01:00", comments = "JAXB RI v2.2.11")
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
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-20T12:24:39+01:00", comments = "JAXB RI v2.2.11")
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
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-20T12:24:39+01:00", comments = "JAXB RI v2.2.11")
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
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-20T12:24:39+01:00", comments = "JAXB RI v2.2.11")
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
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-20T12:24:39+01:00", comments = "JAXB RI v2.2.11")
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
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-20T12:24:39+01:00", comments = "JAXB RI v2.2.11")
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
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-20T12:24:39+01:00", comments = "JAXB RI v2.2.11")
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
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-20T12:24:39+01:00", comments = "JAXB RI v2.2.11")
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
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-20T12:24:39+01:00", comments = "JAXB RI v2.2.11")
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
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-20T12:24:39+01:00", comments = "JAXB RI v2.2.11")
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
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-20T12:24:39+01:00", comments = "JAXB RI v2.2.11")
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
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-20T12:24:39+01:00", comments = "JAXB RI v2.2.11")
    public void setGBFeatureXrefs(GBFeatureXrefs value) {
        this.gbFeatureXrefs = value;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-20T12:24:39+01:00", comments = "JAXB RI v2.2.11")
    public GBFeature withGBFeatureKey(String value) {
        setGBFeatureKey(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-20T12:24:39+01:00", comments = "JAXB RI v2.2.11")
    public GBFeature withGBFeatureLocation(String value) {
        setGBFeatureLocation(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-20T12:24:39+01:00", comments = "JAXB RI v2.2.11")
    public GBFeature withGBFeatureIntervals(GBFeatureIntervals value) {
        setGBFeatureIntervals(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-20T12:24:39+01:00", comments = "JAXB RI v2.2.11")
    public GBFeature withGBFeatureOperator(String value) {
        setGBFeatureOperator(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-20T12:24:39+01:00", comments = "JAXB RI v2.2.11")
    public GBFeature withGBFeaturePartial5(GBFeaturePartial5 value) {
        setGBFeaturePartial5(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-20T12:24:39+01:00", comments = "JAXB RI v2.2.11")
    public GBFeature withGBFeaturePartial3(GBFeaturePartial3 value) {
        setGBFeaturePartial3(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-20T12:24:39+01:00", comments = "JAXB RI v2.2.11")
    public GBFeature withGBFeatureQuals(GBFeatureQuals value) {
        setGBFeatureQuals(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-20T12:24:39+01:00", comments = "JAXB RI v2.2.11")
    public GBFeature withGBFeatureXrefs(GBFeatureXrefs value) {
        setGBFeatureXrefs(value);
        return this;
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-20T12:24:39+01:00", comments = "JAXB RI v2.2.11")
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-20T12:24:39+01:00", comments = "JAXB RI v2.2.11")
    public boolean equals(Object that) {
        return EqualsBuilder.reflectionEquals(this, that);
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-20T12:24:39+01:00", comments = "JAXB RI v2.2.11")
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

}
