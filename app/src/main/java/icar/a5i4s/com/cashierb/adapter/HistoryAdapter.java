package icar.a5i4s.com.cashierb.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import icar.a5i4s.com.cashierb.R;
import icar.a5i4s.com.cashierb.module.Order;

/**
 * Created by light on 2016/11/29.
 */

public class HistoryAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private List<Order> lo;
    protected Context context;
    public HistoryAdapter(Context context, List<Order> lo){
        mInflater = LayoutInflater.from(context);
        this.lo = lo;
        this.context = context;
    }

    @Override
    public int getCount() {
        return lo.size();
    }

    @Override
    public Object getItem(int position) {
        return lo.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    Order mode;
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        mode = lo.get(position);
        if(convertView == null){
            viewHolder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.history_item, null);
            viewHolder.amt = (TextView) convertView.findViewById(R.id.amt);
            viewHolder.orderId = (TextView) convertView.findViewById(R.id.orderId);
            viewHolder.transId = (TextView) convertView.findViewById(R.id.transId);
            viewHolder.onlyLabel = (TextView) convertView.findViewById(R.id.onlyLabel);
            viewHolder.time = (TextView) convertView.findViewById(R.id.time);
            viewHolder.org = (TextView) convertView.findViewById(R.id.org);
            viewHolder.cancel = (TextView) convertView.findViewById(R.id.cancel);
            viewHolder.status = (TextView) convertView.findViewById(R.id.status);

            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }
        if (mode.getOrderId() == null || mode.getOrderId().equals("")){
            viewHolder.orderId.setText("");
        }else{
            viewHolder.orderId.setText(mode.getOrderId());
        }
        viewHolder.time.setText(mode.getTime());
        viewHolder.onlyLabel.setText(mode.getOnlyLabel());
        viewHolder.transId.setText(mode.getTransId() == null ? "" : mode.getTransId());
        viewHolder.amt.setText(mode.getAmt());
        viewHolder.org.setText(mode.getOrgTraceNo());
//        if (mode.getCancelTraceNo() != null){
//            viewHolder.cancel.setVisibility(View.VISIBLE);
//            viewHolder.cancel.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
        viewHolder.org.setText(mode.getOrgTraceNo() == null ? "" : mode.getOrgTraceNo());
        if (mode.getCancelTraceNo() == null || mode.getCancelTraceNo().equals("")){
            viewHolder.cancel.setText("");
        } else {
            viewHolder.cancel.setText("("+mode.getCancelTraceNo()+")");
        }
        //}
        /**
         * "POS系统异常" pos_system_error
         * "支付完成，POS网络异常" pay_success_pos_network_error
         * "支付成功，iCar网络异常" pay_success_icar_network_error
         * "撤销成功，POS网络异常" revoke_success_pos_network_error
         * "撤销成功，iCar网络异常" revoke_success_icar_network_error
         *"支付成功" pay_success
         * 撤销成功 revoke_success
         * 补送成功 repair_success  context.getString(R.string.pos_system_error)
         * */
        if (mode.getMsgText() == null)
            mode.setMsgText("");
        if (mode.getMsgText().equals(context.getString(R.string.pay_success_pos_network_error)) ||
                mode.getMsgText().equals(context.getString(R.string.pay_success_icar_network_error)) ||
                mode.getMsgText().equals(context.getString(R.string.revoke_success_pos_network_error)) ||
                mode.getMsgText().equals(context.getString(R.string.revoke_success_icar_network_error)) ||
                mode.getMsgText().equals(context.getString(R.string.pos_system_error))){
            viewHolder.status.setTextColor(context.getResources().getColor(R.color.array_4));
        } else if (mode.getMsgText().equals(context.getString(R.string.pay_success)) ||
                    mode.getMsgText().equals(context.getString(R.string.revoke_success)) ||
                    mode.getMsgText().equals(context.getString(R.string.repair_success))){
            viewHolder.status.setTextColor(context.getResources().getColor(R.color.array_2));
        } else {
            viewHolder.status.setTextColor(context.getResources().getColor(R.color.array_3));
        }
        viewHolder.status.setText(mode.getMsgText());
        return convertView;
    }
    class ViewHolder{
        TextView time;
        TextView transId;
        TextView onlyLabel;
        TextView orderId;
        TextView org;
        TextView cancel;
        TextView amt;
        TextView status;
    }
}
