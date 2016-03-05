package com.sixturtle.model;

import java.io.Serializable;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;


/**
 * Person entity in database.
 *
 * @author Anurag Sharma
 */
@Entity
@Cacheable
@Table(name = "PERSON")
@SequenceGenerator(name = "PERSON_ID_GENERATOR", sequenceName = "PERSON_ID_SEQ")
@NamedQueries({
    @NamedQuery(
            name = PersonEntity.QUERY_FIND_ALL,
            query = "SELECT p FROM PersonEntity p"),
    @NamedQuery(
            name = PersonEntity.QUERY_COUNT_ALL,
            query = "SELECT COUNT(1) FROM PersonEntity p")
})
public class PersonEntity implements BasicEntity<Long>, Serializable {
    private static final long serialVersionUID = 8990255112980205427L;
    public static final String QUERY_FIND_ALL  = "PersonEntity.findAll";
    public static final String QUERY_COUNT_ALL = "PersonEntity.countAll";

    @Id
    @Column(name = "ID")
    @GeneratedValue(generator = "PERSON_ID_GENERATOR")
    private Long id;

    @NotNull @Size(min = 1, max = 255)
    @Column(name = "FIRST_NAME", length = 255)
    private String firstName;

    @Column(name = "MIDDLE_NAME", length = 255)
    private String middleName;

    @NotNull @Size(min = 1, max = 255)
    @Column(name = "LAST_NAME", length = 255)
    private String lastName;

    @NotNull @Size(min = 1, max = 255)
    @Column(name = "EMAIL", nullable = false, length = 255, unique = true)
    private String email;

    @NotNull @Size(min = 1, max = 20)
    @Column(name = "PHONE", length = 20)
    private String phone;

    @Version
    private Long version;


    /**
     * Default constructor
     */
    public PersonEntity() {
    }

    /**
     * Creates a {@link PersonEntity} object using following parameters.
     *
     * @param firstName
     *            The first name
     * @param middleName
     *            The middle name
     * @param lastName
     *            The last name
     * @param email
     *            The email
     * @param phone
     *            The phone
     */
    public PersonEntity(
            final String firstName,
            final String middleName,
            final String lastName,
            final String email,
            final String phone) {
        this.firstName  = firstName;
        this.middleName = middleName;
        this.lastName   = lastName;
        this.email      = email;
        this.phone      = phone;
    }

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }
    /**
     * @return the firstName
     */
    public String getFirstName() {
        return firstName;
    }
    /**
     * @param firstName the firstName to set
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    /**
     * @return the middleName
     */
    public String getMiddleName() {
        return middleName;
    }
    /**
     * @param middleName the middleName to set
     */
    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }
    /**
     * @return the lastName
     */
    public String getLastName() {
        return lastName;
    }
    /**
     * @param lastName the lastName to set
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    /**
     * @return the email
     */
    public String getEmail() {
        return email;
    }
    /**
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }
    /**
     * @return the phone
     */
    public String getPhone() {
        return phone;
    }
    /**
     * @param phone the phone to set
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("Person {")
               .append("id:").append(id).append(",")
               .append("firstName:").append(firstName).append(",")
               .append("middleName:").append(middleName).append(",")
               .append("lastName:").append(lastName).append(",")
               .append("email:").append(email).append(",")
               .append("phone:").append(phone)
               .append('}');

        return builder.toString();
    }
}
