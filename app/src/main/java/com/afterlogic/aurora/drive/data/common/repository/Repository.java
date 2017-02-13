package com.afterlogic.aurora.drive.data.common.repository;

import com.afterlogic.aurora.drive._unrefactored.model.ApiResponse;
import com.afterlogic.aurora.drive.data.common.Observator;
import com.afterlogic.aurora.drive.data.common.cache.SharedObservableStore;
import com.afterlogic.aurora.drive.data.common.mapper.Mapper;
import com.afterlogic.aurora.drive.model.error.ApiResponseError;

import io.reactivex.Single;

/**
 * Created by sashka on 01.09.16.<p/>
 * mail: sunnyday.development@gmail.com
 */
public class Repository extends Observator {

    public Repository(SharedObservableStore cache, String repositoryId) {
        super(cache, repositoryId);
    }

    public static  <T, R> Single<T> withNetRawMapper(Single<ApiResponse<R>> observable, Mapper<T, ApiResponse<R>> mapper){
        return observable.flatMap(result -> {
            if (result.isSuccess()){
                return Single.just(mapper.map(result));
            } else {
                return Single.error(new ApiResponseError(result.getErrorCode(), result.getErrorMessage()));
            }
        });
    }

    public static  <T, R> Single<T> withNetMapper(Single<ApiResponse<R>> observable, Mapper<T, R> mapper){
        return observable.flatMap(result -> {
            if (result.isSuccess()){
                return Single.just(mapper.map(result.getResult()));
            } else {
                return Single.error(new ApiResponseError(result.getErrorCode(), result.getErrorMessage()));
            }
        });
    }

    public static  <T, R extends ApiResponse<T>> Single<T> withNetMapper(Single<R> observable){
        return observable.flatMap(result -> {
            if (result.isSuccess()){
                return Single.just(result.getResult());
            } else {
                return Single.error(new ApiResponseError(result.getErrorCode(), result.getErrorMessage()));
            }
        });
    }
}