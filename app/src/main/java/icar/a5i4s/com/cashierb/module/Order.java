package icar.a5i4s.com.cashierb.module;

import icar.a5i4s.com.cashierb.helper.MD5;

/**
 * Created by light on 2016/9/3.
 */
public class Order {
    private int id;
    private String orderId, appName, transId, resultCode, resultMsg, transData, sign, userId, onlyLabel, orgTraceNo, msgText, time, amt, cancelTraceNo;

    public String getCancelTraceNo() {
        return cancelTraceNo;
    }

    public void setCancelTraceNo(String cancelTraceNo) {
        this.cancelTraceNo = cancelTraceNo;
    }

    public Order(int id, String orderId, String appName, String transId, String resultCode, String resultMsg, String transData, String userId) {
        this.id = id;
        this.orderId = orderId;
        this.appName = appName;
        this.transId = transId;
        this.resultCode = resultCode;
        this.resultMsg = resultMsg;
        this.transData = transData;
        this.sign = MD5.getMD5(orderId + appName + transId + resultCode + resultMsg + transData);
        this.userId = userId;
    }
    /*history*/
    public Order(int id, String orderId, String appName, String transId, String resultCode, String resultMsg, String transData, String userId, String orgTraceNo, String onlyLabel,String msgText, String time, String amt) {
        this.id = id;
        this.orderId = orderId;
        this.appName = appName;
        this.transId = transId;
        this.resultCode = resultCode;
        this.resultMsg = resultMsg;
        this.transData = transData;
        this.sign = MD5.getMD5(orderId + appName + transId + resultCode + resultMsg + transData);
        this.userId = userId;
        this.orgTraceNo = orgTraceNo;
        this.onlyLabel = onlyLabel;
        this.msgText = msgText;
        this.time = time;
        this.amt = amt;
    }

    public Order(int id, String orderId, String appName, String transId, String resultCode, String resultMsg, String transData) {
        this.id = id;
        this.orderId = orderId;
        this.appName = appName;
        this.transId = transId;
        this.resultCode = resultCode;
        this.resultMsg = resultMsg;
        this.transData = transData;
        this.sign = MD5.getMD5(orderId + appName + transId + resultCode + resultMsg + transData);
    }
    public Order(String orderId, String appName, String transId, String resultCode, String resultMsg, String transData, String userId) {
        this.orderId = orderId;
        this.appName = appName;
        this.transId = transId;
        this.resultCode = resultCode;
        this.resultMsg = resultMsg;
        this.transData = transData;
        this.sign = MD5.getMD5(orderId + appName + transId + resultCode + resultMsg + transData);
        this.userId = userId;
    }

    //异常 构造函数
    public Order(int id, String orderId,String orgTraceNo, String onlyLabel) {
        this.id = id;
        this.orderId = orderId;
        this.orgTraceNo = orgTraceNo;
        this.onlyLabel = onlyLabel;
    }
    public Order(String orderId, String orgTraceNo, String onlyLabel, String userId) {
        this.orderId = orderId;
        this.onlyLabel = onlyLabel;
        this.orgTraceNo = orgTraceNo;
        this.userId = userId;
    }

    public String getAmt() {
        return amt;
    }

    public void setAmt(String amt) {
        this.amt = amt;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getMsgText() {
        return msgText;
    }

    public void setMsgText(String msgText) {
        this.msgText = msgText;
    }

    public String getOrgTraceNo() {
        return orgTraceNo == null ? "" : orgTraceNo;
    }

    public void setOrgTraceNo(String orgTraceNo) {
        this.orgTraceNo = orgTraceNo;
    }

    public String getOnlyLabel() {
        return onlyLabel;
    }

    public void setOnlyLabel(String onlyLabel) {
        this.onlyLabel = onlyLabel;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOrderId() {
        return orderId == null ? "" : orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getTransId() {
        return transId;
    }

    public void setTransId(String transId) {
        this.transId = transId;
    }

    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public String getResultMsg() {
        return resultMsg;
    }

    public void setResultMsg(String resultMsg) {
        this.resultMsg = resultMsg;
    }

    public String getTransData() {
        return transData;
    }

    public void setTransData(String transData) {
        this.transData = transData;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }


    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }
}
