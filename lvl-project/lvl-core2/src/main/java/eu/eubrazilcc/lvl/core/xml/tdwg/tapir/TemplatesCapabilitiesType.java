//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.07.20 at 09:54:46 AM CEST 
//


package eu.eubrazilcc.lvl.core.xml.tdwg.tapir;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;


/**
 * <p>Java class for templatesCapabilitiesType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="templatesCapabilitiesType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="template" maxOccurs="unbounded"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;extension base="{http://rs.tdwg.org/tapir/1.0}externalResourceType"&gt;
 *                 &lt;attribute name="alias" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *                 &lt;attribute name="wsdl" type="{http://www.w3.org/2001/XMLSchema}anyURI" /&gt;
 *               &lt;/extension&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "templatesCapabilitiesType", propOrder = {
    "template"
})
@Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:45+02:00", comments = "JAXB RI v2.2.11")
public class TemplatesCapabilitiesType {

    @XmlElement(required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:45+02:00", comments = "JAXB RI v2.2.11")
    protected List<TemplatesCapabilitiesType.Template> template;

    /**
     * Gets the value of the template property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the template property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTemplate().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TemplatesCapabilitiesType.Template }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:45+02:00", comments = "JAXB RI v2.2.11")
    public List<TemplatesCapabilitiesType.Template> getTemplate() {
        if (template == null) {
            template = new ArrayList<TemplatesCapabilitiesType.Template>();
        }
        return this.template;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:45+02:00", comments = "JAXB RI v2.2.11")
    public TemplatesCapabilitiesType withTemplate(TemplatesCapabilitiesType.Template... values) {
        if (values!= null) {
            for (TemplatesCapabilitiesType.Template value: values) {
                getTemplate().add(value);
            }
        }
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:45+02:00", comments = "JAXB RI v2.2.11")
    public TemplatesCapabilitiesType withTemplate(Collection<TemplatesCapabilitiesType.Template> values) {
        if (values!= null) {
            getTemplate().addAll(values);
        }
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


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;extension base="{http://rs.tdwg.org/tapir/1.0}externalResourceType"&gt;
     *       &lt;attribute name="alias" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
     *       &lt;attribute name="wsdl" type="{http://www.w3.org/2001/XMLSchema}anyURI" /&gt;
     *     &lt;/extension&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:45+02:00", comments = "JAXB RI v2.2.11")
    public static class Template
        extends ExternalResourceType
    {

        @XmlAttribute(name = "alias")
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:45+02:00", comments = "JAXB RI v2.2.11")
        protected String alias;
        @XmlAttribute(name = "wsdl")
        @XmlSchemaType(name = "anyURI")
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:45+02:00", comments = "JAXB RI v2.2.11")
        protected String wsdl;

        /**
         * Gets the value of the alias property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:45+02:00", comments = "JAXB RI v2.2.11")
        public String getAlias() {
            return alias;
        }

        /**
         * Sets the value of the alias property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:45+02:00", comments = "JAXB RI v2.2.11")
        public void setAlias(String value) {
            this.alias = value;
        }

        /**
         * Gets the value of the wsdl property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:45+02:00", comments = "JAXB RI v2.2.11")
        public String getWsdl() {
            return wsdl;
        }

        /**
         * Sets the value of the wsdl property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:45+02:00", comments = "JAXB RI v2.2.11")
        public void setWsdl(String value) {
            this.wsdl = value;
        }

        @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:45+02:00", comments = "JAXB RI v2.2.11")
        public TemplatesCapabilitiesType.Template withAlias(String value) {
            setAlias(value);
            return this;
        }

        @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:45+02:00", comments = "JAXB RI v2.2.11")
        public TemplatesCapabilitiesType.Template withWsdl(String value) {
            setWsdl(value);
            return this;
        }

        @Override
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:45+02:00", comments = "JAXB RI v2.2.11")
        public TemplatesCapabilitiesType.Template withLocation(String value) {
            setLocation(value);
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

}
