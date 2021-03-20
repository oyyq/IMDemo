package net.oyyq.common;


import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.raizlabs.android.dbflow.structure.ModelAdapter;



public class Common {

    public static final Gson gson = new GsonBuilder().serializeNulls()
            // serialize {@code Date} objects according to the pattern provided.
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")
            // 设置一个过滤器，数据库级别的Model不进行Json转换
            .setExclusionStrategies(new DBFlowExclusionStrategy())
            .create();


    public static class DBFlowExclusionStrategy implements ExclusionStrategy {
        @Override
        public boolean shouldSkipField(FieldAttributes f) {
            // 被跳过的字段, 只要是属于DBFlow数据的
            return f.getDeclaredClass().equals(ModelAdapter.class);
        }

        @Override
        public boolean shouldSkipClass(Class<?> clazz) {
            // 跳过的Class: 都不跳过
            return false;
        }
    }



    public interface Constance {
        // 手机号的正则,11位手机号
        String REGEX_MOBILE = "[1][3,4,5,6,7,8,9][0-9]{9}$";

        // 基础的网络请求地址
        String API_URL = "http://10.162.107.210:8080/api/";

    }

}




