//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.04.28 at 08:19:58 PM CEST 
//


package eu.eubrazilcc.lvl.core.xml.ncbi;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "gbSecondaryAccn"
})
@XmlRootElement(name = "GBSeq_secondary-accessions")
@Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
public class GBSeqSecondaryAccessions {

    @XmlElement(name = "GBSecondary-accn")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    protected List<GBSecondaryAccn> gbSecondaryAccn;

    /**
     * Gets the value of the gbSecondaryAccn property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the gbSecondaryAccn property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getGBSecondaryAccn().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link GBSecondaryAccn }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    public List<GBSecondaryAccn> getGBSecondaryAccn() {
        if (gbSecondaryAccn == null) {
            gbSecondaryAccn = new ArrayList<GBSecondaryAccn>();
        }
        return this.gbSecondaryAccn;
    }

}
