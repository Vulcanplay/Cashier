package icar.a5i4s.com.cashierb.helper;

import org.json.JSONArray;

public interface HttpUtilEvent {

    /**
     * @param methodName
     * @param data
     */
    public void finishedProcessData(String methodName, JSONArray data);
    
    /**
     * @param methodName
     * @param errorMsg
     */
    public void finishedWithErrorOrMsg(String methodName, String errorMsg);
}
