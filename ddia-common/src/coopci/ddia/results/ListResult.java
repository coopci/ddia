package coopci.ddia.results;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import coopci.ddia.Result;

public class ListResult extends Result {
	
	
	public List<UserInfo> data = new LinkedList<UserInfo>();
	
	public void add(UserInfo ui) {
		this.data.add(ui);
	}
}
