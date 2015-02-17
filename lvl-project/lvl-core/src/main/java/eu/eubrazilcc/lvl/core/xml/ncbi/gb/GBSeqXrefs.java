//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.02.17 at 06:39:18 PM CET 
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
    "gbXref"
})
@XmlRootElement(name = "GBSeq_xrefs")
@Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T06:39:18+01:00", comments = "JAXB RI v2.2.11")
public class GBSeqXrefs {

    @XmlElement(name = "GBXref")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T06:39:18+01:00", comments = "JAXB RI v2.2.11")
    protected List<GBXref> gbXref;

    /**
     * Gets the value of the gbXref property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the gbXref property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getGBXref().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link GBXref }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T06:39:18+01:00", comments = "JAXB RI v2.2.11")
    public List<GBXref> getGBXref() {
        if (gbXref == null) {
            gbXref = new ArrayList<GBXref>();
        }
        return this.gbXref;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T06:39:18+01:00", comments = "JAXB RI v2.2.11")
    public GBSeqXrefs withGBXref(GBXref... values) {
        if (values!= null) {
            for (GBXref value: values) {
                getGBXref().add(value);
            }
        }
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T06:39:18+01:00", comments = "JAXB RI v2.2.11")
    public GBSeqXrefs withGBXref(Collection<GBXref> values) {
        if (values!= null) {
            getGBXref().addAll(values);
        }
        return this;
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T06:39:18+01:00", comments = "JAXB RI v2.2.11")
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T06:39:18+01:00", comments = "JAXB RI v2.2.11")
    public boolean equals(Object that) {
        return EqualsBuilder.reflectionEquals(this, that);
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T06:39:18+01:00", comments = "JAXB RI v2.2.11")
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

}
