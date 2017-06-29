package coopci.ddia.user.basic;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.taobao.api.ApiException;
import com.taobao.api.DefaultTaobaoClient;
import com.taobao.api.TaobaoClient;
import com.taobao.api.request.AlibabaAliqinFcSmsNumSendRequest;
import com.taobao.api.response.AlibabaAliqinFcSmsNumSendResponse;

public class Utils {

	public static String ALIDAYU_APPKEY = "";
	public static String ALIDAYU_SECRET = "";
	// https://api.alidayu.com/doc2/apiDetail?spm=a3142.7629140.1999205496.20.VUpPi5&apiId=25450
	public static String ALIDAYU_URL = "https://eco.taobao.com/router/rest";
	
	static public AlibabaAliqinFcSmsNumSendResponse sendVcodeViaAlidayuy(String phone, String vcode) throws ApiException {
		TaobaoClient client = new DefaultTaobaoClient(ALIDAYU_URL, ALIDAYU_APPKEY, ALIDAYU_SECRET);
		AlibabaAliqinFcSmsNumSendRequest req = new AlibabaAliqinFcSmsNumSendRequest();
		req.setExtend(vcode);
		req.setSmsType("normal");
		req.setSmsFreeSignName("来嗨");				// 这里要换。
		req.setSmsParamString("{\"vcode\":\""+ vcode + "\"}");
		req.setRecNum(phone);
		req.setSmsTemplateCode("SMS_43180006");  // 这里要换。
		AlibabaAliqinFcSmsNumSendResponse rsp = client.execute(req);
		
		System.out.println(rsp.getBody());
		System.out.println("rsp.getSubMsg(): " + rsp.getSubMsg());
		
		return rsp;
	}
	
	
	
	
	
	public static String mailSenderUsername = "cooper@xinwaihui.com";
	public static String mailSenderPassword = "xwh123QWE";
	public static String smtpHost = "smtp.exmail.qq.com";
	
	
	static public void sendVcodeViaEmail(String to, String vcode, String subjectTemplate, String bodyTemplate) throws ApiException {
		String from = mailSenderUsername;
		Properties properties = System.getProperties();
		properties.setProperty("mail.smtp.host", smtpHost);
		properties.put("mail.transport.protocol", "smtps");
		
		properties.put("mail.smtp.starttls.enable", "true");
		properties.put("mail.smtp.socketFactory.port", 465);
		properties.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
		properties.put("mail.smtp.auth", "true");
		
		Session session = Session.getDefaultInstance(properties,
				new javax.mail.Authenticator() {
		            protected PasswordAuthentication getPasswordAuthentication() {
		                return new PasswordAuthentication(mailSenderUsername, mailSenderPassword);
		            }
        });

		try {
			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress(from));
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(
					to));
			message.setSubject(subjectTemplate.replace("{{vcode}}", vcode));
			message.setText(bodyTemplate.replace("{{vcode}}", vcode));

			Transport.send(message);
			// System.out.println("message sent successfully....");

		} catch (MessagingException mex) {
			mex.printStackTrace();
		}
	}
	
	
	
	public static void main(String[] argv) throws ApiException {
		
		String subjectTemplate = "signin activation.";
		String bodyTemplate = "Your verification code is {{vcode}}.";
		sendVcodeViaEmail("bo.coopci@gmail.com", "64284", subjectTemplate, bodyTemplate);
	}
}
