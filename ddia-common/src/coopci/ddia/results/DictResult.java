package coopci.ddia.results;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import coopci.ddia.Result;

public class DictResult extends Result {

	
	public HashMap<String, Object> data = new HashMap<String, Object>();
	
	public void put(String k, Object v) {
		
		if (v instanceof Date) {
			SimpleDateFormat dt = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss"); 
			String s = dt.format(v);
			this.data.put(k, s);
		} else { 
			this.data.put(k, v);
		}
	}
}
