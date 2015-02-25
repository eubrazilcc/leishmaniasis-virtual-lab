//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.02.25 at 09:42:19 AM CET 
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
    "gbSecondaryAccn"
})
@XmlRootElement(name = "GBSeq_secondary-accessions")
public class GBSeqSecondaryAccessions {

    @XmlElement(name = "GBSecondary-accn")
    protected List<GBSecondaryAccn> gbSecondaryAccn;

    /**
     * Gets the value of the gbSecondaryAccn property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the gbSecondaryAccn property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getGBSecondaryAccn().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link GBSecondaryAccn }
     * 
     * 
     */
    public List<GBSecondaryAccn> getGBSecondaryAccn() {
        if (gbSecondaryAccn == null) {
            gbSecondaryAccn = new ArrayList<GBSecondaryAccn>();
        }
        return this.gbSecondaryAccn;
    }

    public GBSeqSecondaryAccessions withGBSecondaryAccn(GBSecondaryAccn... values) {
        if (values!= null) {
            for (GBSecondaryAccn value: values) {
                getGBSecondaryAccn().add(value);
            }
        }
        return this;
    }

    public GBSeqSecondaryAccessions withGBSecondaryAccn(Collection<GBSecondaryAccn> values) {
        if (values!= null) {
            getGBSecondaryAccn().addAll(values);
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
