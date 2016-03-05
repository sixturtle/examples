package com.sixturtle.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Version;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;


/**
 * A database entity representing a tenant user.
 *
 * @author Anurag Sharma
 */
@Entity
@Cacheable
@Table(name = "USER")
@SequenceGenerator(name = "USER_ID_GENERATOR", sequenceName = "USER_ID_SEQ")
@NamedQueries({
    @NamedQuery(
            name = UserEntity.QUERY_COUNT_ALL,
            query = "SELECT COUNT(1) FROM UserEntity u"),
    @NamedQuery(
            name = UserEntity.QUERY_FIND_ALL,
            query = "SELECT u FROM UserEntity u")
})
public class UserEntity implements BasicEntity<Long>, Serializable {
    private static final long serialVersionUID = 5906694206059291913L;

    public static final String QUERY_COUNT_ALL = "UserEntity.countAll";
    public static final String QUERY_FIND_ALL = "UserEntity.findAll";


    @Id
    @Column(name = "ID")
    @GeneratedValue(generator = "USER_ID_GENERATOR")
    private Long id;

    @NotNull @Valid
    @OneToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name  = "PERSON_ID", nullable = false, unique = true, referencedColumnName = "ID")
    private PersonEntity person;

    @NotNull @Valid
    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JoinTable(
            name = "USER_ROLE",
            joinColumns = @JoinColumn(name = "USER_ID", referencedColumnName = "ID"),
            inverseJoinColumns = @JoinColumn(name = "ROLE_ID", referencedColumnName = "ID"))
    private Set<RoleEntity> roles = new HashSet<RoleEntity>();

    @Column(name = "ACTIVE", nullable = false)
    private Boolean active = Boolean.TRUE;

    @Version
    private Long version;


    /**
     * Gets the id.
     *
     * @return the id
     */
    @Override
    public Long getId() {
        return id;
    }
    /**
     * Gets the person.
     *
     * @return the person
     */
    public PersonEntity getPerson() {
        return person;
    }
    /**
     * Sets the person.
     *
     * @param person
     *            the new person
     */
    public void setPerson(final PersonEntity person) {
        this.person = person;
    }
    /**
     * Gets roles.
     *
     * @return the roles
     */
    public Set<RoleEntity> getRoles() {
        return roles;
    }
    /**
     * Sets roles.
     *
     * @param roles the roles
     */
    public void setRoles(final Set<RoleEntity> roles) {
        this.roles = roles;
    }

    /**
     * @return the active
     */
    public Boolean getActive() {
        return active;
    }
    /**
     * @param active the active to set
     */
    public void setActive(Boolean active) {
        this.active = active;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("User {")
               .append("id:").append(id).append(",")
               .append("person:").append(person).append(",")
               .append("roles:").append(roles).append(",")
               .append("active:").append(active).append(",")
               .append('}');

        return builder.toString();
    }
}
