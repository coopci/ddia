package coopci.ddia.results;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class KVItem extends HashMap<String, Object> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2714045170815504471L;

	public void put(String k, Date v) {
		if (v == null) {
			// this.put(k, null);
			return;
		}
		SimpleDateFormat dt = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss"); 
		String s = dt.format(v);
		this.put(k, s);
	}
	
}