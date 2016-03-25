package mobi.zty.sdk.game.bean;

public class WeiXinOrderInfo {
	 private String orderInfo;
	 

	    public WeiXinOrderInfo() {
	    }

	    public String getOrderInfo() {
	        return orderInfo;
	    }

	    public void setOrderInfo(String orderInfo) {
	        this.orderInfo = orderInfo;
	    }

	    @Override
	    public String toString() {
	        return "AlipayOrderInfo{" +
	                "orderInfo='" + orderInfo + '\'' +
	                '}';
	    }
}
