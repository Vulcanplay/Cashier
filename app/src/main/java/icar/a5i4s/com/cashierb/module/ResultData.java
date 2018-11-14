package icar.a5i4s.com.cashierb.module;

import java.util.ArrayList;
import java.util.List;

public class ResultData {
	private boolean success = true;
	private ArrayList<Error> errors;
	private String errorMassge;
	private int total;
	private List<?> dataList;
	private String status;
	private double djqMoney;  //代金券金额
	private String code;
	
	public void setDataList(List<?> dataList) {
		this.dataList = dataList;
	}
	public List<?> getDataList() {
		return dataList;
	}
	public int getTotal() {
		return total;
	}
	public void setTotal(int total) {
		this.total = total;
	}
	public void setSuccess(boolean success) {
		this.success = success;
	}
	public boolean getSuccess() {
		return success;
	}
	public ArrayList<Error> getErrors() {
		return errors;
	}

	public void addError(String field, String message){
		Error err = new Error(field, message);
		if(errors == null){
			errors = new ArrayList<Error>();
		}
		errors.add(err);
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getErrorMassge() {
		return errorMassge;
	}
	public void setErrorMassge(String errorMassge) {
		this.errorMassge = errorMassge;
	}
	public double getDjqMoney() {
		return djqMoney;
	}
	public void setDjqMoney(double djqMoney) {
		this.djqMoney = djqMoney;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	
}
