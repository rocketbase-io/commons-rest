package io.rocketbase.commons.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.rocketbase.commons.dto.PageableResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.Serializable;


@Slf4j
public abstract class AbstractCrudChildRestResource<Data, Edit, ID extends Serializable> extends AbstractBaseCrudRestResource<Data, Edit> {

    @Autowired
    public AbstractCrudChildRestResource(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    public PageableResult<Data> find(ID parentId, int page, int pagesize) {
        return find(getParentPathWithIdAndChildPath(parentId)
                .queryParam("page", page)
                .queryParam("pageSize", pagesize));
    }

    public Data getById(ID parentId, ID id) {
        return getById(getParentPathWithIdAndChildPath(parentId).path(String.valueOf(id)));
    }

    public Data create(ID parentId, Edit edit) {
        return create(getParentPathWithIdAndChildPath(parentId), edit);
    }

    public Data update(ID parentId, ID id, Edit edit) {
        return update(getParentPathWithIdAndChildPath(parentId).path(String.valueOf(id)), edit);
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
