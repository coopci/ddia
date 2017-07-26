package coopci.ddia.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import org.bson.Document;
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
	
	public static int parseInt(String v, int defaultValue) {
		if (v==null)
			return defaultValue;
		return Integer.parseInt(v);
	}
	
	public static HashSet<String> csvToHashSet(String csv) {
		HashSet<String> ret = new HashSet<String>();
		if (csv != null) {
			for (String v : csv.split(",")) {
				if (v!=null && v.length() > 0)
				ret.add(v);
			}
		}
		
		return ret;
	}
	public static HashSet<String> trueStrings = new HashSet<String>();
	public static HashSet<String> falsyStrings = new HashSet<String>();
	
	static {
		trueStrings.add("1");
		trueStrings.add("on");
		trueStrings.add("true");
		trueStrings.add("yes");
		
		falsyStrings.add("0");
		falsyStrings.add("off");
		falsyStrings.add("false");
		falsyStrings.add("no");
	}
	public static boolean parseBoolean (String v, boolean defaultValue) {
		if (v == null)
			return defaultValue;
		if (trueStrings.contains(v)) {
			return true;
		}
		if (falsyStrings.contains(v)) {
			return false;
		}
		return defaultValue;
		
	}
	public static HashMap<String, Object> parametersToHashMap(Request request, HashSet<String> blockFields ) {
		HashMap<String, Object> ret = new HashMap<String, Object>();
		for (String n:request.getParameterNames()) {
			if (blockFields!= null && blockFields.contains(n))
				continue;
			ret.put(n, request.getParameter(n));
		}
		
		return ret;
	}
	
	public static boolean isEmpty(String s) {
		if (s == null)
			return true;
		if (s.length() == 0) {
			return true;
		}
		return false;
	}
//	
//	public static void put(HashMap<String, Long> dest, Document from) {
//		if (dest == null)
//			return;
//		if (from == null)
//			return;
//		for (Entry<String, Object> entry: from.entrySet()) {
//			Object v = entry.getValue();
//			if (v instanceof Long) {
//				dest.put(entry.getKey(), (Long)v);
//			}
//		}
//	}
//	

	public static void put(HashMap<String, Object> dest, Document from) {
		if (dest == null)
			return;
		if (from == null)
			return;
		for (Entry<String, Object> entry: from.entrySet()) {
			dest.put(entry.getKey(), entry.getValue());
		}
	}
	
	public static long toLong(Object o, long defaultValue) {
		if (o instanceof Integer) {
			return ((Integer)o).longValue();
		} else if(o instanceof Long) {
			return ((Long)o).longValue();
		} else if (o instanceof String) {
			return Long.parseLong((String)o);
		} 
		
		return defaultValue;
	}
}
