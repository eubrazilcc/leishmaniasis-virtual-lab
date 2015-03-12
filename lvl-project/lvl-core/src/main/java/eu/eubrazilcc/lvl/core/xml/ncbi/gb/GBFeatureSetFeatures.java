//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.03.12 at 09:34:25 AM CET 
//


package eu.eubrazilcc.lvl.core.xml.ncbi.gb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
    "gbFeature"
})
@XmlRootElement(name = "GBFeatureSet_features")
@Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-12T09:34:25+01:00", comments = "JAXB RI v2.2.11")
public class GBFeatureSetFeatures {

    @XmlElement(name = "GBFeature")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-12T09:34:25+01:00", comments = "JAXB RI v2.2.11")
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
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-12T09:34:25+01:00", comments = "JAXB RI v2.2.11")
    public List<GBFeature> getGBFeature() {
        if (gbFeature == null) {
            gbFeature = new ArrayList<GBFeature>();
        }
        return this.gbFeature;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-12T09:34:25+01:00", comments = "JAXB RI v2.2.11")
    public GBFeatureSetFeatures withGBFeature(GBFeature... values) {
        if (values!= null) {
            for (GBFeature value: values) {
                getGBFeature().add(value);
            }
        }
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-12T09:34:25+01:00", comments = "JAXB RI v2.2.11")
    public GBFeatureSetFeatures withGBFeature(Collection<GBFeature> values) {
        if (values!= null) {
            getGBFeature().addAll(values);
        }
        return this;
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-12T09:34:25+01:00", comments = "JAXB RI v2.2.11")
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-12T09:34:25+01:00", comments = "JAXB RI v2.2.11")
    public boolean equals(Object that) {
        return EqualsBuilder.reflectionEquals(this, that);
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-12T09:34:25+01:00", comments = "JAXB RI v2.2.11")
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

}
