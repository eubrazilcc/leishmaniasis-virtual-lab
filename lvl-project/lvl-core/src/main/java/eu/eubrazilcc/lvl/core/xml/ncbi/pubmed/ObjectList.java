//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.03.26 at 04:30:37 PM CET 
//


package eu.eubrazilcc.lvl.core.xml.ncbi.pubmed;

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
    "object"
})
@XmlRootElement(name = "ObjectList")
@Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-26T04:30:37+01:00", comments = "JAXB RI v2.2.11")
public class ObjectList {

    @XmlElement(name = "Object", required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-26T04:30:37+01:00", comments = "JAXB RI v2.2.11")
    protected List<eu.eubrazilcc.lvl.core.xml.ncbi.pubmed.Object> object;

    /**
     * Gets the value of the object property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the object property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getObject().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link eu.eubrazilcc.lvl.core.xml.ncbi.pubmed.Object }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-26T04:30:37+01:00", comments = "JAXB RI v2.2.11")
    public List<eu.eubrazilcc.lvl.core.xml.ncbi.pubmed.Object> getObject() {
        if (object == null) {
            object = new ArrayList<eu.eubrazilcc.lvl.core.xml.ncbi.pubmed.Object>();
        }
        return this.object;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-26T04:30:37+01:00", comments = "JAXB RI v2.2.11")
    public ObjectList withObject(eu.eubrazilcc.lvl.core.xml.ncbi.pubmed.Object... values) {
        if (values!= null) {
            for (eu.eubrazilcc.lvl.core.xml.ncbi.pubmed.Object value: values) {
                getObject().add(value);
            }
        }
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-26T04:30:37+01:00", comments = "JAXB RI v2.2.11")
    public ObjectList withObject(Collection<eu.eubrazilcc.lvl.core.xml.ncbi.pubmed.Object> values) {
        if (values!= null) {
            getObject().addAll(values);
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
    public boolean equals(java.lang.Object that) {
        return EqualsBuilder.reflectionEquals(this, that);
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-26T04:30:37+01:00", comments = "JAXB RI v2.2.11")
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

}
