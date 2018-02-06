package io.rocketbase.commons.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.rocketbase.commons.dto.PageableResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.Serializable;


@Slf4j
public abstract class AbstractCrudRestResource<Data, Edit, ID extends Serializable> extends AbstractBaseCrudRestResource<Data, Edit> {


    @Autowired
    public AbstractCrudRestResource(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    public PageableResult<Data> find(int page, int pagesize) {
        return find(UriComponentsBuilder.fromUriString(getBaseApiUrl())
                .queryParam("page", page)
                .queryParam("pageSize", pagesize));
    }

    public Data getById(ID id) {
        return getById(buildPathWithId(id));
    }

    public Data create(Edit edit) {
        return create(UriComponentsBuilder.fromUriString(
                getBaseApiUrl()), edit);
    }

    public Data update(ID id, Edit edit) {
        return update(buildPathWithId(id), edit);
    }

    public void delete(ID id) {
        delete(buildPathWithId(id));
    }

    /**
     * @return full qualified url to the entity base url
     */
    protected abstract String getBaseApiUrl();

    private UriComponentsBuilder buildPathWithId(ID id) {
        String baseApiUrl = getBaseApiUrl();
        String idString = String.valueOf(id);
        return UriComponentsBuilder.fromUriString(baseApiUrl.endsWith("/") ? baseApiUrl + idString :
                baseApiUrl + "/" + idString);
    }

}
