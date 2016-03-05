package com.sixturtle.db;

import java.util.List;

import javax.persistence.EntityManager;

import com.sixturtle.exception.InvalidEntityException;
import com.sixturtle.exception.UnknownEntityException;
import com.sixturtle.model.BasicEntity;

/**
 * Represents a generic CRUD interface for database entities.
 *
 * @author Anurag Sharma
 *
 * @param <E> The Entity Type
 * @param <L> The Entity ID Type
 */
public interface JPARepository<E extends BasicEntity<L>, L> {
    /**
     * @return {@link EntityManager}
     */
    EntityManager getEntityManager();

    /**
     * @param entity
     *            {@link BasicEntity}
     * @return E entity created
     * @throws InvalidEntityException
     *             when validation fails
     */
    E create(final E entity) throws InvalidEntityException;

    /**
     * @param entityId
     *            entityId
     * @param entity
     *            {@link BasicEntity}
     * @return E entity updated
     * @throws UnknownEntityException
     *             when entity is not found by entityId
     * @throws InvalidEntityException
     *             when validation fails
     */
    E update(final L entityId, final E entity) throws InvalidEntityException, UnknownEntityException;

    /**
     * @param entityId
     *            entityId
     * @throws UnknownEntityException
     *             when entity is not found by entityId
     */
    void delete(final L entityId) throws UnknownEntityException;

    /**
     * @param entityId
     *            entityId
     * @return E The {@link BasicEntity} type
     * @throws UnknownEntityException
     *             when entity is not found by entityId
     */
    E find(final L entityId) throws UnknownEntityException;

    /**
     * @param query
     *            The named query name
     * @return Long count
     */
    Long count(final String query);

    /**
     * @param query
     *            The named query name
     * @param offset
     *            offset
     * @param limit
     *            limit
     * @return {@link List} of {@link BasicEntity} type
     */
    List<E> list(final String query, final int offset, final int limit);
}