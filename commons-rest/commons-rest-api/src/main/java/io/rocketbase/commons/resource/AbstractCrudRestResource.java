package io.rocketbase.commons.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.rocketbase.commons.dto.PageableResult;
import io.rocketbase.commons.request.PageableRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.Serializable;
import java.util.function.Consumer;


@Slf4j
public abstract class AbstractCrudRestResource<Read, Write, ID extends Serializable> extends AbstractBaseCrudRestResource<Read, Write> {


    @Autowired
    public AbstractCrudRestResource(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    /**
     * will call paged find method, iterate through all results and execute the consumer on all data instances
     *
     * @param execute   method to exeute on all found instances
     * @param batchSize pagesize with which the find method is called
     */
    public void executeAll(Consumer<Read> execute, int batchSize) {
        int page = 0;
        PageableResult<Read> pageableResult;
        do {
            pageableResult = find(page++, batchSize);
            pageableResult.forEach(execute);
        } while (pageableResult.hasNextPage());
    }

    public PageableResult<Read> find(int page, int pagesize) {
        return find(appendParams(buildBaseUriBuilder(),
                new PageableRequest(page, pagesize, null)));
    }

    public PageableResult<Read> find(PageableRequest request) {
        return find(appendParams(buildBaseUriBuilder(), request));
    }

    public Read getById(ID id) {
        return getById(buildBaseUriBuilder().path(String.valueOf(id)));
    }

    public Read create(Write write) {
        return create(UriComponentsBuilder.fromUriString(
                getBaseApiUrl()), write);
    }

    public Read update(ID id, Write write) {
        return update(buildBaseUriBuilder().path(String.valueOf(id)), write);
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
