package io.rocketbase.commons.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.rocketbase.commons.dto.PageableResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.Serializable;
import java.util.function.Consumer;


@Slf4j
public abstract class AbstractCrudRestResource<Data, Edit, ID extends Serializable> extends AbstractBaseCrudRestResource<Data, Edit> {


    @Autowired
    public AbstractCrudRestResource(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    public void findAllAndExecute(Consumer<Data> execute, int pagesize) {
        int page = 0;
        PageableResult<Data> pageableResult;
        do {
            pageableResult = find(page++, pagesize);
            pageableResult.forEach(execute);
        } while (pageableResult.hasNextPage());
    }

    public PageableResult<Data> find(int page, int pagesize) {
        return find(buildBaseUriBuilder()
                .queryParam("page", page)
                .queryParam("pageSize", pagesize));
    }

    public Data getById(ID id) {
        return getById(buildBaseUriBuilder().path(String.valueOf(id)));
    }

    public Data create(Edit edit) {
        return create(UriComponentsBuilder.fromUriString(
                getBaseApiUrl()), edit);
    }

    public Data update(ID id, Edit edit) {
        return update(buildBaseUriBuilder().path(String.valueOf(id)), edit);
    }

    public void delete(ID id) {
        delete(buildBaseUriBuilder().path(String.valueOf(id)));
    }

    /**
     * @return full qualified url to the entity base url
     */
    protected abstract String getBaseApiUrl();

    UriComponentsBuilder buildBaseUriBuilder() {
        return UriComponentsBuilder.fromUriString(getBaseApiUrl() + (getBaseApiUrl().endsWith("/") ? "" : "/"));
    }

}
