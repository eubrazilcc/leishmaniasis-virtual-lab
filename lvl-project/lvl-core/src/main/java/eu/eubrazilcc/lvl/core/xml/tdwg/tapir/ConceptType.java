//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.11.27 at 05:37:46 PM CET 
//


package eu.eubrazilcc.lvl.core.xml.tdwg.tapir;

import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;


/**
 * <p>Java class for conceptType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="conceptType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://rs.tdwg.org/tapir/1.0}qualifiedConceptReferenceType"&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "conceptType")
@XmlSeeAlso({
    eu.eubrazilcc.lvl.core.xml.tdwg.tapir.OutputModel.Mapping.Node.Concept.class,
    eu.eubrazilcc.lvl.core.xml.tdwg.tapir.RequestType.Search.OrderBy.Concept.class
})
@Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
public class ConceptType
    extends QualifiedConceptReferenceType
{


    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
    public ConceptType withId(String value) {
        setId(value);
        return this;
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
    public boolean equals(Object that) {
        return EqualsBuilder.reflectionEquals(this, that);
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

}