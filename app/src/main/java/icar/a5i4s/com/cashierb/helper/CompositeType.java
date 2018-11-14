package icar.a5i4s.com.cashierb.helper;

/**
 * Created by zhang-wei on 2016/3/9.
 */
public class CompositeType {


    public Boolean BoolValue;

    public String StringValue;

    public void setStringValue(String stringValue) {
        StringValue = stringValue;
    }

    public void setBoolValue(Boolean boolValue) {
        BoolValue = boolValue;
    }

    public Boolean getBoolValue() {
        return BoolValue;

    }

    public String getStringValue() {
        return StringValue;
    }

    public CompositeType() {
    }

    public CompositeType(
            Boolean boolValue,
            String stringValue) {
        this.BoolValue = boolValue;
        this.StringValue = stringValue;
    }


}
