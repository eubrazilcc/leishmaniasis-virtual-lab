//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.02.26 at 11:41:32 PM CET 
//


package eu.eubrazilcc.lvl.core.xml.ncbi.gb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
    "gbFeature"
})
@XmlRootElement(name = "GBFeatureSet_features")
public class GBFeatureSetFeatures {

    @XmlElement(name = "GBFeature")
    protected List<GBFeature> gbFeature;

    /**
     * Gets the value of the gbFeature property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the gbFeature property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getGBFeature().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link GBFeature }
     * 
     * 
     */
    public List<GBFeature> getGBFeature() {
        if (gbFeature == null) {
            gbFeature = new ArrayList<GBFeature>();
        }
        return this.gbFeature;
    }

    public GBFeatureSetFeatures withGBFeature(GBFeature... values) {
        if (values!= null) {
            for (GBFeature value: values) {
                getGBFeature().add(value);
            }
        }
        return this;
    }

    public GBFeatureSetFeatures withGBFeature(Collection<GBFeature> values) {
        if (values!= null) {
            getGBFeature().addAll(values);
        }
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
