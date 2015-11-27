//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.11.27 at 05:37:46 PM CET 
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
 * Entity information.
 * 
 * <p>Java class for entityInformationType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="entityInformationType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="identifier" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="name" type="{http://rs.tdwg.org/tapir/1.0}languageAwareElementType" maxOccurs="unbounded"/&gt;
 *         &lt;element name="acronym" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="logoURL" type="{http://www.w3.org/2001/XMLSchema}anyURI" minOccurs="0"/&gt;
 *         &lt;element name="description" type="{http://rs.tdwg.org/tapir/1.0}languageAwareElementType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="address" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="regionCode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="countryCode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="zipCode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="relatedInformation" type="{http://www.w3.org/2001/XMLSchema}anyURI" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="hasContact" maxOccurs="unbounded"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="role" type="{http://rs.tdwg.org/tapir/1.0}contactRoleExtensionType" maxOccurs="unbounded"/&gt;
 *                   &lt;element ref="{http://www.w3.org/2001/vcard-rdf/3.0#}VCARD"/&gt;
 *                   &lt;element ref="{http://rs.tdwg.org/tapir/1.0}custom" minOccurs="0"/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element ref="{http://www.w3.org/2003/01/geo/wgs84_pos#}Point" minOccurs="0"/&gt;
 *         &lt;element ref="{http://rs.tdwg.org/tapir/1.0}custom" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="type" default="organization"&gt;
 *         &lt;simpleType&gt;
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *             &lt;enumeration value="organization"/&gt;
 *             &lt;enumeration value="person"/&gt;
 *           &lt;/restriction&gt;
 *         &lt;/simpleType&gt;
 *       &lt;/attribute&gt;
 *       &lt;attribute ref="{http://www.w3.org/XML/1998/namespace}lang"/&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "entityInformationType", propOrder = {
    "identifier",
    "name",
    "acronym",
    "logoURL",
    "description",
    "address",
    "regionCode",
    "countryCode",
    "zipCode",
    "relatedInformation",
    "hasContact",
    "point",
    "custom"
})
@Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
public class EntityInformationType {

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
    protected String identifier;
    @XmlElement(required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
    protected List<LanguageAwareElementType> name;
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
    protected String acronym;
    @XmlSchemaType(name = "anyURI")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
    protected String logoURL;
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
    protected List<LanguageAwareElementType> description;
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
    protected String address;
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
    protected String regionCode;
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
    protected String countryCode;
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
    protected String zipCode;
    @XmlSchemaType(name = "anyURI")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
    protected List<String> relatedInformation;
    @XmlElement(required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
    protected List<EntityInformationType.HasContact> hasContact;
    @XmlElement(name = "Point", namespace = "http://www.w3.org/2003/01/geo/wgs84_pos#")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
    protected Point point;
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
    protected Custom custom;
    @XmlAttribute(name = "type")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
    protected String type;
    @XmlAttribute(name = "lang", namespace = "http://www.w3.org/XML/1998/namespace")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
    protected String lang;

    /**
     * Gets the value of the identifier property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Sets the value of the identifier property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
    public void setIdentifier(String value) {
        this.identifier = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the name property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getName().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link LanguageAwareElementType }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
    public List<LanguageAwareElementType> getName() {
        if (name == null) {
            name = new ArrayList<LanguageAwareElementType>();
        }
        return this.name;
    }

    /**
     * Gets the value of the acronym property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
    public String getAcronym() {
        return acronym;
    }

    /**
     * Sets the value of the acronym property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
    public void setAcronym(String value) {
        this.acronym = value;
    }

    /**
     * Gets the value of the logoURL property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
    public String getLogoURL() {
        return logoURL;
    }

    /**
     * Sets the value of the logoURL property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
    public void setLogoURL(String value) {
        this.logoURL = value;
    }

    /**
     * Gets the value of the description property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the description property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDescription().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link LanguageAwareElementType }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
    public List<LanguageAwareElementType> getDescription() {
        if (description == null) {
            description = new ArrayList<LanguageAwareElementType>();
        }
        return this.description;
    }

    /**
     * Gets the value of the address property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
    public String getAddress() {
        return address;
    }

    /**
     * Sets the value of the address property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
    public void setAddress(String value) {
        this.address = value;
    }

    /**
     * Gets the value of the regionCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
    public String getRegionCode() {
        return regionCode;
    }

    /**
     * Sets the value of the regionCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
    public void setRegionCode(String value) {
        this.regionCode = value;
    }

    /**
     * Gets the value of the countryCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
    public String getCountryCode() {
        return countryCode;
    }

    /**
     * Sets the value of the countryCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
    public void setCountryCode(String value) {
        this.countryCode = value;
    }

    /**
     * Gets the value of the zipCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
    public String getZipCode() {
        return zipCode;
    }

    /**
     * Sets the value of the zipCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
    public void setZipCode(String value) {
        this.zipCode = value;
    }

    /**
     * Gets the value of the relatedInformation property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the relatedInformation property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRelatedInformation().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
    public List<String> getRelatedInformation() {
        if (relatedInformation == null) {
            relatedInformation = new ArrayList<String>();
        }
        return this.relatedInformation;
    }

    /**
     * Gets the value of the hasContact property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the hasContact property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getHasContact().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link EntityInformationType.HasContact }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
    public List<EntityInformationType.HasContact> getHasContact() {
        if (hasContact == null) {
            hasContact = new ArrayList<EntityInformationType.HasContact>();
        }
        return this.hasContact;
    }

    /**
     * Location of the entity in decimal WGS84 latitude and longitude (and optional altitude) as defined by the W3C Basic Geo Vocabulary
     * 
     * @return
     *     possible object is
     *     {@link Point }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
    public Point getPoint() {
        return point;
    }

    /**
     * Sets the value of the point property.
     * 
     * @param value
     *     allowed object is
     *     {@link Point }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
    public void setPoint(Point value) {
        this.point = value;
    }

    /**
     * Gets the value of the custom property.
     * 
     * @return
     *     possible object is
     *     {@link Custom }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
    public Custom getCustom() {
        return custom;
    }

    /**
     * Sets the value of the custom property.
     * 
     * @param value
     *     allowed object is
     *     {@link Custom }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
    public void setCustom(Custom value) {
        this.custom = value;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
    public String getType() {
        if (type == null) {
            return "organization";
        } else {
            return type;
        }
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
    public void setType(String value) {
        this.type = value;
    }

    /**
     * Gets the value of the lang property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
    public String getLang() {
        return lang;
    }

    /**
     * Sets the value of the lang property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
    public void setLang(String value) {
        this.lang = value;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
    public EntityInformationType withIdentifier(String value) {
        setIdentifier(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
    public EntityInformationType withName(LanguageAwareElementType... values) {
        if (values!= null) {
            for (LanguageAwareElementType value: values) {
                getName().add(value);
            }
        }
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
    public EntityInformationType withName(Collection<LanguageAwareElementType> values) {
        if (values!= null) {
            getName().addAll(values);
        }
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
    public EntityInformationType withAcronym(String value) {
        setAcronym(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
    public EntityInformationType withLogoURL(String value) {
        setLogoURL(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
    public EntityInformationType withDescription(LanguageAwareElementType... values) {
        if (values!= null) {
            for (LanguageAwareElementType value: values) {
                getDescription().add(value);
            }
        }
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
    public EntityInformationType withDescription(Collection<LanguageAwareElementType> values) {
        if (values!= null) {
            getDescription().addAll(values);
        }
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
    public EntityInformationType withAddress(String value) {
        setAddress(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
    public EntityInformationType withRegionCode(String value) {
        setRegionCode(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
    public EntityInformationType withCountryCode(String value) {
        setCountryCode(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
    public EntityInformationType withZipCode(String value) {
        setZipCode(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
    public EntityInformationType withRelatedInformation(String... values) {
        if (values!= null) {
            for (String value: values) {
                getRelatedInformation().add(value);
            }
        }
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
    public EntityInformationType withRelatedInformation(Collection<String> values) {
        if (values!= null) {
            getRelatedInformation().addAll(values);
        }
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
    public EntityInformationType withHasContact(EntityInformationType.HasContact... values) {
        if (values!= null) {
            for (EntityInformationType.HasContact value: values) {
                getHasContact().add(value);
            }
        }
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
    public EntityInformationType withHasContact(Collection<EntityInformationType.HasContact> values) {
        if (values!= null) {
            getHasContact().addAll(values);
        }
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
    public EntityInformationType withPoint(Point value) {
        setPoint(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
    public EntityInformationType withCustom(Custom value) {
        setCustom(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
    public EntityInformationType withType(String value) {
        setType(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
    public EntityInformationType withLang(String value) {
        setLang(value);
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


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;sequence&gt;
     *         &lt;element name="role" type="{http://rs.tdwg.org/tapir/1.0}contactRoleExtensionType" maxOccurs="unbounded"/&gt;
     *         &lt;element ref="{http://www.w3.org/2001/vcard-rdf/3.0#}VCARD"/&gt;
     *         &lt;element ref="{http://rs.tdwg.org/tapir/1.0}custom" minOccurs="0"/&gt;
     *       &lt;/sequence&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "role",
        "vcard",
        "custom"
    })
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
    public static class HasContact {

        @XmlElement(required = true)
        @XmlSchemaType(name = "anySimpleType")
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
        protected List<String> role;
        @XmlElement(name = "VCARD", namespace = "http://www.w3.org/2001/vcard-rdf/3.0#", required = true)
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
        protected VCARD vcard;
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
        protected Custom custom;

        /**
         * Gets the value of the role property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the role property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getRole().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link String }
         * 
         * 
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
        public List<String> getRole() {
            if (role == null) {
                role = new ArrayList<String>();
            }
            return this.role;
        }

        /**
         * Gets the value of the vcard property.
         * 
         * @return
         *     possible object is
         *     {@link VCARD }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
        public VCARD getVCARD() {
            return vcard;
        }

        /**
         * Sets the value of the vcard property.
         * 
         * @param value
         *     allowed object is
         *     {@link VCARD }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
        public void setVCARD(VCARD value) {
            this.vcard = value;
        }

        /**
         * Gets the value of the custom property.
         * 
         * @return
         *     possible object is
         *     {@link Custom }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
        public Custom getCustom() {
            return custom;
        }

        /**
         * Sets the value of the custom property.
         * 
         * @param value
         *     allowed object is
         *     {@link Custom }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
        public void setCustom(Custom value) {
            this.custom = value;
        }

        @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
        public EntityInformationType.HasContact withRole(String... values) {
            if (values!= null) {
                for (String value: values) {
                    getRole().add(value);
                }
            }
            return this;
        }

        @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
        public EntityInformationType.HasContact withRole(Collection<String> values) {
            if (values!= null) {
                getRole().addAll(values);
            }
            return this;
        }

        @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
        public EntityInformationType.HasContact withVCARD(VCARD value) {
            setVCARD(value);
            return this;
        }

        @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
        public EntityInformationType.HasContact withCustom(Custom value) {
            setCustom(value);
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

}
