package coopci.ddia.util;

import java.util.HashMap;
import java.util.HashSet;

import org.glassfish.grizzly.http.server.Request;

public class Funcs {

	public static String toHexString(byte[] bytes) {
	    StringBuilder hexString = new StringBuilder();

	    for (int i = 0; i < bytes.length; i++) {
	        String hex = Integer.toHexString(0xFF & bytes[i]);
	        if (hex.length() == 1) {
	            hexString.append('0');
	        }
	        hexString.append(hex);
	    }

	    return hexString.toString();
	}
	
	
	
	public static HashSet<String> csvToHashSet(String csv) {
		HashSet<String> ret = new HashSet<String>();
		if (csv != null) {
			for (String v : csv.split(",")) {
				ret.add(v);
			}
		}
		
		return ret;
	}
	
	public static HashMap<String, Object> parametersToHashMap(Request request, HashSet<String> blockFields ) {
		HashMap<String, Object> ret = new HashMap<String, Object>();
		for (String n:request.getParameterNames()) {
			if (blockFields!= null && blockFields.contains(n))
				continue;
			ret.put(n, request.getAttribute(n));
		}
		
		return ret;
	}
	
}
