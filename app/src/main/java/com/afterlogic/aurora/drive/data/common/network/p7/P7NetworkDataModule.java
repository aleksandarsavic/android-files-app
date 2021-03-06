package com.afterlogic.aurora.drive.data.common.network.p7;

import com.afterlogic.aurora.drive.BuildConfig;
import com.afterlogic.aurora.drive.data.common.annotations.P7;
import com.afterlogic.aurora.drive.data.common.network.DynamicDomainProvider;
import com.afterlogic.aurora.drive.data.common.network.DynamicEndPointInterceptor;
import com.afterlogic.aurora.drive.data.common.network.util.IgnoreDeserealizationExcludeStrategy;
import com.afterlogic.aurora.drive.data.common.network.util.IgnoreSerealizationExcludeStrategy;
import com.afterlogic.aurora.drive.data.model.project7.ApiResponseP7;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by sashka on 03.02.17.<p/>
 * mail: sunnyday.development@gmail.com
 */
@Module
public class P7NetworkDataModule {

    @Provides
    @P7
    OkHttpClient provideClient(DynamicDomainProvider domainProvider){
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();

        //Dynamic notifyEnd point interceptor
        clientBuilder.addInterceptor(new DynamicEndPointInterceptor(
                DynamicEndPointInterceptor.DYNAMIC_BASE_URL,
                domainProvider
        ));

        clientBuilder.addInterceptor(new DownloadInterceptor());
        clientBuilder.addInterceptor(new UploadInterceptor());

        //Add logging for debug
        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(
                    BuildConfig.DEBUG_HTTP
                    ? HttpLoggingInterceptor.Level.BODY
                    : HttpLoggingInterceptor.Level.HEADERS
            );
            clientBuilder.addInterceptor(interceptor);
        }

        return clientBuilder
                .connectTimeout(1, TimeUnit.MINUTES)
                .readTimeout(1, TimeUnit.MINUTES)
                .writeTimeout(1, TimeUnit.MINUTES)
                .build();
    }

    @Provides @P7
    Gson provideGson(){
        Gson gson = new GsonBuilder()
                .addDeserializationExclusionStrategy(new IgnoreDeserealizationExcludeStrategy())
                .addSerializationExclusionStrategy(new IgnoreSerealizationExcludeStrategy())
                .create();

        return new GsonBuilder()
                .registerTypeAdapter(ApiResponseP7.class, new ApiResponseConverter7(gson))
                .create();
    }

    @Provides @P7
    Retrofit provideRetrofit(@P7 Gson gson, @P7 OkHttpClient client){
        return new Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)
                .baseUrl(DynamicEndPointInterceptor.DYNAMIC_BASE_URL)
                .build();
    }

    @Provides
    Api7 provideApi(@P7 Retrofit retrofit){
        return retrofit.create(Api7.class);
    }
}
