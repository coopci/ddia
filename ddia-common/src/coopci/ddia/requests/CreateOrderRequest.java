package coopci.ddia.requests;

public class CreateOrderRequest extends Request {
	public Long uid = 0L;
	public String payChannel = "";
	public String appid = "";
	public String apptranxid = "";
	public Double totalAmount = 0.0; // 单位是分。
	public String desc = "";
}
