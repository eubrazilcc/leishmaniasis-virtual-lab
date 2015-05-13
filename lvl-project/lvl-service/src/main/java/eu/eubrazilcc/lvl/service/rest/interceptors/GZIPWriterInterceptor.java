/*
 * Copyright 2014-2015 EUBrazilCC (EU‐Brazil Cloud Connect)
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

package eu.eubrazilcc.lvl.service.rest.interceptors;

import static java.lang.Math.min;
import static java.util.zip.Deflater.BEST_COMPRESSION;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;

/**
 * A writer interceptor that enables GZIP compression of the whole entity body. This interceptor works selectively,
 * by compressing the REST response if the annotation is present.
 * @author Erik Torres <ertorser@upv.es>
 * @see <a href="https://jersey.java.net/documentation/latest/user-guide.html#d0e8359">Jersey User Guide - Interceptors</a>
 * @see <a href="http://www.javacodegeeks.com/2014/11/how-to-compress-responses-in-java-rest-api-with-gzip-and-jersey.html">How to compress responses in Java REST API with GZip and Jersey</a>
 */
@Compress
public class GZIPWriterInterceptor implements WriterInterceptor {

	public static final int DEFAULT_COMPRESSION = 5;

	@Override
	public void aroundWriteTo(final WriterInterceptorContext context) throws IOException, WebApplicationException {
		// add content header to enable client decompression
		final MultivaluedMap<String, Object> headers = context.getHeaders();
		headers.add("Content-Encoding", "gzip");
		// compress the content of the entity body
		final OutputStream outputStream = context.getOutputStream();
		context.setOutputStream(new GZIPOutputStream(outputStream) {
			{
				this.def.setLevel(min(BEST_COMPRESSION, DEFAULT_COMPRESSION));
			} 
		});
		context.proceed();
	}

}