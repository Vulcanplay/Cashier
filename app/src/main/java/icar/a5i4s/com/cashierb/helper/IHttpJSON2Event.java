package icar.a5i4s.com.cashierb.helper;

import org.json.JSONArray;

public interface IHttpJSON2Event {
    public void startedRequest();
    /***
     * 业务操作正常结束提示信息处理
     * @param methodName 业务操作码
     * @param code 信息代码
     * @param message 信息内容
     */
    public void processNormalCode(String methodName, int code, String message);
    /***
     * 业务操作非正常结束信息处理
     * @param methodName 业务操作码
     * @param code 信息代码
     * @param message 信息内容
     */
    public void processWarnCode(String methodName, int code, String message);
    /***
     * 返回的数据处理 
     * @param methodName 业务操作码
     * @param data
     */
    public void finishedProcessData(String methodName, JSONArray data);
    /***
     * 无数据的时处理
     * @param methodName 业务操作码
     */
    public void finishedNoData(String methodName);
    /***
     * 数据出错处理
     * @param methodName 业务操作码
     * @param ex 异常
     */
    public void finishedWithException(String methodName, Exception ex);
    /***
     * 数据请求处理结束
     * @param methodName 务操作码
     * @param code 信息代码(0:正常结束 -999:不正常返回 ）
     */
    public void endedRequest(String methodName, int code);
}
