package io.rocketbase.commons.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.rocketbase.commons.dto.PageableResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.Serializable;


@Slf4j
public abstract class AbstractCrudChildRestResource<ResponseData, EditData, ID extends Serializable> extends AbstractBaseCrudRestResource<ResponseData, EditData> {

    @Autowired
    public AbstractCrudChildRestResource(RestTemplate restTemplate, ObjectMapper objectMapper) {
        super(restTemplate, objectMapper);
    }

    public PageableResult<ResponseData> find(ID parentId, int page, int pagesize) {
        return find(getParentPathWithIdAndChildPath(parentId)
                .queryParam("page", page)
                .queryParam("pageSize", pagesize));
    }

    public ResponseData getById(ID parentId, ID id) {
        return getById(getParentPathWithIdAndChildPath(parentId).path(String.valueOf(id)));
    }

    public ResponseData create(ID parentId, EditData editData) {
        return create(getParentPathWithIdAndChildPath(parentId), editData);
    }

    public ResponseData update(ID parentId, ID id, EditData editData) {
        return update(getParentPathWithIdAndChildPath(parentId).path(String.valueOf(id)), editData);
    }

    public void delete(ID parentId, ID id) {
        delete(getParentPathWithIdAndChildPath(parentId).path(String.valueOf(id)));
    }

    protected UriComponentsBuilder getParentPathWithIdAndChildPath(ID parentId) {
        return UriComponentsBuilder.fromUriString(getBaseParentApiUrl()).path(String.valueOf(parentId)).path(getChildPath());
    }

    /**
     * @return full qualified url to the parent base url <b>without ID etc</b>
     */
    protected abstract String getBaseParentApiUrl();

    /**
     * @return url path of the child entity
     */
    protected abstract String getChildPath();

}
