//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.07.20 at 09:54:46 AM CEST 
//


package eu.eubrazilcc.lvl.core.xml.tdwg.tapir;

import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;


/**
 * Response structures are part of a model definition which
 * 				includes three sections: a record schema (structure), a mapping section for linking to the datasource model
 * 				and an indexing section that is used as a reference for record counting and paging responses.
 * 			               The outputModel template is a separate, external XML document, called by a search operation,
 * 				that uses  'outputModel' as the root element. 
 * 
 * <p>Java class for outputModelType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="outputModelType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;group ref="{http://rs.tdwg.org/tapir/1.0}outputModelGroup"/&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "outputModelType", propOrder = {
    "structure",
    "rootElement",
    "indexingElement",
    "mapping"
})
@Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:45+02:00", comments = "JAXB RI v2.2.11")
public class OutputModelType {

    @XmlElement(required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:45+02:00", comments = "JAXB RI v2.2.11")
    protected eu.eubrazilcc.lvl.core.xml.tdwg.tapir.OutputModel.Structure structure;
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:45+02:00", comments = "JAXB RI v2.2.11")
    protected eu.eubrazilcc.lvl.core.xml.tdwg.tapir.OutputModel.RootElement rootElement;
    @XmlElement(required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:45+02:00", comments = "JAXB RI v2.2.11")
    protected eu.eubrazilcc.lvl.core.xml.tdwg.tapir.OutputModel.IndexingElement indexingElement;
    @XmlElement(required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:45+02:00", comments = "JAXB RI v2.2.11")
    protected eu.eubrazilcc.lvl.core.xml.tdwg.tapir.OutputModel.Mapping mapping;

    /**
     * Gets the value of the structure property.
     * 
     * @return
     *     possible object is
     *     {@link eu.eubrazilcc.lvl.core.xml.tdwg.tapir.OutputModel.Structure }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:45+02:00", comments = "JAXB RI v2.2.11")
    public eu.eubrazilcc.lvl.core.xml.tdwg.tapir.OutputModel.Structure getStructure() {
        return structure;
    }

    /**
     * Sets the value of the structure property.
     * 
     * @param value
     *     allowed object is
     *     {@link eu.eubrazilcc.lvl.core.xml.tdwg.tapir.OutputModel.Structure }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:45+02:00", comments = "JAXB RI v2.2.11")
    public void setStructure(eu.eubrazilcc.lvl.core.xml.tdwg.tapir.OutputModel.Structure value) {
        this.structure = value;
    }

    /**
     * Gets the value of the rootElement property.
     * 
     * @return
     *     possible object is
     *     {@link eu.eubrazilcc.lvl.core.xml.tdwg.tapir.OutputModel.RootElement }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:45+02:00", comments = "JAXB RI v2.2.11")
    public eu.eubrazilcc.lvl.core.xml.tdwg.tapir.OutputModel.RootElement getRootElement() {
        return rootElement;
    }

    /**
     * Sets the value of the rootElement property.
     * 
     * @param value
     *     allowed object is
     *     {@link eu.eubrazilcc.lvl.core.xml.tdwg.tapir.OutputModel.RootElement }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:45+02:00", comments = "JAXB RI v2.2.11")
    public void setRootElement(eu.eubrazilcc.lvl.core.xml.tdwg.tapir.OutputModel.RootElement value) {
        this.rootElement = value;
    }

    /**
     * Gets the value of the indexingElement property.
     * 
     * @return
     *     possible object is
     *     {@link eu.eubrazilcc.lvl.core.xml.tdwg.tapir.OutputModel.IndexingElement }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:45+02:00", comments = "JAXB RI v2.2.11")
    public eu.eubrazilcc.lvl.core.xml.tdwg.tapir.OutputModel.IndexingElement getIndexingElement() {
        return indexingElement;
    }

    /**
     * Sets the value of the indexingElement property.
     * 
     * @param value
     *     allowed object is
     *     {@link eu.eubrazilcc.lvl.core.xml.tdwg.tapir.OutputModel.IndexingElement }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:45+02:00", comments = "JAXB RI v2.2.11")
    public void setIndexingElement(eu.eubrazilcc.lvl.core.xml.tdwg.tapir.OutputModel.IndexingElement value) {
        this.indexingElement = value;
    }

    /**
     * Gets the value of the mapping property.
     * 
     * @return
     *     possible object is
     *     {@link eu.eubrazilcc.lvl.core.xml.tdwg.tapir.OutputModel.Mapping }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:45+02:00", comments = "JAXB RI v2.2.11")
    public eu.eubrazilcc.lvl.core.xml.tdwg.tapir.OutputModel.Mapping getMapping() {
        return mapping;
    }

    /**
     * Sets the value of the mapping property.
     * 
     * @param value
     *     allowed object is
     *     {@link eu.eubrazilcc.lvl.core.xml.tdwg.tapir.OutputModel.Mapping }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:45+02:00", comments = "JAXB RI v2.2.11")
    public void setMapping(eu.eubrazilcc.lvl.core.xml.tdwg.tapir.OutputModel.Mapping value) {
        this.mapping = value;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:45+02:00", comments = "JAXB RI v2.2.11")
    public OutputModelType withStructure(eu.eubrazilcc.lvl.core.xml.tdwg.tapir.OutputModel.Structure value) {
        setStructure(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:45+02:00", comments = "JAXB RI v2.2.11")
    public OutputModelType withRootElement(eu.eubrazilcc.lvl.core.xml.tdwg.tapir.OutputModel.RootElement value) {
        setRootElement(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:45+02:00", comments = "JAXB RI v2.2.11")
    public OutputModelType withIndexingElement(eu.eubrazilcc.lvl.core.xml.tdwg.tapir.OutputModel.IndexingElement value) {
        setIndexingElement(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:45+02:00", comments = "JAXB RI v2.2.11")
    public OutputModelType withMapping(eu.eubrazilcc.lvl.core.xml.tdwg.tapir.OutputModel.Mapping value) {
        setMapping(value);
        return this;
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:45+02:00", comments = "JAXB RI v2.2.11")
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:45+02:00", comments = "JAXB RI v2.2.11")
    public boolean equals(Object that) {
        return EqualsBuilder.reflectionEquals(this, that);
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:45+02:00", comments = "JAXB RI v2.2.11")
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

}
