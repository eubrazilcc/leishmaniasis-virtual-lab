/*
 * Copyright 2014 EUBrazilCC (EU‐Brazil Cloud Connect)
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by 
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 *   http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 * 
 * This product combines work with different licenses. See the "NOTICE" text
 * file for details on the various modules and licenses.
 * The "NOTICE" text file is part of the distribution. Any derivative works
 * that you distribute must include a readable copy of the "NOTICE" text file.
 */

package eu.eubrazilcc.lvl.core;

import static com.google.common.base.MoreObjects.toStringHelper;
import static org.apache.commons.lang.StringUtils.trimToNull;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

/**
 * Base class from which to extend to create objects stored in the file-system and referred in the application's database.
 * The attributes are derived from the MongoDB files collection, which is part of the GridFS specification. The additional
 * property {@link #outfile} can used to store a copy of the object in a file. 
 * @author Erik Torres <ertorser@upv.es>
 * @see <a href="http://docs.mongodb.org/manual/reference/gridfs/#gridfs-files-collection">GridFS Reference: The files Collection</a>
 */
@JsonIgnoreProperties({ "outfile" })
@JsonTypeInfo(property = "type_", use = Id.NAME)
@JsonSubTypes({ @Type(Dataset.class) })
public class BaseFile {	

	private String id;
	private long length;
	private long chunkSize;
	private Date uploadDate;
	private String md5;
	private String filename;
	private String contentType;
	private List<String> aliases;
	private Metadata metadata;

	/**
	 * Additional property (not part of the GridFS specification) that can be used to store a copy of the object in a file.
	 */
	private File outfile;

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public long getLength() {
		return length;
	}

	public void setLength(final long length) {
		this.length = length;
	}

	public long getChunkSize() {
		return chunkSize;
	}

	public void setChunkSize(final long chunkSize) {
		this.chunkSize = chunkSize;
	}

	public Date getUploadDate() {
		return uploadDate;
	}

	public void setUploadDate(final Date uploadDate) {
		this.uploadDate = uploadDate;
	}

	public String getMd5() {
		return md5;
	}

	public void setMd5(final String md5) {
		this.md5 = md5;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(final String filename) {
		this.filename = trimToNull(filename);		
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public List<String> getAliases() {
		return aliases;
	}

	public void setAliases(final List<String> aliases) {
		this.aliases = aliases;
	}

	public Metadata getMetadata() {
		return metadata;
	}

	public void setMetadata(final Metadata metadata) {
		this.metadata = metadata;
	}

	public File getOutfile() {
		return outfile;
	}

	public void setOutfile(final File outfile) {
		this.outfile = outfile;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof BaseFile)) {
			return false;
		}
		final BaseFile other = BaseFile.class.cast(obj);
		return Objects.equals(id, other.id)
				&& Objects.equals(length, other.length)
				&& Objects.equals(chunkSize, other.chunkSize)				
				&& Objects.equals(uploadDate, other.uploadDate)
				&& Objects.equals(md5, other.md5)
				&& Objects.equals(filename, other.filename)
				&& Objects.equals(contentType, other.contentType)
				&& Objects.equals(aliases, other.aliases)
				&& Objects.equals(metadata, other.metadata);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, length, chunkSize, uploadDate, md5, filename, contentType, aliases, metadata);
	}

	@Override
	public String toString() {
		return toStringHelper(this)
				.add("id", id)
				.add("length", length)
				.add("chunkSize", chunkSize)
				.add("uploadDate", uploadDate)
				.add("md5", md5)
				.add("filename", filename)
				.add("contentType", contentType)
				.add("aliases", aliases)
				.add("metadata", metadata)
				.add("outfile", outfile)
				.toString();
	}

}