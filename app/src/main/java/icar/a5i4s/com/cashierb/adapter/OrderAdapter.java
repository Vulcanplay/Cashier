package icar.a5i4s.com.cashierb.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import icar.a5i4s.com.cashierb.R;

/**
 * Created by light on 2017/2/21.
 */

public class OrderAdapter extends BaseAdapter implements Filterable {
    private LayoutInflater mInflater;

    protected int colors[] = {0,
            R.color.array_1,
            R.color.array_2,
            R.color.array_3,
            R.color.array_4,
            R.color.array_5,
            R.color.array_6,
            R.color.array_7,
            R.color.array_8,
            R.color.array_9};
    private List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
    Context context;
    public OrderAdapter(Context context, List<Map<String, Object>> data) {
        // TODO Auto-generated constructor stub
        mInflater = LayoutInflater.from(context);
        this.data = data;
        this.context = context;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        ViewHolder viewHolder = null;
        Map<String, Object> map = data.get(position);
        if(convertView == null){
            viewHolder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.order_cashier_item, null);
            viewHolder.type = (TextView) convertView.findViewById(R.id.type);
            viewHolder.name = (TextView) convertView.findViewById(R.id.name);
            viewHolder.orderId = (TextView) convertView.findViewById(R.id.orderId);
            viewHolder.price = (TextView) convertView.findViewById(R.id.price);
            viewHolder.priceValue = (TextView) convertView.findViewById(R.id.price_value);

            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.type.setText(map.get("orderTypeName").toString());
        viewHolder.type.setBackgroundResource(colors[Integer.parseInt(map.get("orderType").toString()) % 10]);
        viewHolder.name.setText(map.get("customerName") == null ? context.getString(R.string.anonymous) : map.get("customerName").toString());
        Log.w("====TEST", String.valueOf(map.get("id")));
        viewHolder.orderId.setText(map.get("orderId").toString());
        viewHolder.price.setText(map.get("actualAmount").toString() + context.getString(R.string.YUAN));
        viewHolder.priceValue.setText(map.get("actualAmount").toString());

        return convertView;
    }
    MyFilter mFilter;
    //当ListView调用setTextFilter()方法的时候，便会调用该方法
    @Override
    public Filter getFilter() {
        if (mFilter ==null){
            mFilter = new MyFilter();
        }
        return mFilter;
    }
    //我们需要定义一个过滤器的类来定义过滤规则
    class MyFilter extends Filter {
        //我们在performFiltering(CharSequence charSequence)这个方法中定义过滤规则
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            FilterResults result = new FilterResults();
            List<Map<String, Object>> list;
            if (TextUtils.isEmpty(charSequence)){//当过滤的关键字为空的时候，我们则显示所有的数据
                list  = data;
            }else {//否则把符合条件的数据对象添加到集合中
                list = new ArrayList<>();
                for (Map<String, Object> l : list){
                    if (l.get("orderId").toString().contains(charSequence)){
                        list.add(l);
                    }
                }
            }
            result.values = list; //将得到的集合保存到FilterResults的value变量中
            result.count = list.size();//将集合的大小保存到FilterResults的count变量中
            return result;
        }
        //在publishResults方法中告诉适配器更新界面
        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            data = (List<Map<String, Object>>)filterResults.values;
            if (filterResults.count>0){
                notifyDataSetChanged();//通知数据发生了改变
            }else {
                notifyDataSetInvalidated();//通知数据失效
            }
        }
    }

    class ViewHolder{
        TextView type;
        TextView name;
        TextView orderId;
        TextView price;
        TextView priceValue;
    }
}
