//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.07.10 at 02:13:39 PM CEST 
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
    "descriptorName",
    "qualifierName"
})
@XmlRootElement(name = "MeshHeading")
@Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-10T02:13:39+02:00", comments = "JAXB RI v2.2.11")
public class MeshHeading {

    @XmlElement(name = "DescriptorName", required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-10T02:13:39+02:00", comments = "JAXB RI v2.2.11")
    protected DescriptorName descriptorName;
    @XmlElement(name = "QualifierName")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-10T02:13:39+02:00", comments = "JAXB RI v2.2.11")
    protected List<QualifierName> qualifierName;

    /**
     * Gets the value of the descriptorName property.
     * 
     * @return
     *     possible object is
     *     {@link DescriptorName }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-10T02:13:39+02:00", comments = "JAXB RI v2.2.11")
    public DescriptorName getDescriptorName() {
        return descriptorName;
    }

    /**
     * Sets the value of the descriptorName property.
     * 
     * @param value
     *     allowed object is
     *     {@link DescriptorName }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-10T02:13:39+02:00", comments = "JAXB RI v2.2.11")
    public void setDescriptorName(DescriptorName value) {
        this.descriptorName = value;
    }

    /**
     * Gets the value of the qualifierName property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the qualifierName property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getQualifierName().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link QualifierName }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-10T02:13:39+02:00", comments = "JAXB RI v2.2.11")
    public List<QualifierName> getQualifierName() {
        if (qualifierName == null) {
            qualifierName = new ArrayList<QualifierName>();
        }
        return this.qualifierName;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-10T02:13:39+02:00", comments = "JAXB RI v2.2.11")
    public MeshHeading withDescriptorName(DescriptorName value) {
        setDescriptorName(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-10T02:13:39+02:00", comments = "JAXB RI v2.2.11")
    public MeshHeading withQualifierName(QualifierName... values) {
        if (values!= null) {
            for (QualifierName value: values) {
                getQualifierName().add(value);
            }
        }
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-10T02:13:39+02:00", comments = "JAXB RI v2.2.11")
    public MeshHeading withQualifierName(Collection<QualifierName> values) {
        if (values!= null) {
            getQualifierName().addAll(values);
        }
        return this;
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-10T02:13:39+02:00", comments = "JAXB RI v2.2.11")
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-10T02:13:39+02:00", comments = "JAXB RI v2.2.11")
    public boolean equals(java.lang.Object that) {
        return EqualsBuilder.reflectionEquals(this, that);
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-10T02:13:39+02:00", comments = "JAXB RI v2.2.11")
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

}
