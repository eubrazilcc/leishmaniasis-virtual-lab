//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.07.23 at 01:04:58 PM CEST 
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
    "gbInterval"
})
@XmlRootElement(name = "GBFeature_intervals")
@Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:04:58+02:00", comments = "JAXB RI v2.2.11")
public class GBFeatureIntervals {

    @XmlElement(name = "GBInterval")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:04:58+02:00", comments = "JAXB RI v2.2.11")
    protected List<GBInterval> gbInterval;

    /**
     * Gets the value of the gbInterval property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the gbInterval property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getGBInterval().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link GBInterval }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:04:58+02:00", comments = "JAXB RI v2.2.11")
    public List<GBInterval> getGBInterval() {
        if (gbInterval == null) {
            gbInterval = new ArrayList<GBInterval>();
        }
        return this.gbInterval;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:04:58+02:00", comments = "JAXB RI v2.2.11")
    public GBFeatureIntervals withGBInterval(GBInterval... values) {
        if (values!= null) {
            for (GBInterval value: values) {
                getGBInterval().add(value);
            }
        }
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:04:58+02:00", comments = "JAXB RI v2.2.11")
    public GBFeatureIntervals withGBInterval(Collection<GBInterval> values) {
        if (values!= null) {
            getGBInterval().addAll(values);
        }
        return this;
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:04:58+02:00", comments = "JAXB RI v2.2.11")
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:04:58+02:00", comments = "JAXB RI v2.2.11")
    public boolean equals(Object that) {
        return EqualsBuilder.reflectionEquals(this, that);
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:04:58+02:00", comments = "JAXB RI v2.2.11")
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

}
