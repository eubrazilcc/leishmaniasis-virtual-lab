//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.02.17 at 10:57:46 PM CET 
//


package eu.eubrazilcc.lvl.core.xml.ncbi.pubmed;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
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
    "startPageOrEndPageOrMedlinePgn"
})
@XmlRootElement(name = "Pagination")
@Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T10:57:46+01:00", comments = "JAXB RI v2.2.11")
public class Pagination {

    @XmlElements({
        @XmlElement(name = "StartPage", required = true, type = StartPage.class),
        @XmlElement(name = "EndPage", required = true, type = EndPage.class),
        @XmlElement(name = "MedlinePgn", required = true, type = MedlinePgn.class)
    })
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T10:57:46+01:00", comments = "JAXB RI v2.2.11")
    protected List<java.lang.Object> startPageOrEndPageOrMedlinePgn;

    /**
     * Gets the value of the startPageOrEndPageOrMedlinePgn property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the startPageOrEndPageOrMedlinePgn property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getStartPageOrEndPageOrMedlinePgn().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link StartPage }
     * {@link EndPage }
     * {@link MedlinePgn }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T10:57:46+01:00", comments = "JAXB RI v2.2.11")
    public List<java.lang.Object> getStartPageOrEndPageOrMedlinePgn() {
        if (startPageOrEndPageOrMedlinePgn == null) {
            startPageOrEndPageOrMedlinePgn = new ArrayList<java.lang.Object>();
        }
        return this.startPageOrEndPageOrMedlinePgn;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T10:57:46+01:00", comments = "JAXB RI v2.2.11")
    public Pagination withStartPageOrEndPageOrMedlinePgn(java.lang.Object... values) {
        if (values!= null) {
            for (java.lang.Object value: values) {
                getStartPageOrEndPageOrMedlinePgn().add(value);
            }
        }
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T10:57:46+01:00", comments = "JAXB RI v2.2.11")
    public Pagination withStartPageOrEndPageOrMedlinePgn(Collection<java.lang.Object> values) {
        if (values!= null) {
            getStartPageOrEndPageOrMedlinePgn().addAll(values);
        }
        return this;
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T10:57:46+01:00", comments = "JAXB RI v2.2.11")
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T10:57:46+01:00", comments = "JAXB RI v2.2.11")
    public boolean equals(java.lang.Object that) {
        return EqualsBuilder.reflectionEquals(this, that);
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T10:57:46+01:00", comments = "JAXB RI v2.2.11")
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

}
