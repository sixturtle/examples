package com.sixturtle.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Super class of managed entity beans to enforce primary key type. This would
 * help implement a generic repository.
 *
 * @author Anurag Sharma
 * @param <E>
 *            The data type of primary key
 */
@XmlRootElement
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public interface BasicEntity<E> extends Serializable {
    /**
     * @return The primary key of the entity
     */
    E getId();
}
