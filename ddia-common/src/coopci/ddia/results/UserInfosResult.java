package coopci.ddia.results;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import coopci.ddia.Result;

public class UserInfosResult extends Result {
	
	
	public HashMap<Long, KVItem> data = new HashMap<Long, KVItem>();
	
	public KVItem addEmpty(Long uid) {
		KVItem ui = new KVItem();
		data.put(uid, ui);
		return ui;
	}
	
}
