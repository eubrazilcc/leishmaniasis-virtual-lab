//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.04.28 at 08:19:58 PM CEST 
//


package eu.eubrazilcc.lvl.core.xml.ncbi;

import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


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
@Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
public class GBFeature {

    @XmlElement(name = "GBFeature_key", required = true)
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    protected String gbFeatureKey;
    @XmlElement(name = "GBFeature_location", required = true)
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    protected String gbFeatureLocation;
    @XmlElement(name = "GBFeature_intervals")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    protected GBFeatureIntervals gbFeatureIntervals;
    @XmlElement(name = "GBFeature_operator")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    protected String gbFeatureOperator;
    @XmlElement(name = "GBFeature_partial5")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    protected GBFeaturePartial5 gbFeaturePartial5;
    @XmlElement(name = "GBFeature_partial3")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    protected GBFeaturePartial3 gbFeaturePartial3;
    @XmlElement(name = "GBFeature_quals")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    protected GBFeatureQuals gbFeatureQuals;
    @XmlElement(name = "GBFeature_xrefs")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    protected GBFeatureXrefs gbFeatureXrefs;

    /**
     * Gets the value of the gbFeatureKey property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
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
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
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
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
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
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
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
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
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
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
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
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
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
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
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
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
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
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
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
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
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
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
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
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
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
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
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
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
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
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    public void setGBFeatureXrefs(GBFeatureXrefs value) {
        this.gbFeatureXrefs = value;
    }

}
