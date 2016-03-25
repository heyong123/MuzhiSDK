package mobi.zty.sdk.game.bean;

public class MmpayOrderInfo {
	 private int ret;
	 private String order_no;

	    public MmpayOrderInfo() {
	    }
	    
	    public int getRet() {
	        return ret;
	    }

	    public void setRet(int ret) {
	        this.ret = ret;
	    }

	    public String getOrderNo() {
	        return order_no;
	    }
	    public void setOrderNo(String order_no) {
	        this.order_no = order_no;
	    }

	    @Override
	    public String toString() {
	        return "MmpayOrderInfo{" +
	                "ret=" + ret +
	                ", order_no='" + order_no + '\'' +
	                '}';
	    }
}
