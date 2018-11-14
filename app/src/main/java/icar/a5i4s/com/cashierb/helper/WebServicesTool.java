package icar.a5i4s.com.cashierb.helper;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.builder.PostFormBuilder;
import com.zhy.http.okhttp.log.LoggerInterceptor;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import icar.a5i4s.com.cashierb.module.ResultData;
import okhttp3.OkHttpClient;

/**
 * Created by light on 2016/3/11.
 */
public class WebServicesTool {

    //获取本地存储
    protected SharedPreferences sf;
    //服务器
    public static String url;

    //String url = "http://www.5i4s.com/icar/admin/";
    public WebServicesTool(Context context){
        sf = context.getSharedPreferences("setting", Activity.MODE_PRIVATE);
        if(sf.getString("typeDebug", "").equals("")){
            SharedPreferences sp;
            SharedPreferences.Editor editor;
            //存储到 XML
            sp = context.getSharedPreferences("setting", 0);
            editor = sp.edit();
            editor.putString("typeDebug", "http://www.open4s.net/icar/admin/");//测试环境
            editor.putBoolean("debugPower", false);
            editor.putString("typeRelease", "http://www.5i4s.com/icar/admin/");//正式环境
            editor.putBoolean("releasePower", true);
//            editor.putString("typeRepair", "http://www.open4ss.net/icar/admin/");//补送模拟
//            editor.putBoolean("repairPower", false);
            editor.commit();
        }

        if (sf.getBoolean("debugPower", false)){
            url = sf.getString("typeDebug", "");
            Log.d("====测试环境开启", url);
        } else if(sf.getBoolean("releasePower", false)) {
            url = sf.getString("typeRelease", "");
            Log.d("====正式环境开启", url);
        }
//        else if(sf.getBoolean("repairPower", false)) {
//            url = sf.getString("typeRepair", "");
//            Log.d("====补送模式开启", url);
//        }
    }

    public PostFormBuilder Connect(String action){
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new LoggerInterceptor("TAG"))
                //其他配置
                .build();
        OkHttpUtils.initClient(okHttpClient);
        return OkHttpUtils
                .post()
                .url(url + action);
    }

    public static Gson getOrderGson(){
        return new GsonBuilder()
                .registerTypeAdapter(
                        new TypeToken<ResultData>(){}.getType(),
                        new JsonDeserializer<ResultData>() {
                            @Override
                            public ResultData deserialize(
                                    JsonElement json, Type typeOfT,
                                    JsonDeserializationContext context) throws JsonParseException {
                                ResultData treeMap = new ResultData();
                                JsonObject jsonObject = json.getAsJsonObject();
                                Set<Map.Entry<String, JsonElement>> entrySet = jsonObject.entrySet();
                                for (Map.Entry<String, JsonElement> entry : entrySet) {
                                    String keyName=entry.getKey().toString();
                                    if("dataList".equals(keyName)){
                                        List<Map<String,Object>> dataList = new ArrayList<Map<String, Object>>();
                                        if(entry.getValue().isJsonArray()){
                                            JsonArray jsonArray =entry.getValue().getAsJsonArray();
                                            for (JsonElement json1:jsonArray ) {
                                                Map<String,Object> map = new HashMap<String, Object>();
                                                Set<Map.Entry<String, JsonElement>> itemSet =json1.getAsJsonObject().entrySet();
                                                for (Map.Entry<String, JsonElement> item :itemSet){
                                                    map.put(item.getKey(),item.getValue().isJsonNull()?null:item.getValue().getAsString());
                                                }
                                                dataList.add(map);
                                            }
                                         }

                                        treeMap.setDataList(dataList);
                                    }else if("code".equals(keyName)){
                                        treeMap.setCode(entry.getValue().isJsonNull() ? null:entry.getValue().getAsString());
                                    }else if("success".equals(keyName)){
                                        treeMap.setSuccess(entry.getValue().getAsBoolean());
                                    }
                                }
                                return treeMap;
                            }
                        }).create();
    }
}
