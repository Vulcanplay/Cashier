package icar.a5i4s.com.cashierb.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import icar.a5i4s.com.cashierb.R;
import icar.a5i4s.com.cashierb.module.WXTicket;

/**
 * Created by light on 2016/11/29.
 */

public class WeChatRePrintAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private List<WXTicket> lv;
    public WeChatRePrintAdapter(Context context, List<WXTicket> lv){
        mInflater = LayoutInflater.from(context);
        this.lv = lv;
    }

    @Override
    public int getCount() {
        return lv.size();
    }

    @Override
    public Object getItem(int position) {
        return lv.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        WXTicket mode = lv.get(position);
        if(convertView == null){
            viewHolder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.reprint_item, null);
            viewHolder.reAMT = (TextView) convertView.findViewById(R.id.rp_amt);
            viewHolder.reOrderId = (TextView) convertView.findViewById(R.id.rp_orderId);
            viewHolder.reTransId = (TextView) convertView.findViewById(R.id.rp_transId);
            viewHolder.reTime = (TextView) convertView.findViewById(R.id.rp_time);

            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.reOrderId.setText(mode.getOrderId());
        viewHolder.reTime.setText(mode.getDate());
        viewHolder.reTransId.setText(mode.getTransId());
        viewHolder.reAMT.setText(mode.getAmt());
        return convertView;
    }
    class ViewHolder{
        TextView reTime;
        TextView reTransId;
        TextView reOrderId;
        TextView reAMT;
    }
}
