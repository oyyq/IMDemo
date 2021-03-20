package net.oyyq.dbflowdemo.remote;


import android.text.TextUtils;
import net.oyyq.common.Common;
import net.oyyq.dbflowdemo.db.Account;
import net.oyyq.dbflowdemo.factory.Factory;
import net.oyyq.factory.service.interceptor.TimeOutInterceptor;
import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


/**
 * 网络请求的封装
 */
public class Network {

    private static Network instance;
    private Retrofit retrofit;
    private OkHttpClient client;

    static {
        instance = new Network();
    }

    private Network() { }


    public static OkHttpClient getClient() {
        if (instance.client != null)
            return instance.client;


        instance.client = new OkHttpClient.Builder()
                // 给所有的请求添加2个拦截器
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        // 拿到我们的请求, 重新进行build, 请求头注入Token & Content-Type
                        Request original = chain.request();

                        Request.Builder builder = original.newBuilder();
                        if (!TextUtils.isEmpty(Account.getToken())) {
                            builder.addHeader("token", Account.getToken());
                        }

                        builder.addHeader("Content-Type", "application/json");
                        Request newRequest = builder.build();

                        return chain.proceed(newRequest);
                    }
                })
                //再添加一个针对请求的请求头设置超时时间的拦截器
                .addInterceptor(TimeOutInterceptor.getInterceptor())
                .build();

        return instance.client;
    }



    // 构建一个Retrofit
    public static Retrofit getRetrofit() {
        if (instance.retrofit != null)
            return instance.retrofit;

        // 得到一个OK Client
        OkHttpClient client = getClient();
        // Retrofit
        Retrofit.Builder builder = new Retrofit.Builder();

        // 设置电脑链接
        instance.retrofit = builder.baseUrl(Common.Constance.API_URL)
                // 设置client
                .client(client)
                // 设置Json解析器, 将Request Body中的对象解析成Json字符串
                .addConverterFactory(GsonConverterFactory.create(Factory.getGson()))
                .build();

        return instance.retrofit;

    }


    /**
     * 返回一个请求代理
     *
     * @return RemoteService
     */
    public static RemoteService remote() {
        return Network.getRetrofit().create(RemoteService.class);
    }


}
