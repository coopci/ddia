package coopci.ddia.results;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import coopci.ddia.Result;

public class ListResult extends Result {
	
	// 这个data里面只有本次返回的实际数据。
	public List<KVItem> data = new LinkedList<KVItem>();
	
	// 如过total>0， 则表示"数据库"里一共有total条符合条件的数据，data里面是本"页"的数据。
	// 本"页"的筛选条件 和 查询的筛选条件 的语义由各个API自己定。
	public int total = -1;
	
	public void add(KVItem kvitem) {
		this.data.add(kvitem);
	}
}
