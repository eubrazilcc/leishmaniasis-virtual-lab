//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.03.11 at 05:46:54 PM CET 
//


package eu.eubrazilcc.lvl.core.xml.ncbi.esearch;

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
    "id"
})
@XmlRootElement(name = "IdList")
@Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-11T05:46:54+01:00", comments = "JAXB RI v2.2.11")
public class IdList {

    @XmlElement(name = "Id")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-11T05:46:54+01:00", comments = "JAXB RI v2.2.11")
    protected List<Id> id;

    /**
     * Gets the value of the id property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the id property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getId().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Id }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-11T05:46:54+01:00", comments = "JAXB RI v2.2.11")
    public List<Id> getId() {
        if (id == null) {
            id = new ArrayList<Id>();
        }
        return this.id;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-11T05:46:54+01:00", comments = "JAXB RI v2.2.11")
    public IdList withId(Id... values) {
        if (values!= null) {
            for (Id value: values) {
                getId().add(value);
            }
        }
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-11T05:46:54+01:00", comments = "JAXB RI v2.2.11")
    public IdList withId(Collection<Id> values) {
        if (values!= null) {
            getId().addAll(values);
        }
        return this;
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-11T05:46:54+01:00", comments = "JAXB RI v2.2.11")
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-11T05:46:54+01:00", comments = "JAXB RI v2.2.11")
    public boolean equals(Object that) {
        return EqualsBuilder.reflectionEquals(this, that);
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-11T05:46:54+01:00", comments = "JAXB RI v2.2.11")
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

}
