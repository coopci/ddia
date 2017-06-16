package coopci.ddia.notify;

import java.io.IOException;

// 给内部的微服务用的工具接口，当微服务需要用户发消息时，调用publish或broadcast就行了。
public interface IPublisher {
	void config(IPublisherConf conf);
	void init();
	void start() throws Exception;
	
	// 发给单个用户。
	void publish(long uid, String msg) throws Exception;
	// 广播给所有用户。
	void broadcast(String msg) throws Exception;
	boolean isOpen();
	
}
