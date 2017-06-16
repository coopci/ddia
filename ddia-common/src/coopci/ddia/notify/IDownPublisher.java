package coopci.ddia.notify;

public interface IDownPublisher {

	 void sendToUid(long uid, String msg);
	
	 void broadcast(String msg);
	
}
