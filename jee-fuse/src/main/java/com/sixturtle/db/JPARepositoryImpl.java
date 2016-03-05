package com.sixturtle.db;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sixturtle.exception.InvalidEntityException;
import com.sixturtle.exception.UnknownEntityException;
import com.sixturtle.model.BasicEntity;

/**
 * Generic JPA implementation of {@link JPARepository}.
 *
 * @author Anurag Sharma
 *
 * @param <E>
 *            Entity type
 * @param <L>l
 *            Entity Id type
 */
public abstract class JPARepositoryImpl<E extends BasicEntity<L>, L> implements JPARepository<E, L> {
    protected static Logger log = LoggerFactory.getLogger(JPARepository.class);

    protected static final String HINT_HIBERNATE_CACHEABLE = "org.hibernate.cacheable";

    private Class<E> entityClass;

    private EntityManager em;
    private Validator validator;

    /**
     * Set the {@link EntityManager} or let CDI inject it.
     *
     * @param em
     *            An instance of {@link EntityManager}
     *
     * @see PersistenceContext
     */
    @PersistenceContext
    public void setEntityManager(final EntityManager em) {
        this.em = em;
    }
    /**
     * @return an instance of {@link EntityManager}.
     */
    public EntityManager getEntityManager() {
        return em;
    }

    /**
     * Sets the validator.
     *
     * @param validator
     *            the new validator
     */
    @Inject
    public void setValidator(final Validator validator) {
        this.validator = validator;
    }


    /*
     * (non-Javadoc)
     * @see com.sixturtle.db.JPARepository#create(com.sixturtle.model.BasicEntity)
     */
    @Override
    public E create(final E entity) throws InvalidEntityException {
        if (entity == null) {
            throw new InvalidEntityException();
        }

        Set<ConstraintViolation<E>> violations = validator.validate(entity);
        if (!violations.isEmpty()) {
            throw InvalidEntityException.valueOf("Unable to create entity due to validation errors", violations);
        }
        try {
            em.persist(entity);
        } catch (final Exception e) {
            final String message = String.format("Unexpected error occurred while creating the entity: %s", entity);
            log.error(message, e);
            throw new RuntimeException(message, e);
        }

        return entity;
    }

    /*
     * (non-Javadoc)
     * @see com.sixturtle.db.JPARepository#update(java.lang.Object, com.sixturtle.model.BasicEntity)
     */
    @Override
    public E update(final L id, final E entity) throws InvalidEntityException, UnknownEntityException {
        if (entity == null) {
            throw new InvalidEntityException();
        }

        E managed = find(id);
        if (managed == null) {
            throw new UnknownEntityException("Unable to update because the entity was not found by Id: " + id);
        } else {
            final Set<ConstraintViolation<E>> violations = validator.validate(entity);
            if (!violations.isEmpty()) {
                throw InvalidEntityException.valueOf("Unable to update entity due to validation errors", violations);
            }
            try {
                /*
                 * Copy property values from the origin bean to the destination
                 * bean for all cases where the property names are the same.
                 */
                BeanUtils.copyProperties(managed /* destination */, entity /* origin */);
                em.persist(managed);
            } catch (Exception e) {
                final String message = String.format("Unexpected error occurred while updating the entity: %s", entity);
                throw new RuntimeException(message, e);
            }
        }
        return managed;
    }

    /*
     * (non-Javadoc)
     * @see com.sixturtle.db.JPARepository#delete(java.lang.Object)
     */
    @Override
    public void delete(final L id) throws UnknownEntityException {
        E managed = find(id);

        if (managed == null) {
            throw new UnknownEntityException("Unable to delete because the entity was not found by Id: " + id);
        } else {
            try {
                em.remove(managed);
            } catch (final Exception e) {
                final String message = String.format("Unexpected error occurred while deleting the entity with id: %s", id);
                throw new RuntimeException(message, e);
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see com.sixturtle.db.JPARepository#count(java.lang.String)
     */
    @Override
    public Long count(final String queryName) {
        Query jpqlQuery = getEntityManager().createNamedQuery(queryName);
        return (long) jpqlQuery.setHint(HINT_HIBERNATE_CACHEABLE, true).getSingleResult();
    }

    /*
     * (non-Javadoc)
     * @see com.sixturtle.controller.BasicRepository#list(int, int)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<E> list(final String queryName, final int offset, final int limit) {
        Query jpqlQuery = getEntityManager().createNamedQuery(queryName);

        return jpqlQuery.setFirstResult(offset).setMaxResults(limit).setHint(HINT_HIBERNATE_CACHEABLE, true)
                .getResultList();
    }

    /**
     * Finds entity by determining the generic class name.
     *
     * @param id
     *            The entity Id
     * @return {@link BasicEntity} type
     */
   public E find(final L id) {
       E entity = null;
       try {
           entity = em.find(getEntityClass(), id);
       } catch (final EntityNotFoundException e) {
           log.debug("Entity not found by Id: {}", id);
       } catch (final Exception e) {
           final String message = String.format("Unexpected error while looking for an entity by Id: %s", id);
           log.error(message, e);
           throw new RuntimeException(message, e);
       }
       return entity;
   }

    /**
     * Get the class of the generic type E.
     *
     * @return the class of the generic type E
     */
    @SuppressWarnings("unchecked")
    private Class<E> getEntityClass() {
        if (entityClass == null) {
            final Type type = getClass().getGenericSuperclass();
            if (type instanceof ParameterizedType) {
                final ParameterizedType paramType = (ParameterizedType) type;
                entityClass = (Class<E>) paramType.getActualTypeArguments()[0];
            } else {
                throw new IllegalArgumentException("Could not guess entity class by reflection");
            }
        }
        return entityClass;
    }
}
