package coopci.ddia.results;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import coopci.ddia.Result;

public class UserInfosResult extends Result {
	
	
	public HashMap<Long, UserInfo> data = new HashMap<Long, UserInfo>();
	
	public UserInfo addEmpty(Long uid) {
		UserInfo ui = new UserInfo();
		data.put(uid, ui);
		return ui;
	}
	
}
