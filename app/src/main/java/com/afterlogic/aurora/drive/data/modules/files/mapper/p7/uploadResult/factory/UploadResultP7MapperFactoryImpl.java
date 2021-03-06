package com.afterlogic.aurora.drive.data.modules.files.mapper.p7.uploadResult.factory;

import com.afterlogic.aurora.drive.data.common.mapper.Mapper;
import com.afterlogic.aurora.drive.data.modules.files.mapper.p7.uploadResult.concrete.UploadResultP7toBlMapper;
import com.afterlogic.aurora.drive.data.model.UploadResult;
import com.afterlogic.aurora.drive.data.model.project7.UploadResultP7;

import javax.inject.Inject;

/**
 * Created by sashka on 15.11.16.<p/>
 * mail: sunnyday.development@gmail.com
 */
public class UploadResultP7MapperFactoryImpl implements UploadResultP7MapperFactory {

    @Inject public UploadResultP7MapperFactoryImpl() {
    }

    @Override
    public Mapper<UploadResult, UploadResultP7> p7toBl(){
        return new UploadResultP7toBlMapper();
    }
}
