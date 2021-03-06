//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.11.27 at 05:37:46 PM CET 
//


package eu.eubrazilcc.lvl.core.xml.tdwg.tapir;

import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for entityRoleType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="entityRoleType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="data supplier"/&gt;
 *     &lt;enumeration value="technical host"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "entityRoleType")
@XmlEnum
@Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:46+01:00", comments = "JAXB RI v2.2.11")
public enum EntityRoleType {

    @XmlEnumValue("data supplier")
    DATA_SUPPLIER("data supplier"),
    @XmlEnumValue("technical host")
    TECHNICAL_HOST("technical host");
    private final String value;

    EntityRoleType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static EntityRoleType fromValue(String v) {
        for (EntityRoleType c: EntityRoleType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
