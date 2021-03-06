package com.afterlogic.aurora.drive.data.common.network.p8;

import androidx.annotation.NonNull;
import android.text.TextUtils;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by sashka on 19.10.16.<p/>
 * mail: sunnyday.development@gmail.com
 */
public class CloudServiceP8 {

    private final String mModuleName;
    private final Gson mGson;

    public CloudServiceP8(@NonNull String moduleName, @NonNull Gson gson) {
        mModuleName = moduleName;
        mGson = gson;
    }

    protected Map<String, Object> getDefaultFields(@NonNull String method, @NonNull String params){
        Map<String, Object> fields = new HashMap<>();
        fields.put(Api8.ApiField.MODULE, mModuleName);
        fields.put(Api8.ApiField.METHOD, method);
        if (!TextUtils.isEmpty(params)) {
            fields.put(Api8.ApiField.PARAMS, params);
        }
        return fields;
    }

    protected Map<String, Object> getDefaultFields(@NonNull String method, Map<String, ?> params){
        return getDefaultFields(method, params == null || params.isEmpty()? "" : mGson.toJson(params));
    }
}
