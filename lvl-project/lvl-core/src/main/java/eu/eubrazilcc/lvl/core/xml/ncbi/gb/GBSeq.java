//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.04.28 at 08:19:58 PM CEST 
//


package eu.eubrazilcc.lvl.core.xml.ncbi.gb;

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
    "gbSeqLocus",
    "gbSeqLength",
    "gbSeqStrandedness",
    "gbSeqMoltype",
    "gbSeqTopology",
    "gbSeqDivision",
    "gbSeqUpdateDate",
    "gbSeqCreateDate",
    "gbSeqUpdateRelease",
    "gbSeqCreateRelease",
    "gbSeqDefinition",
    "gbSeqPrimaryAccession",
    "gbSeqEntryVersion",
    "gbSeqAccessionVersion",
    "gbSeqOtherSeqids",
    "gbSeqSecondaryAccessions",
    "gbSeqProject",
    "gbSeqKeywords",
    "gbSeqSegment",
    "gbSeqSource",
    "gbSeqOrganism",
    "gbSeqTaxonomy",
    "gbSeqReferences",
    "gbSeqComment",
    "gbSeqCommentSet",
    "gbSeqStrucComments",
    "gbSeqPrimary",
    "gbSeqSourceDb",
    "gbSeqDatabaseReference",
    "gbSeqFeatureTable",
    "gbSeqFeatureSet",
    "gbSeqSequence",
    "gbSeqContig",
    "gbSeqAltSeq",
    "gbSeqXrefs"
})
@XmlRootElement(name = "GBSeq")
@Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
public class GBSeq {

    @XmlElement(name = "GBSeq_locus")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    protected String gbSeqLocus;
    @XmlElement(name = "GBSeq_length", required = true)
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    protected String gbSeqLength;
    @XmlElement(name = "GBSeq_strandedness")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    protected String gbSeqStrandedness;
    @XmlElement(name = "GBSeq_moltype", required = true)
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    protected String gbSeqMoltype;
    @XmlElement(name = "GBSeq_topology")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    protected String gbSeqTopology;
    @XmlElement(name = "GBSeq_division")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    protected String gbSeqDivision;
    @XmlElement(name = "GBSeq_update-date")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    protected String gbSeqUpdateDate;
    @XmlElement(name = "GBSeq_create-date")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    protected String gbSeqCreateDate;
    @XmlElement(name = "GBSeq_update-release")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    protected String gbSeqUpdateRelease;
    @XmlElement(name = "GBSeq_create-release")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    protected String gbSeqCreateRelease;
    @XmlElement(name = "GBSeq_definition")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    protected String gbSeqDefinition;
    @XmlElement(name = "GBSeq_primary-accession")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    protected String gbSeqPrimaryAccession;
    @XmlElement(name = "GBSeq_entry-version")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    protected String gbSeqEntryVersion;
    @XmlElement(name = "GBSeq_accession-version")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    protected String gbSeqAccessionVersion;
    @XmlElement(name = "GBSeq_other-seqids")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    protected GBSeqOtherSeqids gbSeqOtherSeqids;
    @XmlElement(name = "GBSeq_secondary-accessions")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    protected GBSeqSecondaryAccessions gbSeqSecondaryAccessions;
    @XmlElement(name = "GBSeq_project")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    protected String gbSeqProject;
    @XmlElement(name = "GBSeq_keywords")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    protected GBSeqKeywords gbSeqKeywords;
    @XmlElement(name = "GBSeq_segment")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    protected String gbSeqSegment;
    @XmlElement(name = "GBSeq_source")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    protected String gbSeqSource;
    @XmlElement(name = "GBSeq_organism")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    protected String gbSeqOrganism;
    @XmlElement(name = "GBSeq_taxonomy")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    protected String gbSeqTaxonomy;
    @XmlElement(name = "GBSeq_references")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    protected GBSeqReferences gbSeqReferences;
    @XmlElement(name = "GBSeq_comment")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    protected String gbSeqComment;
    @XmlElement(name = "GBSeq_comment-set")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    protected GBSeqCommentSet gbSeqCommentSet;
    @XmlElement(name = "GBSeq_struc-comments")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    protected GBSeqStrucComments gbSeqStrucComments;
    @XmlElement(name = "GBSeq_primary")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    protected String gbSeqPrimary;
    @XmlElement(name = "GBSeq_source-db")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    protected String gbSeqSourceDb;
    @XmlElement(name = "GBSeq_database-reference")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    protected String gbSeqDatabaseReference;
    @XmlElement(name = "GBSeq_feature-table")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    protected GBSeqFeatureTable gbSeqFeatureTable;
    @XmlElement(name = "GBSeq_feature-set")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    protected GBSeqFeatureSet gbSeqFeatureSet;
    @XmlElement(name = "GBSeq_sequence")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    protected String gbSeqSequence;
    @XmlElement(name = "GBSeq_contig")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    protected String gbSeqContig;
    @XmlElement(name = "GBSeq_alt-seq")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    protected GBSeqAltSeq gbSeqAltSeq;
    @XmlElement(name = "GBSeq_xrefs")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    protected GBSeqXrefs gbSeqXrefs;

    /**
     * Gets the value of the gbSeqLocus property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    public String getGBSeqLocus() {
        return gbSeqLocus;
    }

    /**
     * Sets the value of the gbSeqLocus property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    public void setGBSeqLocus(String value) {
        this.gbSeqLocus = value;
    }

    /**
     * Gets the value of the gbSeqLength property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    public String getGBSeqLength() {
        return gbSeqLength;
    }

    /**
     * Sets the value of the gbSeqLength property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    public void setGBSeqLength(String value) {
        this.gbSeqLength = value;
    }

    /**
     * Gets the value of the gbSeqStrandedness property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    public String getGBSeqStrandedness() {
        return gbSeqStrandedness;
    }

    /**
     * Sets the value of the gbSeqStrandedness property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    public void setGBSeqStrandedness(String value) {
        this.gbSeqStrandedness = value;
    }

    /**
     * Gets the value of the gbSeqMoltype property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    public String getGBSeqMoltype() {
        return gbSeqMoltype;
    }

    /**
     * Sets the value of the gbSeqMoltype property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    public void setGBSeqMoltype(String value) {
        this.gbSeqMoltype = value;
    }

    /**
     * Gets the value of the gbSeqTopology property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    public String getGBSeqTopology() {
        return gbSeqTopology;
    }

    /**
     * Sets the value of the gbSeqTopology property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    public void setGBSeqTopology(String value) {
        this.gbSeqTopology = value;
    }

    /**
     * Gets the value of the gbSeqDivision property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    public String getGBSeqDivision() {
        return gbSeqDivision;
    }

    /**
     * Sets the value of the gbSeqDivision property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    public void setGBSeqDivision(String value) {
        this.gbSeqDivision = value;
    }

    /**
     * Gets the value of the gbSeqUpdateDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    public String getGBSeqUpdateDate() {
        return gbSeqUpdateDate;
    }

    /**
     * Sets the value of the gbSeqUpdateDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    public void setGBSeqUpdateDate(String value) {
        this.gbSeqUpdateDate = value;
    }

    /**
     * Gets the value of the gbSeqCreateDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    public String getGBSeqCreateDate() {
        return gbSeqCreateDate;
    }

    /**
     * Sets the value of the gbSeqCreateDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    public void setGBSeqCreateDate(String value) {
        this.gbSeqCreateDate = value;
    }

    /**
     * Gets the value of the gbSeqUpdateRelease property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    public String getGBSeqUpdateRelease() {
        return gbSeqUpdateRelease;
    }

    /**
     * Sets the value of the gbSeqUpdateRelease property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    public void setGBSeqUpdateRelease(String value) {
        this.gbSeqUpdateRelease = value;
    }

    /**
     * Gets the value of the gbSeqCreateRelease property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    public String getGBSeqCreateRelease() {
        return gbSeqCreateRelease;
    }

    /**
     * Sets the value of the gbSeqCreateRelease property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    public void setGBSeqCreateRelease(String value) {
        this.gbSeqCreateRelease = value;
    }

    /**
     * Gets the value of the gbSeqDefinition property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    public String getGBSeqDefinition() {
        return gbSeqDefinition;
    }

    /**
     * Sets the value of the gbSeqDefinition property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    public void setGBSeqDefinition(String value) {
        this.gbSeqDefinition = value;
    }

    /**
     * Gets the value of the gbSeqPrimaryAccession property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    public String getGBSeqPrimaryAccession() {
        return gbSeqPrimaryAccession;
    }

    /**
     * Sets the value of the gbSeqPrimaryAccession property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    public void setGBSeqPrimaryAccession(String value) {
        this.gbSeqPrimaryAccession = value;
    }

    /**
     * Gets the value of the gbSeqEntryVersion property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    public String getGBSeqEntryVersion() {
        return gbSeqEntryVersion;
    }

    /**
     * Sets the value of the gbSeqEntryVersion property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    public void setGBSeqEntryVersion(String value) {
        this.gbSeqEntryVersion = value;
    }

    /**
     * Gets the value of the gbSeqAccessionVersion property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    public String getGBSeqAccessionVersion() {
        return gbSeqAccessionVersion;
    }

    /**
     * Sets the value of the gbSeqAccessionVersion property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    public void setGBSeqAccessionVersion(String value) {
        this.gbSeqAccessionVersion = value;
    }

    /**
     * Gets the value of the gbSeqOtherSeqids property.
     * 
     * @return
     *     possible object is
     *     {@link GBSeqOtherSeqids }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    public GBSeqOtherSeqids getGBSeqOtherSeqids() {
        return gbSeqOtherSeqids;
    }

    /**
     * Sets the value of the gbSeqOtherSeqids property.
     * 
     * @param value
     *     allowed object is
     *     {@link GBSeqOtherSeqids }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    public void setGBSeqOtherSeqids(GBSeqOtherSeqids value) {
        this.gbSeqOtherSeqids = value;
    }

    /**
     * Gets the value of the gbSeqSecondaryAccessions property.
     * 
     * @return
     *     possible object is
     *     {@link GBSeqSecondaryAccessions }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    public GBSeqSecondaryAccessions getGBSeqSecondaryAccessions() {
        return gbSeqSecondaryAccessions;
    }

    /**
     * Sets the value of the gbSeqSecondaryAccessions property.
     * 
     * @param value
     *     allowed object is
     *     {@link GBSeqSecondaryAccessions }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    public void setGBSeqSecondaryAccessions(GBSeqSecondaryAccessions value) {
        this.gbSeqSecondaryAccessions = value;
    }

    /**
     * Gets the value of the gbSeqProject property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    public String getGBSeqProject() {
        return gbSeqProject;
    }

    /**
     * Sets the value of the gbSeqProject property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    public void setGBSeqProject(String value) {
        this.gbSeqProject = value;
    }

    /**
     * Gets the value of the gbSeqKeywords property.
     * 
     * @return
     *     possible object is
     *     {@link GBSeqKeywords }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    public GBSeqKeywords getGBSeqKeywords() {
        return gbSeqKeywords;
    }

    /**
     * Sets the value of the gbSeqKeywords property.
     * 
     * @param value
     *     allowed object is
     *     {@link GBSeqKeywords }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    public void setGBSeqKeywords(GBSeqKeywords value) {
        this.gbSeqKeywords = value;
    }

    /**
     * Gets the value of the gbSeqSegment property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    public String getGBSeqSegment() {
        return gbSeqSegment;
    }

    /**
     * Sets the value of the gbSeqSegment property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    public void setGBSeqSegment(String value) {
        this.gbSeqSegment = value;
    }

    /**
     * Gets the value of the gbSeqSource property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    public String getGBSeqSource() {
        return gbSeqSource;
    }

    /**
     * Sets the value of the gbSeqSource property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    public void setGBSeqSource(String value) {
        this.gbSeqSource = value;
    }

    /**
     * Gets the value of the gbSeqOrganism property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    public String getGBSeqOrganism() {
        return gbSeqOrganism;
    }

    /**
     * Sets the value of the gbSeqOrganism property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    public void setGBSeqOrganism(String value) {
        this.gbSeqOrganism = value;
    }

    /**
     * Gets the value of the gbSeqTaxonomy property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    public String getGBSeqTaxonomy() {
        return gbSeqTaxonomy;
    }

    /**
     * Sets the value of the gbSeqTaxonomy property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    public void setGBSeqTaxonomy(String value) {
        this.gbSeqTaxonomy = value;
    }

    /**
     * Gets the value of the gbSeqReferences property.
     * 
     * @return
     *     possible object is
     *     {@link GBSeqReferences }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    public GBSeqReferences getGBSeqReferences() {
        return gbSeqReferences;
    }

    /**
     * Sets the value of the gbSeqReferences property.
     * 
     * @param value
     *     allowed object is
     *     {@link GBSeqReferences }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    public void setGBSeqReferences(GBSeqReferences value) {
        this.gbSeqReferences = value;
    }

    /**
     * Gets the value of the gbSeqComment property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    public String getGBSeqComment() {
        return gbSeqComment;
    }

    /**
     * Sets the value of the gbSeqComment property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    public void setGBSeqComment(String value) {
        this.gbSeqComment = value;
    }

    /**
     * Gets the value of the gbSeqCommentSet property.
     * 
     * @return
     *     possible object is
     *     {@link GBSeqCommentSet }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    public GBSeqCommentSet getGBSeqCommentSet() {
        return gbSeqCommentSet;
    }

    /**
     * Sets the value of the gbSeqCommentSet property.
     * 
     * @param value
     *     allowed object is
     *     {@link GBSeqCommentSet }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    public void setGBSeqCommentSet(GBSeqCommentSet value) {
        this.gbSeqCommentSet = value;
    }

    /**
     * Gets the value of the gbSeqStrucComments property.
     * 
     * @return
     *     possible object is
     *     {@link GBSeqStrucComments }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    public GBSeqStrucComments getGBSeqStrucComments() {
        return gbSeqStrucComments;
    }

    /**
     * Sets the value of the gbSeqStrucComments property.
     * 
     * @param value
     *     allowed object is
     *     {@link GBSeqStrucComments }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    public void setGBSeqStrucComments(GBSeqStrucComments value) {
        this.gbSeqStrucComments = value;
    }

    /**
     * Gets the value of the gbSeqPrimary property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    public String getGBSeqPrimary() {
        return gbSeqPrimary;
    }

    /**
     * Sets the value of the gbSeqPrimary property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    public void setGBSeqPrimary(String value) {
        this.gbSeqPrimary = value;
    }

    /**
     * Gets the value of the gbSeqSourceDb property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    public String getGBSeqSourceDb() {
        return gbSeqSourceDb;
    }

    /**
     * Sets the value of the gbSeqSourceDb property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    public void setGBSeqSourceDb(String value) {
        this.gbSeqSourceDb = value;
    }

    /**
     * Gets the value of the gbSeqDatabaseReference property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    public String getGBSeqDatabaseReference() {
        return gbSeqDatabaseReference;
    }

    /**
     * Sets the value of the gbSeqDatabaseReference property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    public void setGBSeqDatabaseReference(String value) {
        this.gbSeqDatabaseReference = value;
    }

    /**
     * Gets the value of the gbSeqFeatureTable property.
     * 
     * @return
     *     possible object is
     *     {@link GBSeqFeatureTable }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    public GBSeqFeatureTable getGBSeqFeatureTable() {
        return gbSeqFeatureTable;
    }

    /**
     * Sets the value of the gbSeqFeatureTable property.
     * 
     * @param value
     *     allowed object is
     *     {@link GBSeqFeatureTable }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    public void setGBSeqFeatureTable(GBSeqFeatureTable value) {
        this.gbSeqFeatureTable = value;
    }

    /**
     * Gets the value of the gbSeqFeatureSet property.
     * 
     * @return
     *     possible object is
     *     {@link GBSeqFeatureSet }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    public GBSeqFeatureSet getGBSeqFeatureSet() {
        return gbSeqFeatureSet;
    }

    /**
     * Sets the value of the gbSeqFeatureSet property.
     * 
     * @param value
     *     allowed object is
     *     {@link GBSeqFeatureSet }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    public void setGBSeqFeatureSet(GBSeqFeatureSet value) {
        this.gbSeqFeatureSet = value;
    }

    /**
     * Gets the value of the gbSeqSequence property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    public String getGBSeqSequence() {
        return gbSeqSequence;
    }

    /**
     * Sets the value of the gbSeqSequence property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    public void setGBSeqSequence(String value) {
        this.gbSeqSequence = value;
    }

    /**
     * Gets the value of the gbSeqContig property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    public String getGBSeqContig() {
        return gbSeqContig;
    }

    /**
     * Sets the value of the gbSeqContig property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    public void setGBSeqContig(String value) {
        this.gbSeqContig = value;
    }

    /**
     * Gets the value of the gbSeqAltSeq property.
     * 
     * @return
     *     possible object is
     *     {@link GBSeqAltSeq }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    public GBSeqAltSeq getGBSeqAltSeq() {
        return gbSeqAltSeq;
    }

    /**
     * Sets the value of the gbSeqAltSeq property.
     * 
     * @param value
     *     allowed object is
     *     {@link GBSeqAltSeq }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    public void setGBSeqAltSeq(GBSeqAltSeq value) {
        this.gbSeqAltSeq = value;
    }

    /**
     * Gets the value of the gbSeqXrefs property.
     * 
     * @return
     *     possible object is
     *     {@link GBSeqXrefs }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    public GBSeqXrefs getGBSeqXrefs() {
        return gbSeqXrefs;
    }

    /**
     * Sets the value of the gbSeqXrefs property.
     * 
     * @param value
     *     allowed object is
     *     {@link GBSeqXrefs }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    public void setGBSeqXrefs(GBSeqXrefs value) {
        this.gbSeqXrefs = value;
    }

}