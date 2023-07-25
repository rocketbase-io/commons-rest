package io.rocketbase.sample.converter;

import io.hypersistence.tsid.TSID;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public abstract class TsidMapper {


    public TSID convert(long id) {
        return TSID.from(id);
    }

}
