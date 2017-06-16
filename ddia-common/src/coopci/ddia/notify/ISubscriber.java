package coopci.ddia.notify;

import java.io.IOException;

// 给gateway用的工具接口，用来接收各个微服务发出的消息，并转发给用户。
public interface ISubscriber {
	void config(ISubscriberConf conf);
	void init();
	void start() throws Exception;
	void subscribe(long uid)  throws Exception;
	void onMessageReceived(long uid, byte[] msg);
	void onBroadcastReceived(byte[] msg);
	void setDownPublisher(IDownPublisher dp);
	IDownPublisher getDownPublisher();
}
