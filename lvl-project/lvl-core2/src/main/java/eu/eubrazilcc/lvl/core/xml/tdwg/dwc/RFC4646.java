//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.07.23 at 01:05:18 PM CEST 
//


package eu.eubrazilcc.lvl.core.xml.tdwg.dwc;

import java.util.Collection;
import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;


/**
 * <p>Java class for RFC4646 complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RFC4646"&gt;
 *   &lt;simpleContent&gt;
 *     &lt;restriction base="&lt;http://purl.org/dc/elements/1.1/&gt;SimpleLiteral"&gt;
 *     &lt;/restriction&gt;
 *   &lt;/simpleContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RFC4646")
@Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:18+02:00", comments = "JAXB RI v2.2.11")
public class RFC4646
    extends SimpleLiteral
{


    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:18+02:00", comments = "JAXB RI v2.2.11")
    public RFC4646 withContent(String... values) {
        if (values!= null) {
            for (String value: values) {
                getContent().add(value);
            }
        }
        return this;
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:18+02:00", comments = "JAXB RI v2.2.11")
    public RFC4646 withContent(Collection<String> values) {
        if (values!= null) {
            getContent().addAll(values);
        }
        return this;
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:18+02:00", comments = "JAXB RI v2.2.11")
    public RFC4646 withLang(String value) {
        setLang(value);
        return this;
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:18+02:00", comments = "JAXB RI v2.2.11")
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:18+02:00", comments = "JAXB RI v2.2.11")
    public boolean equals(Object that) {
        return EqualsBuilder.reflectionEquals(this, that);
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:18+02:00", comments = "JAXB RI v2.2.11")
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

}
