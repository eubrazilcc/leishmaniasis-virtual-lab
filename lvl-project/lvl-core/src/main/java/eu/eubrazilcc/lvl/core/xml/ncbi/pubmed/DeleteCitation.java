//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.09.08 at 06:22:46 PM CEST 
//


package eu.eubrazilcc.lvl.core.xml.ncbi.pubmed;

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
    "pmid"
})
@XmlRootElement(name = "DeleteCitation")
@Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-09-08T06:22:46+02:00", comments = "JAXB RI v2.2.4-2")
public class DeleteCitation {

    @XmlElement(name = "PMID", required = true)
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-09-08T06:22:46+02:00", comments = "JAXB RI v2.2.4-2")
    protected List<PMID> pmid;

    /**
     * Gets the value of the pmid property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the pmid property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPMID().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PMID }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-09-08T06:22:46+02:00", comments = "JAXB RI v2.2.4-2")
    public List<PMID> getPMID() {
        if (pmid == null) {
            pmid = new ArrayList<PMID>();
        }
        return this.pmid;
    }

}