package coopci.ddia.results;

import java.util.HashMap;

import coopci.ddia.Result;

public class DictResult extends Result {

	
	public HashMap<String, Object> data = new HashMap<String, Object>();
	
	public void put(String k, Object v) {
		this.data.put(k, v);
	}
}
