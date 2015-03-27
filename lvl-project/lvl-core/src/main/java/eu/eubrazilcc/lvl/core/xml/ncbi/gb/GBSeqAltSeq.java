//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.03.26 at 04:30:37 PM CET 
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
    "gbAltSeqData"
})
@XmlRootElement(name = "GBSeq_alt-seq")
@Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-26T04:30:37+01:00", comments = "JAXB RI v2.2.11")
public class GBSeqAltSeq {

    @XmlElement(name = "GBAltSeqData")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-26T04:30:37+01:00", comments = "JAXB RI v2.2.11")
    protected List<GBAltSeqData> gbAltSeqData;

    /**
     * Gets the value of the gbAltSeqData property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the gbAltSeqData property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getGBAltSeqData().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link GBAltSeqData }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-26T04:30:37+01:00", comments = "JAXB RI v2.2.11")
    public List<GBAltSeqData> getGBAltSeqData() {
        if (gbAltSeqData == null) {
            gbAltSeqData = new ArrayList<GBAltSeqData>();
        }
        return this.gbAltSeqData;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-26T04:30:37+01:00", comments = "JAXB RI v2.2.11")
    public GBSeqAltSeq withGBAltSeqData(GBAltSeqData... values) {
        if (values!= null) {
            for (GBAltSeqData value: values) {
                getGBAltSeqData().add(value);
            }
        }
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-26T04:30:37+01:00", comments = "JAXB RI v2.2.11")
    public GBSeqAltSeq withGBAltSeqData(Collection<GBAltSeqData> values) {
        if (values!= null) {
            getGBAltSeqData().addAll(values);
        }
        return this;
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-26T04:30:37+01:00", comments = "JAXB RI v2.2.11")
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-26T04:30:37+01:00", comments = "JAXB RI v2.2.11")
    public boolean equals(Object that) {
        return EqualsBuilder.reflectionEquals(this, that);
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-26T04:30:37+01:00", comments = "JAXB RI v2.2.11")
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

}
