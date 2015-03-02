//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.03.02 at 08:40:23 AM CET 
//


package eu.eubrazilcc.lvl.core.xml.ncbi.gb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
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
public class GBSeq {

    @XmlElement(name = "GBSeq_locus")
    protected String gbSeqLocus;
    @XmlElement(name = "GBSeq_length", required = true)
    protected String gbSeqLength;
    @XmlElement(name = "GBSeq_strandedness")
    protected String gbSeqStrandedness;
    @XmlElement(name = "GBSeq_moltype", required = true)
    protected String gbSeqMoltype;
    @XmlElement(name = "GBSeq_topology")
    protected String gbSeqTopology;
    @XmlElement(name = "GBSeq_division")
    protected String gbSeqDivision;
    @XmlElement(name = "GBSeq_update-date")
    protected String gbSeqUpdateDate;
    @XmlElement(name = "GBSeq_create-date")
    protected String gbSeqCreateDate;
    @XmlElement(name = "GBSeq_update-release")
    protected String gbSeqUpdateRelease;
    @XmlElement(name = "GBSeq_create-release")
    protected String gbSeqCreateRelease;
    @XmlElement(name = "GBSeq_definition")
    protected String gbSeqDefinition;
    @XmlElement(name = "GBSeq_primary-accession")
    protected String gbSeqPrimaryAccession;
    @XmlElement(name = "GBSeq_entry-version")
    protected String gbSeqEntryVersion;
    @XmlElement(name = "GBSeq_accession-version")
    protected String gbSeqAccessionVersion;
    @XmlElement(name = "GBSeq_other-seqids")
    protected GBSeqOtherSeqids gbSeqOtherSeqids;
    @XmlElement(name = "GBSeq_secondary-accessions")
    protected GBSeqSecondaryAccessions gbSeqSecondaryAccessions;
    @XmlElement(name = "GBSeq_project")
    protected String gbSeqProject;
    @XmlElement(name = "GBSeq_keywords")
    protected GBSeqKeywords gbSeqKeywords;
    @XmlElement(name = "GBSeq_segment")
    protected String gbSeqSegment;
    @XmlElement(name = "GBSeq_source")
    protected String gbSeqSource;
    @XmlElement(name = "GBSeq_organism")
    protected String gbSeqOrganism;
    @XmlElement(name = "GBSeq_taxonomy")
    protected String gbSeqTaxonomy;
    @XmlElement(name = "GBSeq_references")
    protected GBSeqReferences gbSeqReferences;
    @XmlElement(name = "GBSeq_comment")
    protected String gbSeqComment;
    @XmlElement(name = "GBSeq_comment-set")
    protected GBSeqCommentSet gbSeqCommentSet;
    @XmlElement(name = "GBSeq_struc-comments")
    protected GBSeqStrucComments gbSeqStrucComments;
    @XmlElement(name = "GBSeq_primary")
    protected String gbSeqPrimary;
    @XmlElement(name = "GBSeq_source-db")
    protected String gbSeqSourceDb;
    @XmlElement(name = "GBSeq_database-reference")
    protected String gbSeqDatabaseReference;
    @XmlElement(name = "GBSeq_feature-table")
    protected GBSeqFeatureTable gbSeqFeatureTable;
    @XmlElement(name = "GBSeq_feature-set")
    protected GBSeqFeatureSet gbSeqFeatureSet;
    @XmlElement(name = "GBSeq_sequence")
    protected String gbSeqSequence;
    @XmlElement(name = "GBSeq_contig")
    protected String gbSeqContig;
    @XmlElement(name = "GBSeq_alt-seq")
    protected GBSeqAltSeq gbSeqAltSeq;
    @XmlElement(name = "GBSeq_xrefs")
    protected GBSeqXrefs gbSeqXrefs;

    /**
     * Gets the value of the gbSeqLocus property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
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
    public void setGBSeqXrefs(GBSeqXrefs value) {
        this.gbSeqXrefs = value;
    }

    public GBSeq withGBSeqLocus(String value) {
        setGBSeqLocus(value);
        return this;
    }

    public GBSeq withGBSeqLength(String value) {
        setGBSeqLength(value);
        return this;
    }

    public GBSeq withGBSeqStrandedness(String value) {
        setGBSeqStrandedness(value);
        return this;
    }

    public GBSeq withGBSeqMoltype(String value) {
        setGBSeqMoltype(value);
        return this;
    }

    public GBSeq withGBSeqTopology(String value) {
        setGBSeqTopology(value);
        return this;
    }

    public GBSeq withGBSeqDivision(String value) {
        setGBSeqDivision(value);
        return this;
    }

    public GBSeq withGBSeqUpdateDate(String value) {
        setGBSeqUpdateDate(value);
        return this;
    }

    public GBSeq withGBSeqCreateDate(String value) {
        setGBSeqCreateDate(value);
        return this;
    }

    public GBSeq withGBSeqUpdateRelease(String value) {
        setGBSeqUpdateRelease(value);
        return this;
    }

    public GBSeq withGBSeqCreateRelease(String value) {
        setGBSeqCreateRelease(value);
        return this;
    }

    public GBSeq withGBSeqDefinition(String value) {
        setGBSeqDefinition(value);
        return this;
    }

    public GBSeq withGBSeqPrimaryAccession(String value) {
        setGBSeqPrimaryAccession(value);
        return this;
    }

    public GBSeq withGBSeqEntryVersion(String value) {
        setGBSeqEntryVersion(value);
        return this;
    }

    public GBSeq withGBSeqAccessionVersion(String value) {
        setGBSeqAccessionVersion(value);
        return this;
    }

    public GBSeq withGBSeqOtherSeqids(GBSeqOtherSeqids value) {
        setGBSeqOtherSeqids(value);
        return this;
    }

    public GBSeq withGBSeqSecondaryAccessions(GBSeqSecondaryAccessions value) {
        setGBSeqSecondaryAccessions(value);
        return this;
    }

    public GBSeq withGBSeqProject(String value) {
        setGBSeqProject(value);
        return this;
    }

    public GBSeq withGBSeqKeywords(GBSeqKeywords value) {
        setGBSeqKeywords(value);
        return this;
    }

    public GBSeq withGBSeqSegment(String value) {
        setGBSeqSegment(value);
        return this;
    }

    public GBSeq withGBSeqSource(String value) {
        setGBSeqSource(value);
        return this;
    }

    public GBSeq withGBSeqOrganism(String value) {
        setGBSeqOrganism(value);
        return this;
    }

    public GBSeq withGBSeqTaxonomy(String value) {
        setGBSeqTaxonomy(value);
        return this;
    }

    public GBSeq withGBSeqReferences(GBSeqReferences value) {
        setGBSeqReferences(value);
        return this;
    }

    public GBSeq withGBSeqComment(String value) {
        setGBSeqComment(value);
        return this;
    }

    public GBSeq withGBSeqCommentSet(GBSeqCommentSet value) {
        setGBSeqCommentSet(value);
        return this;
    }

    public GBSeq withGBSeqStrucComments(GBSeqStrucComments value) {
        setGBSeqStrucComments(value);
        return this;
    }

    public GBSeq withGBSeqPrimary(String value) {
        setGBSeqPrimary(value);
        return this;
    }

    public GBSeq withGBSeqSourceDb(String value) {
        setGBSeqSourceDb(value);
        return this;
    }

    public GBSeq withGBSeqDatabaseReference(String value) {
        setGBSeqDatabaseReference(value);
        return this;
    }

    public GBSeq withGBSeqFeatureTable(GBSeqFeatureTable value) {
        setGBSeqFeatureTable(value);
        return this;
    }

    public GBSeq withGBSeqFeatureSet(GBSeqFeatureSet value) {
        setGBSeqFeatureSet(value);
        return this;
    }

    public GBSeq withGBSeqSequence(String value) {
        setGBSeqSequence(value);
        return this;
    }

    public GBSeq withGBSeqContig(String value) {
        setGBSeqContig(value);
        return this;
    }

    public GBSeq withGBSeqAltSeq(GBSeqAltSeq value) {
        setGBSeqAltSeq(value);
        return this;
    }

    public GBSeq withGBSeqXrefs(GBSeqXrefs value) {
        setGBSeqXrefs(value);
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
