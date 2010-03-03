/**
 * JBroFuzz 1.9
 *
 * JBroFuzz - A stateless network protocol fuzzer for web applications.
 * 
 * Copyright (C) 2007 - 2010 subere@uncon.org
 *
 * This file is part of JBroFuzz.
 * 
 * JBroFuzz is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * JBroFuzz is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with JBroFuzz.  If not, see <http://www.gnu.org/licenses/>.
 * Alternatively, write to the Free Software Foundation, Inc., 51 
 * Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * 
 * Verbatim copying and distribution of this entire program file is 
 * permitted in any medium without royalty provided this notice 
 * is preserved. 
 * 
 */
package org.owasp.jbrofuzz.fuzz;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.owasp.jbrofuzz.JBroFuzz;

/**
 * Description: The class responsible for making the connection for the purposes
 * of fuzzing through the corresponding socket.
 * 
 * <p>
 * This class gets used to establish each connection being made on a given
 * address, port and for a given request.
 * </p>
 * 
 * @author subere@uncon.org
 * @version 2.0
 * @since 0.1
 */
class SocketConnection implements AbstractConnection {

	// The maximum size for the socket I/O
	// private final static int SEND_BUF_SIZE = 256 * 1024;
	private final static int RECV_BUF_SIZE = 256 * 1024;

	// Singleton SSLSocket factory used with it's factory
	private static SSLSocketFactory mSSLSocketFactory;


	private final transient String message;
	private transient Socket mSocket;
	private transient String reply;	
	private transient int port;

	private transient InputStream inStream;
	private transient OutputStream outStream;

	private int socketTimeout;

	/**
	 * <p>
	 * The constructor for the connection, responsible for creating the
	 * corresponding socket (or SSL socket) and transmitting/receiving data from
	 * the wire.
	 * </p>
	 * 
	 * @param urlString
	 *            The url string from which the protocol (e.g. "https"), the
	 *            host (e.g. www.owasp.org) and the port number will be
	 *            determined.
	 * 
	 * @param message
	 *            of what to put on the wire, once a connection has been
	 *            established.
	 * 
	 * @throws ConnectionException
	 * 
	 * @author subere@uncon.org
	 * @version 2.0
	 * @since 0.1
	 */
	protected SocketConnection(final String protocol, final String host, final int port, final String message)
	throws ConnectionException {

		this.message = message;

		final byte[] recv = new byte[SocketConnection.RECV_BUF_SIZE];

		try {
			if (protocol.equalsIgnoreCase("https")) {

				// Make sure we have a factory for the SSL socket
				if (mSSLSocketFactory == null) {
					mSSLSocketFactory = Connection.getSocketFactory();
				}

				// Handle HTTPS differently then HTTP
				mSocket = mSSLSocketFactory.createSocket(host, port);
				mSocket.setSoTimeout(socketTimeout);

			} else {

				// Handle HTTP differently then HTTPS
				mSocket = new Socket();

				mSocket.connect(new InetSocketAddress(host, port),
						socketTimeout);
			}


			// Set buffers, streams, smile...
			mSocket.setSendBufferSize(this.message.getBytes().length);
			mSocket.setReceiveBufferSize(SocketConnection.RECV_BUF_SIZE);

			inStream = mSocket.getInputStream();
			outStream = mSocket.getOutputStream();

			// Put message on the wire
			outStream.write(this.message.getBytes());

			// Get the timeout value on the Socket
			socketTimeout = JBroFuzz.PREFS.getInt("Socket.Max.Timeout", 7);
			// validate
			if( (socketTimeout < 1) || (socketTimeout > 51) ) {
				socketTimeout = 7;
			}
			// Start timer
			final SocketTimer timer = new SocketTimer(this, socketTimeout * 1000);
			timer.start();

			// Read response, see what you have back
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int got;
			while ((got = inStream.read(recv)) > -1) {
				baos.write(recv, 0, got);
			}

			// If the timer is not reset, close() will be called
			timer.reset();

			baos.close();

			inStream.close();
			outStream.close();

			mSocket.close();

			reply = new String(baos.toByteArray());

		} catch (MalformedURLException e1) {

			reply = "Malformed URL: " + e1.getMessage() + "\n";
			throw new ConnectionException(reply);

		} catch (IOException e3) {

			reply = "An IO Error occured: " + e3.getMessage() + 
			". \n\nThis could also be a Connection Timeout, " +
			"\ntry increasing the value under Preferences ->" +
			" Fuzzing\n";
			throw new ConnectionException(reply);

		} finally {

			IOUtils.closeQuietly(inStream);
			IOUtils.closeQuietly(outStream);

		}

	}

	/* (non-Javadoc)
	 * @see org.owasp.jbrofuzz.fuzz.AbstractConnection#getMessage()
	 */
	public String getMessage() {
		if (message.isEmpty()) {
			return "[JBROFUZZ REQUEST IS BLANK]";
		} else {
			return message;
		}
	}

	/* (non-Javadoc)
	 * @see org.owasp.jbrofuzz.fuzz.AbstractConnection#getPort()
	 */
	public String getPort() {

		if (port == -1) {
			return "[JBROFUZZ PORT IS INVALID]";
		} else {
			return Integer.toString(port);
		}

	}

	/* (non-Javadoc)
	 * @see org.owasp.jbrofuzz.fuzz.AbstractConnection#getReply()
	 */
	public String getReply() {

		if (reply.isEmpty()) {
			return "[JBROFUZZ REPLY IS EMPTY]";
		} else {
			return reply;
		}

	}

	/* (non-Javadoc)
	 * @see org.owasp.jbrofuzz.fuzz.AbstractConnection#getStatus()
	 */
	public String getStatus() {

		char[] code = {'0', '0', '0'};

		try {
			final String out = reply.split(" ")[1].substring(0, 3);

			if (StringUtils.isNumeric(out)) {
				code = out.toCharArray();
			} 

		} catch (Exception exception1) {

			code[0] = '-';
			code[1] = '-';
			code[2] = '-';

		}
		return new String(code);
	}

	/* (non-Javadoc)
	 * @see org.owasp.jbrofuzz.fuzz.AbstractConnection#protocolIsHTTP11(java.lang.String)
	 */
	public final boolean protocolIsHTTP11(final String message) {

		return false;
	}

	/* (non-Javadoc)
	 * @see org.owasp.jbrofuzz.fuzz.AbstractConnection#getPostDataInMessage()
	 */
	public final String getPostDataInMessage() {

		try {

			return message.split("\r\n\r\n")[1];

		} catch (Exception e1) {

			return message;

		}

	}

	/* (non-Javadoc)
	 * @see org.owasp.jbrofuzz.fuzz.AbstractConnection#isResponse100Continue()
	 */
	public boolean isResponse100Continue() {

		return false;

	}


	public void close() {

		IOUtils.closeQuietly(inStream);
		IOUtils.closeQuietly(outStream);

	}

}
