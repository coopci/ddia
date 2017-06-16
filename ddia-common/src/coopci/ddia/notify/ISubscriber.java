package coopci.ddia.notify;

// 给gateway用的工具接口，用来接收各个微服务发出的消息，并转发给用户。
public interface ISubscriber {
	void config(ISubscriberConf conf);
	void init();
	void start();
	void subscribe(long uid);
	void onMessageReceived(long uid, String msg);
	void onBroadcastReceived(long uid, String msg);
}
