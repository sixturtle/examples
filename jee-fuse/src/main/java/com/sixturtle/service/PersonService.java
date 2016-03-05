package com.sixturtle.service;

import java.util.List;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sixturtle.db.PersonRepository;
import com.sixturtle.exception.InvalidEntityException;
import com.sixturtle.exception.UnknownEntityException;
import com.sixturtle.model.PersonEntity;
import com.sixturtle.web.PaginatedModel;
import com.sixturtle.web.URLHelper;

/**
 * Represents REST API for {@link PersonEntity}.
 *
 * @author Anurag Sharma
 */
@Transactional
@Path("/persons")
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
public class PersonService {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Context
    protected UriInfo uriInfo;

    @Inject
    private PersonRepository repository;

    /**
     * @param repository the repository to set
     */
    public void setRepository(PersonRepository repository) {
        this.repository = repository;
    }

    /**
     * Represents POST operation to create a new resource.
     *
     * @param resource
     *            The resource of type {@link PersonEntity}
     *
     * @return {@link Response} with one of the following codes.
     *         <ul>
     *          <li>201 in case success with no content</li>
     *          <li>400 if input validation fails</li>
     *          <li>500 in case of system error.</li>
     *         </ul>
     */
    @POST
    public Response createResource(@Valid final PersonEntity resource) {
        try {
            PersonEntity entity = repository.create(resource);
            log.debug("created entity: {}", entity);
            return Response.created(URLHelper.selfLink(uriInfo, entity.getId().toString(), this.getClass()))
                           .build();
        } catch (InvalidEntityException e) {
            throw new BadRequestException(e);
        }
    }

    /**
     * Represents PUT operation to update an existing resource.
     *
     * @param resourceId
     *            The id of type {@link Long} for the resource of type {@link PersonEntity}
     * @param resource
     *            The resource of type {@link PersonEntity}
     *
     * @return {@link Response} with one of the following codes.
     *         <ul>
     *          <li>204 in case success with no content</li>
     *          <li>404 if resourceId is not found</li>
     *          <li>400 if input validation fails</li>
     *          <li>500 in case of system error.</li>
     *         </ul>
     */
    @PUT
    @Path("{id}")
    public Response updateResource(@PathParam("id") final Long resourceId, @Valid final PersonEntity resource) {
        try {
            PersonEntity entity = repository.update(resourceId, resource);
            log.debug("updated entity: {}", entity);
            return Response.noContent().build();
        } catch (InvalidEntityException e) {
            throw new BadRequestException(e);
        } catch (UnknownEntityException e) {
            throw new NotFoundException(e);
        }
    }

    /**
     * Represents DELETE operation to remove an existing resource.
     *
     * @param resourceId
     *            The id of type {@link Long} for the resource of type {@link PersonEntity}
     *
     * @return {@link Response} with one of the following codes.
     *         <ul>
     *          <li>204 in case success with no content</li>
     *          <li>404 if resourceId is not found</li>
     *          <li>500 in case of system error.</li>
     *         </ul>
     */
    @DELETE
    @Path("{id}")
    public Response deleteResource(@PathParam("id") final Long resourceId) {
        try {
            repository.delete(resourceId);
            log.debug("deleted entity: {}", resourceId);
            return Response.noContent().build();
        } catch (UnknownEntityException e) {
            throw new NotFoundException(e);
        }
    }

    /**
     * Represents GET operation to retrieve an existing resource.
     *
     * @param resourceId
     *            The id of type {@link Long} for the resource of type {@link PersonEntity}
     *
     * @return {@link Response} with one of the following codes.
     *         <ul>
     *          <li>200 in case success with entity as T</li>
     *          <li>404 if resourceId is not found</li>
     *          <li>500 in case of system error.</li>
     *         </ul>
     */
    @GET
    @Path("{id}")
    public Response findResource(@PathParam("id")final Long resourceId) {
        PersonEntity entity = repository.find(resourceId);
        if (entity != null) {
            return Response.ok().entity(entity).build();
        } else {
            throw new NotFoundException("Unable to find " + resourceId);
        }
    }

    /**
     * Represents HEAD operation to retrieve an existing resource.
     *
     * @param resourceId
     *            The id of type {@link Long} for the resource of type {@link PersonEntity}
     *
     * @return {@link Response} with one of the following codes.
     *         <ul>
     *          <li>204 in case success with headers only</li>
     *          <li>404 if resourceId is not found</li>
     *          <li>500 in case of system error.</li>
     *         </ul>
     */
    @HEAD
    @Path("{id}")
    public Response checkResource(@PathParam("id")final Long resourceId) {
        PersonEntity entity = repository.find(resourceId);
        if (entity != null) {
        return Response.ok(URLHelper.selfLink(uriInfo, entity.getId().toString(), this.getClass()))
                       .status(Status.NO_CONTENT)
                       .build();
        } else {
            throw new NotFoundException("Unable to find " + resourceId);
        }
    }

    /**
     * Represents GET operation to retrieve a list of resources of type PersonEntity.
     *
     * @param offset
     *            The start index of the list
     * @param limit
     *            Max elements in the list
     *
     * @return {@link Response} with one of the following codes.
     *         <ul>
     *          <li>200 in case success with a list of entities of type T and navigation headers</li>
     *          <li>500 in case of system error.</li>
     *         </ul>
     */
    @GET
    public Response listResources(
            @QueryParam(URLHelper.PARAM_OFFSET) @DefaultValue(URLHelper.DEFAULT_OFFSET)int offset,
            @QueryParam(URLHelper.PARAM_LIMIT)  @DefaultValue(URLHelper.DEFAULT_LIMIT) int limit) {

        List<PersonEntity> data = repository.list(PersonEntity.QUERY_FIND_ALL, offset, limit);
        Long count = repository.count(PersonEntity.QUERY_COUNT_ALL);

        ResponseBuilder builder = Response.ok().entity(data);
        URLHelper.addNavHeaders(builder, uriInfo, new PaginatedModel<>(offset, limit, count, data));

        return builder.build();
    }
}
