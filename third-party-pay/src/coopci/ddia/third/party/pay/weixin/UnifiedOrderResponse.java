package coopci.ddia.third.party.pay.weixin;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

// 微信给的回复�?
@JacksonXmlRootElement(localName="xml")
public class UnifiedOrderResponse extends WeixinResponse {
	
	
	public String trade_type = "";
	public String prepay_id = "";
	
	
}
