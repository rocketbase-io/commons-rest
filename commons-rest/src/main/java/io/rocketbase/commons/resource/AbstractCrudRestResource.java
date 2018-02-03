package io.rocketbase.commons.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.rocketbase.commons.dto.PageableResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.Serializable;


@Slf4j
public abstract class AbstractCrudRestResource<ResponseData, EditData, ID extends Serializable> extends AbstractBaseCrudRestResource<ResponseData, EditData> {


    @Autowired
    public AbstractCrudRestResource(RestTemplate restTemplate, ObjectMapper objectMapper) {
        super(restTemplate, objectMapper);
    }

    public PageableResult<ResponseData> find(int page, int pagesize) {
        return find(UriComponentsBuilder.fromUriString(getBaseApiUrl())
                .queryParam("page", page)
                .queryParam("pageSize", pagesize));
    }

    public ResponseData getById(ID id) {
        return getById(UriComponentsBuilder.fromUriString(
                getBaseApiUrl() + "/" + String.valueOf(id)));
    }

    public ResponseData create(EditData editData) {
        return create(UriComponentsBuilder.fromUriString(
                getBaseApiUrl()), editData);
    }

    public ResponseData update(ID id, EditData editData) {
        return update(UriComponentsBuilder.fromUriString(getBaseApiUrl()).path(String.valueOf(id)), editData);
    }

    public void delete(ID id) {
        delete(UriComponentsBuilder.fromUriString(getBaseApiUrl()).path(String.valueOf(id)));
    }

    /**
     * @return full qualified url to the entity base url
     */
    protected abstract String getBaseApiUrl();

}
