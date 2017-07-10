package coopci.ddia.notify.rabbitmq;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

import coopci.ddia.notify.IDownPublisher;
import coopci.ddia.notify.IPublisher;
import coopci.ddia.notify.IPublisherConf;
import coopci.ddia.notify.ISubscriber;
import coopci.ddia.notify.ISubscriberConf;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.AMQP;

public class RabbitmqPublisher implements IPublisher {

	@Override
	public void config(IPublisherConf conf) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}
	
	String exchangeName = "user.notify";
	String queueName = "";
	Channel channel = null;
	
	String username = "ddia";
	String passwd = "ddia";
	@Override
	public void start() throws KeyManagementException, NoSuchAlgorithmException, URISyntaxException, IOException, TimeoutException {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setUri("amqp://localhost:5672/");
		factory.setVirtualHost("/");
		factory.setUsername(username);
		factory.setPassword(passwd);
		Connection conn = factory.newConnection();
		channel = conn.createChannel();
		channel.exchangeDeclare(exchangeName, "topic", true);
		queueName = channel.queueDeclare().getQueue();
		
		
	}
	public boolean isOpen() {
		return this.channel.isOpen();
	}
	
	// 发给单个用户。
	public void publish(long uid, String msg) throws Exception {
		String routingKey = "uid=" + uid;
		byte[] messageBodyBytes = msg.getBytes();
		this.channel.basicPublish(this.exchangeName, routingKey, null, messageBodyBytes);
		
	}
	// 广播给所有用户。
	public void broadcast(String msg) throws IOException {
		String routingKey = "broadcast";
		byte[] messageBodyBytes = msg.getBytes();
		this.channel.basicPublish(this.exchangeName, routingKey, null, messageBodyBytes);
	}

	public static void main(String[] args) throws Exception {
		RabbitmqPublisher pub = new RabbitmqPublisher();
		pub.start();
		pub.publish(6, "hahaha");
		return;
	}
}
