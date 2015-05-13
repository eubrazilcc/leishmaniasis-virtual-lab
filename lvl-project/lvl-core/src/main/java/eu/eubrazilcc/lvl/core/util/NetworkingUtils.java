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

package eu.eubrazilcc.lvl.core.util;

import static com.google.common.base.Preconditions.checkArgument;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

import com.google.common.collect.Range;
import static com.google.common.collect.Range.closedOpen;

/**
 * Utilities to discover networking configuration.
 * @author Erik Torres <ertorser@upv.es>
 * @see <a href="https://tools.ietf.org/html/rfc6335">RFC 6335</a>
 */
public final class NetworkingUtils {

	private static final Logger LOGGER = getLogger(NetworkingUtils.class);
	
	/**
	 * The usable user port number limits. Set at 1024 <= x < 49151 to avoid returning privileged port numbers.
	 */	
	public static final Range<Integer> USER_PORTS = closedOpen(1024, 49151);
	
	public static final Range<Integer> USER_PORTS_16_BITS = closedOpen(1024, 65535);

	/**
	 * Gets the first public IP address of the host. If no public address are found, one of the private
	 * IPs are randomly selected. Otherwise, it returns {@code localhost}.
	 * @return the first public IP address of the host.
	 */
	public static final String getInet4Address() {
		String inet4Address = null;
		final List<String> localAddresses = new ArrayList<String>();
		try {
			final Enumeration<NetworkInterface> networks = NetworkInterface.getNetworkInterfaces();
			if (networks != null) {
				final List<NetworkInterface> ifs = Collections.list(networks);
				for (int i = 0; i < ifs.size() && inet4Address == null; i++) {
					final Enumeration<InetAddress> inetAddresses = ifs.get(i).getInetAddresses();
					if (inetAddresses != null) {
						final List<InetAddress> addresses = Collections.list(inetAddresses);
						for (int j = 0; j < addresses.size() && inet4Address == null; j++) {
							final InetAddress address = addresses.get(j);
							if (address instanceof Inet4Address && !address.isAnyLocalAddress() 
									&& !address.isLinkLocalAddress() && !address.isLoopbackAddress()
									&& StringUtils.isNotBlank(address.getHostAddress())) {							
								final String hostAddress = address.getHostAddress().trim();
								if (!hostAddress.startsWith("10.") && !hostAddress.startsWith("172.16.") 
										&& !hostAddress.startsWith("192.168.")) {
									inet4Address = hostAddress;
								} else {
									localAddresses.add(hostAddress);
								}
								LOGGER.trace("IP found - Name: " + address.getHostName() + ", Addr: " + hostAddress);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			LOGGER.warn("Failed to discover public IP address for this host", e);
		}
		return (StringUtils.isNotBlank(inet4Address) ? inet4Address : (!localAddresses.isEmpty() 
				? localAddresses.get(new Random().nextInt(localAddresses.size())) : "localhost")).trim();
	}

	public static final boolean isPortAvailable(final int port) throws IllegalArgumentException {
		checkArgument(USER_PORTS_16_BITS.contains(port), "Invalid start port: " + port);		
		try (final ServerSocket ss = new ServerSocket(port); final DatagramSocket ds = new DatagramSocket(port)) {
			ss.setReuseAddress(true);		
			ds.setReuseAddress(true);
			return true;
		} catch (IOException ignore) { }
		return false;
	}

}