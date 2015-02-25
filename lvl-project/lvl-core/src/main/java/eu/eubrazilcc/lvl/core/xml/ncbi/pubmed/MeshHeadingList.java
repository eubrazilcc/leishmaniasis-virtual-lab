//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.02.25 at 03:44:35 PM CET 
//


package eu.eubrazilcc.lvl.core.xml.ncbi.pubmed;

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
    "meshHeading"
})
@XmlRootElement(name = "MeshHeadingList")
public class MeshHeadingList {

    @XmlElement(name = "MeshHeading", required = true)
    protected List<MeshHeading> meshHeading;

    /**
     * Gets the value of the meshHeading property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the meshHeading property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMeshHeading().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link MeshHeading }
     * 
     * 
     */
    public List<MeshHeading> getMeshHeading() {
        if (meshHeading == null) {
            meshHeading = new ArrayList<MeshHeading>();
        }
        return this.meshHeading;
    }

    public MeshHeadingList withMeshHeading(MeshHeading... values) {
        if (values!= null) {
            for (MeshHeading value: values) {
                getMeshHeading().add(value);
            }
        }
        return this;
    }

    public MeshHeadingList withMeshHeading(Collection<MeshHeading> values) {
        if (values!= null) {
            getMeshHeading().addAll(values);
        }
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    @Override
    public boolean equals(java.lang.Object that) {
        return EqualsBuilder.reflectionEquals(this, that);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

}
