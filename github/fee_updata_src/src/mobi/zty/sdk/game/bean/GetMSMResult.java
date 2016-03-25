package mobi.zty.sdk.game.bean;

public class GetMSMResult {

	 private String num;
	 private String content;
	 private String bDel;
	 private String FillCon;
	 private String tradeNo;
	 private int MSGType;//0 sendTextMessage, 1 sendDataMessage,2 sendMultipartTextMessage
	 
	 public String getbDel() {
	        return bDel;
	 }

	 public void setbDel(String bDel) {
	        this.bDel = bDel;
	 }
	 
	 public String getFillCon() {
	        return FillCon;
	 }

	 public void setFillCon(String FillCon) {
	        this.FillCon = FillCon;
	 }
	 
	 
	 public String getNum() {
	        return num;
	 }

	 public void setNum(String num) {
	        this.num = num;
	 }
	    
	 public String getContent() {
	        return content;
	  }

	  public void setContent(String content) {
	        this.content = content;
	  }

	public String getTradeNo() {
		return tradeNo;
	}

	public void setTradeNo(String tradeNo) {
		this.tradeNo = tradeNo;
	}

	/**
	 * 0 sendTextMessage, 
	 * 1 sendDataMessage,
	 * 2 sendMultipartTextMessage
	 * @return
	 */
	public int getMSGType() {
		return MSGType;
	}

	public void setMSGType(int mSGType) {
		MSGType = mSGType;
	}
}
