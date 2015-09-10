package org.geworkbenchweb.plugins.cnkb;

/**  
 * The exception of HttpURLConnection.HTTP_UNAUTHORIZED
 */
public class UnAuthenticatedException extends Exception {
	private static final long serialVersionUID = -6825529221374869868L;
	
	public UnAuthenticatedException(String message) {
		super(message);
	}
}