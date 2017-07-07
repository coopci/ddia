package coopci.ddia.third.party.pay.weixin;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;



//微信给的回复:
//https://pay.weixin.qq.com/wiki/doc/api/app/app.php?chapter=9_2&index=4
@JacksonXmlRootElement(localName="xml")
public class OrderQueryResponse extends WeixinResponse {
	
	public String openid="";
	public String is_subscribe="";
	public String trade_type="";
	public String trade_state ="";
	public String bank_type ="";
	public int total_fee =0;
	public String fee_type ="";
	public int cash_fee 	=0;
	public String cash_fee_type ="";
	public int coupon_fee=0;
	public int coupon_count =0;
	
	// coupon_batch_id_$n 
	// coupon_id_$n
	// coupon_fee_$n 
	
	public String transaction_id="";
	public String out_trade_no ="";
	public String attach ="";
	public String time_end ="";
	public String trade_state_desc="";
	
}
