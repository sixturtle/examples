package com.sixturtle.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;


/**
 *
 * Role entity in database. A user can have more than one role.
 *
 * @author Anurag Sharma
 */
@Entity
@Table(name = "ROLE")
@SequenceGenerator(name = "ROLE_ID_GENERATOR", sequenceName = "ROLE_ID_SEQ")
@NamedQueries({
    @NamedQuery(
            name = RoleEntity.QUERY_FIND_BY_ROLE_TYPE,
            query = "SELECT r FROM RoleEntity r WHERE r.name = :name"),
    @NamedQuery(
            name = RoleEntity.QUERY_GET_ALL_ROLES,
            query = "FROM RoleEntity r")
})
public class RoleEntity implements BasicEntity<Long>, Serializable {
    private static final long serialVersionUID = 6997056091436404528L;

    public static final String QUERY_FIND_BY_ROLE_TYPE   = "RoleEntity.findByRoleType";
    public static final String QUERY_GET_ALL_ROLES   = "RoleEntity.getAllRoles";

    @Id
    @Column(name = "ID")
    @GeneratedValue(generator = "ROLE_ID_GENERATOR")
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "ROLE_NAME", nullable = false, length = 255, unique = true)
    private RoleType name = RoleType.USER;

    @Column(name = "ROLE_DESCRIPTION", length = 1024)
    private String description;

    @Version
    private Long version;

    /**
     * Gets id.
     *
     * @return the id
     */
    public Long getId() {
        return id;
    }
    /**
     * @return the name
     */
    public RoleType getName() {
        return name;
    }
    /**
     * @param name the name to set
     */
    public void setName(RoleType name) {
        this.name = name;
    }
    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }
    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("Role {");

        builder.append("id:").append(id).append(",")
               .append("name:").append(name).append(",")
               .append("description:").append(description)
               .append('}');

        return builder.toString();
    }
}
