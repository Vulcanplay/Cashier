package icar.a5i4s.com.cashierb.module;

/**
 * Created by light on 2016/11/23.
 */

public class WXTicket {
    /**
     * 金额
     * 商品名
     * 商户
     * 支付状态
     * 支付日期
     * 交易单号
     * */
    //
    private String amt, shopName, appName, status, date, transId, orderId;

    public WXTicket(String amt, String shopName, String appName, String status, String date, String transId) {
        this.amt = amt;
        this.shopName = shopName;
        this.appName = appName;
        this.status = status;
        this.date = date;
        this.transId = transId;
    }

    public WXTicket(String amt, String date, String transId, String orderId) {
        this.amt = amt;
        this.date = date;
        this.transId = transId;
        this.orderId = orderId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getAmt() {
        return amt;
    }

    public void setAmt(String amt) {
        this.amt = amt;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTransId() {
        return transId;
    }

    public void setTransId(String transId) {
        this.transId = transId;
    }
}
