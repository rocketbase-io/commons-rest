package io.rocketbase.commons.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.rocketbase.commons.dto.PageableResult;
import io.rocketbase.commons.request.PageableRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.Serializable;


@Slf4j
public abstract class AbstractCrudChildRestResource<Read, Write, ID extends Serializable> extends AbstractBaseCrudRestResource<Read, Write> {

    @Autowired
    public AbstractCrudChildRestResource(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    public PageableResult<Read> find(ID parentId, int page, int pagesize) {
        return find(appendParams(buildBaseUriBuilder(parentId),
                new PageableRequest(page, pagesize, null)));
    }

    public PageableResult<Read> find(ID parentId, PageableRequest request) {
        return find(appendParams(buildBaseUriBuilder(parentId), request));
    }

    public Read getById(ID parentId, ID id) {
        return getById(buildBaseUriBuilder(parentId).path(String.valueOf(id)));
    }

    public Read create(ID parentId, Write write) {
        return create(buildBaseUriBuilder(parentId), write);
    }

    public Read update(ID parentId, ID id, Write write) {
        return update(buildBaseUriBuilder(parentId).path(String.valueOf(id)), write);
    }

    public void delete(ID parentId, ID id) {
        delete(buildBaseUriBuilder(parentId).path(String.valueOf(id)));
    }

    UriComponentsBuilder buildBaseUriBuilder(ID parentId) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(ensureEndsWithSlash(getBaseParentApiUrl()));
        builder.path(String.valueOf(parentId));
        builder.path((getChildPath().startsWith("/") ? "" : "/") + ensureEndsWithSlash(getChildPath()));
        return builder;
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
