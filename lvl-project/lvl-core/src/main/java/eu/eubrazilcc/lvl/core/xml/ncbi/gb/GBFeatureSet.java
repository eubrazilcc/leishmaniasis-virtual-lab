//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.02.18 at 10:55:54 AM CET 
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
    "gbFeatureSetAnnotSource",
    "gbFeatureSetFeatures"
})
@XmlRootElement(name = "GBFeatureSet")
public class GBFeatureSet {

    @XmlElement(name = "GBFeatureSet_annot-source")
    protected String gbFeatureSetAnnotSource;
    @XmlElement(name = "GBFeatureSet_features", required = true)
    protected GBFeatureSetFeatures gbFeatureSetFeatures;

    /**
     * Gets the value of the gbFeatureSetAnnotSource property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGBFeatureSetAnnotSource() {
        return gbFeatureSetAnnotSource;
    }

    /**
     * Sets the value of the gbFeatureSetAnnotSource property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGBFeatureSetAnnotSource(String value) {
        this.gbFeatureSetAnnotSource = value;
    }

    /**
     * Gets the value of the gbFeatureSetFeatures property.
     * 
     * @return
     *     possible object is
     *     {@link GBFeatureSetFeatures }
     *     
     */
    public GBFeatureSetFeatures getGBFeatureSetFeatures() {
        return gbFeatureSetFeatures;
    }

    /**
     * Sets the value of the gbFeatureSetFeatures property.
     * 
     * @param value
     *     allowed object is
     *     {@link GBFeatureSetFeatures }
     *     
     */
    public void setGBFeatureSetFeatures(GBFeatureSetFeatures value) {
        this.gbFeatureSetFeatures = value;
    }

    public GBFeatureSet withGBFeatureSetAnnotSource(String value) {
        setGBFeatureSetAnnotSource(value);
        return this;
    }

    public GBFeatureSet withGBFeatureSetFeatures(GBFeatureSetFeatures value) {
        setGBFeatureSetFeatures(value);
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
