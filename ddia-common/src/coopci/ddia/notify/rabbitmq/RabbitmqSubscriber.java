package coopci.ddia.notify.rabbitmq;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

import coopci.ddia.notify.IDownPublisher;
import coopci.ddia.notify.ISubscriber;
import coopci.ddia.notify.ISubscriberConf;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.AMQP;

public class RabbitmqSubscriber implements ISubscriber {

	@Override
	public void config(ISubscriberConf conf) {
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
		
		boolean autoAck = true;
		channel.basicConsume(queueName, autoAck, "myConsumerTag",
			     new DefaultConsumer(channel) {
			         @Override
			         public void handleDelivery(String consumerTag,
			                                    Envelope envelope,
			                                    AMQP.BasicProperties properties,
			                                    byte[] body)
			             throws IOException
			         {
			             String routingKey = envelope.getRoutingKey();
			             String contentType = properties.getContentType();
			             long deliveryTag = envelope.getDeliveryTag();
			             if (routingKey.equals("broadcast")) {
			            	 onBroadcastReceived(body);
			             } else if (routingKey.startsWith("uid=")) {
			            	 long uid = Long.parseLong(routingKey.replace("uid=", ""));
			            	 onMessageReceived(uid, body);
			             }
			         }
			     });
		channel.queueBind(this.queueName, exchangeName, "broadcast");
		
	}

	@Override
	public void subscribe(long uid) throws IOException {
		if (this.channel == null)
			return;
		String routingKey = "uid=" + uid;
		channel.queueBind(this.queueName, exchangeName, routingKey);
	}

	@Override
	public void onMessageReceived(long uid, byte[] msg) {
		StringBuilder sb = new StringBuilder();
		sb.append("onMessageReceived(");
		sb.append(uid);
		sb.append(", \"");
		sb.append(new String(msg));
		sb.append("\")");
		System.out.println(sb.toString());
		
		if (this.getDownPublisher()!=null) {
			this.getDownPublisher().sendToUid(uid, new String(msg));
		}
	}

	@Override
	public void onBroadcastReceived(byte[] msg) {
		StringBuilder sb = new StringBuilder();
		sb.append("onBroadcastReceived(\"");
		sb.append(new String(msg));
		sb.append("\")");
		System.out.println(sb.toString());
		
		if (this.getDownPublisher()!=null) {
			this.getDownPublisher().broadcast(new String(msg));
		}
	}

	

	IDownPublisher downpublisher = null;
	@Override
	public void setDownPublisher(IDownPublisher dp) {
		downpublisher = dp;
	}

	@Override
	public IDownPublisher getDownPublisher() {
		return downpublisher;
	}
	

	public static void main(String[] args) throws KeyManagementException, NoSuchAlgorithmException, URISyntaxException, IOException, TimeoutException {
		RabbitmqSubscriber sub = new RabbitmqSubscriber();
		sub.start();
		return;
	}
}
