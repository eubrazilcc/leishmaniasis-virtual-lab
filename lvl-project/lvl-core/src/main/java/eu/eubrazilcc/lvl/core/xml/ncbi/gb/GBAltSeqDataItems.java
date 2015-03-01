//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.03.01 at 11:30:20 AM CET 
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
    "gbAltSeqItem"
})
@XmlRootElement(name = "GBAltSeqData_items")
public class GBAltSeqDataItems {

    @XmlElement(name = "GBAltSeqItem")
    protected List<GBAltSeqItem> gbAltSeqItem;

    /**
     * Gets the value of the gbAltSeqItem property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the gbAltSeqItem property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getGBAltSeqItem().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link GBAltSeqItem }
     * 
     * 
     */
    public List<GBAltSeqItem> getGBAltSeqItem() {
        if (gbAltSeqItem == null) {
            gbAltSeqItem = new ArrayList<GBAltSeqItem>();
        }
        return this.gbAltSeqItem;
    }

    public GBAltSeqDataItems withGBAltSeqItem(GBAltSeqItem... values) {
        if (values!= null) {
            for (GBAltSeqItem value: values) {
                getGBAltSeqItem().add(value);
            }
        }
        return this;
    }

    public GBAltSeqDataItems withGBAltSeqItem(Collection<GBAltSeqItem> values) {
        if (values!= null) {
            getGBAltSeqItem().addAll(values);
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
