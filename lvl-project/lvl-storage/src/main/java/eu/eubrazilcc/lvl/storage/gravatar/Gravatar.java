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

package eu.eubrazilcc.lvl.storage.gravatar;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.Integer.toHexString;
import static java.security.MessageDigest.getInstance;
import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.annotation.Nullable;

import org.apache.http.client.utils.URIBuilder;

/**
 * Gravatar image and profile.
 * @author Erik Torres <ertorser@upv.es>
 * @see <a href="https://en.gravatar.com/site/implement/">Gravatar: Developer Resources</a>
 * @see <a href="https://en.gravatar.com/site/implement/profiles/json/">Gravatar: JSON Profile Data</a>
 */
public class Gravatar {

	public static final String DEFAULT_URL_BASE = "http://www.gravatar.com/avatar/";
	public static final String SECURE_URL_BASE  = "https://secure.gravatar.com/avatar/";

	private String email;
	private int imageSize;
	private DefaultImage defaultImage;
	private Rating imageRating;
	private String profileCallback;
	private boolean secure;	

	public Gravatar() { }

	public String getEmail() {
		return email;
	}
	public void setEmail(final String email) {
		this.email = email;
	}
	public int getImageSize() {
		return imageSize;
	}
	public void setImageSize(final int imageSize) {
		this.imageSize = imageSize;
	}
	public DefaultImage getDefaultImage() {
		return defaultImage;
	}
	public void setDefaultImage(final DefaultImage defaultImage) {
		this.defaultImage = defaultImage;
	}
	public Rating getImageRating() {
		return imageRating;
	}
	public void setImageRating(final Rating imageRating) {
		this.imageRating = imageRating;
	}
	public String getProfileCallback() {
		return profileCallback;
	}
	public void setProfileCallback(final String profileCallback) {
		this.profileCallback = profileCallback;
	}
	public boolean isSecure() {
		return secure;
	}
	public void setSecure(final boolean secure) {
		this.secure = secure;
	}

	public URL imageUrl() {
		try {
			final URIBuilder uriBuilder = new URIBuilder((secure ? SECURE_URL_BASE : DEFAULT_URL_BASE) + emailHash(email))
			.addParameter("s", Integer.toString(imageSize));
			if (imageRating != Rating.GENERAL_AUDIENCES) {
				uriBuilder.addParameter("r", imageRating.getCode());
			}			
			if (defaultImage != DefaultImage.DEFAULT) {
				uriBuilder.addParameter("d", defaultImage.getCode());
			}
			return uriBuilder.build().toURL();
		} catch (Exception e) {
			throw new IllegalStateException("Failed to get Gravatar image URL", e);
		}
	}

	public URL jsonProfileUrl() {
		try {
			final URIBuilder uriBuilder = new URIBuilder((secure ? SECURE_URL_BASE : DEFAULT_URL_BASE) + emailHash(email) + ".json")
			.addParameter("d", "404");
			if (isNotBlank(profileCallback)) {
				uriBuilder.addParameter("callback", profileCallback);
			}			
			return uriBuilder.build().toURL();
		} catch (Exception e) {
			throw new IllegalStateException("Failed to get Gravatar JSON profile URL", e);
		}		
	}

	private static String emailHash(final String email) {
		checkArgument(isNotBlank(email), "Uninitialized or invalid email");
		return md5Hex(email.toLowerCase().trim());
	}

	private static @Nullable String md5Hex(final String message) {
		try {
			final MessageDigest md = getInstance("MD5");
			return hex(md.digest(message.getBytes("CP1252")));
		} catch (NoSuchAlgorithmException e) {
		} catch (UnsupportedEncodingException e) {
		}
		return null;
	}

	private static String hex(final byte[] array) {
		final StringBuffer sb = new StringBuffer();
		for (int i = 0; i < array.length; i++) {
			sb.append(toHexString((array[i] & 0xFF) | 0x100).substring(1,3));        
		}
		return sb.toString();
	}

	/* Fluent API */

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private final Gravatar gravatar = new Gravatar();

		public Builder() {
			gravatar.setImageSize(80);
			gravatar.setDefaultImage(DefaultImage.MISTERY_MAN);
			gravatar.setImageRating(Rating.GENERAL_AUDIENCES);
			gravatar.setSecure(true);
		}

		public Builder email(final String email) {
			checkArgument(isNotBlank(email), "Uninitialized or invalid email");
			gravatar.setEmail(email);
			return this;
		}

		public Builder imageSize(final int imageSize) {
			checkArgument(imageSize >= 1 && imageSize <= 512, "Invalid image size (between 1 and 512 pixels)");
			gravatar.setImageSize(imageSize);
			return this;
		}

		public Builder defaultImage(final DefaultImage defaultImage) {
			checkArgument(defaultImage != null, "Uninitialized default image");
			gravatar.setDefaultImage(defaultImage);
			return this;
		}

		public Builder imageRating(final Rating imageRating) {
			checkArgument(imageRating != null, "Uninitialized image rating");
			gravatar.setImageRating(imageRating);
			return this;
		}

		public Builder profileCallback(final String profileCallback) {
			gravatar.setProfileCallback(profileCallback);
			return this;
		}

		public Builder secure(final boolean secure) {
			gravatar.setSecure(secure);
			return this;
		}

		public Gravatar build() {
			return gravatar;
		}
	}

	/**
	 * Gravatar default image.
	 * @author Erik Torres <ertorser@upv.es>
	 */
	public enum DefaultImage {

		DEFAULT(""),
		HTTP_404("404"),
		MISTERY_MAN("mm"),
		IDENTICON("identicon"),
		MONSTERID("monsterid"),
		WAVATAR("wavatar"),
		RETRO("retro"),
		BLANK("blank");

		private String code;

		private DefaultImage(String code) {
			this.code = code;
		}
		public String getCode() {
			return code;
		}
	}

	/**
	 * Gravatar rating.
	 * @author Erik Torres <ertorser@upv.es>
	 */
	public enum Rating {

		GENERAL_AUDIENCES("g"),
		PARENTAL_GUIDANCE_SUGGESTED("pg"),
		RESTRICTED("r"),
		XPLICIT("x");

		private String code;

		private Rating(final String code) {
			this.code = code;
		}
		public String getCode() {
			return code;
		}
	}

}