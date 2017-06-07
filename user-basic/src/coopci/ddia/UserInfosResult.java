package coopci.ddia;

import java.util.HashMap;

public class UserInfosResult extends Result {
	
	static public class UserInfo extends HashMap<String, Object>{}
	public HashMap<Long, UserInfo> data = new HashMap<Long, UserInfo>();
	
	public UserInfo addEmpty(Long uid) {
		UserInfo ui = new UserInfo();
		data.put(uid, ui);
		return ui;
	}
	
}
