package coopci.ddia.results;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import coopci.ddia.Result;

public class ListResult extends Result {
	
	
	public List<KVItem> data = new LinkedList<KVItem>();
	
	public void add(KVItem kvitem) {
		this.data.add(kvitem);
	}
}
