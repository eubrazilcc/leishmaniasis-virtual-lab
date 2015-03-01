//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.03.01 at 03:36:39 PM CET 
//


package eu.eubrazilcc.lvl.core.xml.ncbi.esearch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
    "termSetOrOP"
})
@XmlRootElement(name = "TranslationStack")
public class TranslationStack {

    @XmlElements({
        @XmlElement(name = "TermSet", type = TermSet.class),
        @XmlElement(name = "OP", type = OP.class)
    })
    protected List<Object> termSetOrOP;

    /**
     * Gets the value of the termSetOrOP property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the termSetOrOP property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTermSetOrOP().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TermSet }
     * {@link OP }
     * 
     * 
     */
    public List<Object> getTermSetOrOP() {
        if (termSetOrOP == null) {
            termSetOrOP = new ArrayList<Object>();
        }
        return this.termSetOrOP;
    }

    public TranslationStack withTermSetOrOP(Object... values) {
        if (values!= null) {
            for (Object value: values) {
                getTermSetOrOP().add(value);
            }
        }
        return this;
    }

    public TranslationStack withTermSetOrOP(Collection<Object> values) {
        if (values!= null) {
            getTermSetOrOP().addAll(values);
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
