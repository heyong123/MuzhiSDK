package mobi.zty.sdk.game.bean;

import java.io.Serializable;

public class UserInfo implements Serializable {

    private int userId;
    private String loginAccount;

    private int result;
    private int amount;
    private int max_amount;
    private String message;

    private String sign;
    
    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }
    
    
    public UserInfo() {
    }

    public UserInfo(int userId, String loginAccount) {
        this.userId = userId;
        this.loginAccount = loginAccount;
    }
    public int getMax_amount() {
        return max_amount;
    }

    public void setMax_amount(int max_amount) {
        this.max_amount = max_amount;
    }
    
    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getLoginAccount() {
        return loginAccount;
    }

    public void setLoginAccount(String loginAccount) {
        this.loginAccount = loginAccount;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "UserInfo{" +
                "userId=" + userId +
                ", loginAccount='" + loginAccount + '\'' +
                '}';
    }

}
