package de.sb.messenger.persistence;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbTransient;
import javax.json.bind.annotation.JsonbVisibility;
import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.*;
import javax.xml.bind.annotation.*;

import de.sb.toolbox.bind.JsonProtectedPropertyStrategy;

/*

        ___
    . -^   `--,
   /# =========`-_
  /# (--====___====\
 /#   .- --.  . --.|
/##   |  * ) (   * ),
|##   \    /\ \   / |
|###   ---   \ ---  |
|####      ___)    #|
|######           ##|
 \##### ---------- /
  \####           (
   `\###          |
     \###         |
      \##        |
       \###.    .)
        `======/

        SHOW ME WHAT YOU GOT!

 */

@Table(name = "Person", schema = "messenger")
@Entity
@PrimaryKeyJoinColumn(name = "personIdentity")
@JsonbVisibility(JsonProtectedPropertyStrategy.class)
@XmlRootElement
@XmlType
public class Person extends BaseEntity {

    private static final byte[] DEFAULT_HASH = HashTools.sha256HashCode("default");

    public static final Comparator<Person> PERSON_COMPARATOR = Comparator.comparing(Person::getName).thenComparing(Person::getEmail);

    // attributes

    @Size(min = 1, max = 128)
    @NotEmpty
    @Column(nullable = true, updatable = true)
    @Email
    private String email;

    @Size(min = 32, max = 32)
    @NotNull
    @NotEmpty
    @Column(nullable = false, updatable = true)
    private byte[] passwordHash;

    @NotNull
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            schema = "messenger",
            name = "ObservationAssociation",
            joinColumns = @JoinColumn(name = "observedReference"),
            inverseJoinColumns = @JoinColumn(name = "observingReference"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"observedReference", "observingReference"})
    )
    private Set<Person> peopleObserved;

    @Embedded
    @Valid
    private Name name;

    @Embedded
    @Valid
    private Address address;

    @Column(name = "groupAlias", nullable = false, updatable = true)
    @Enumerated(EnumType.STRING)
    private Group group;

    @NotNull
    @ManyToMany(mappedBy = "peopleObserved", cascade = {CascadeType.REFRESH, CascadeType.MERGE, CascadeType.DETACH, CascadeType.REMOVE})
    private Set<Person> peopleObserving;

    @OneToMany(mappedBy = "author", cascade = {CascadeType.REFRESH, CascadeType.MERGE, CascadeType.DETACH, CascadeType.REMOVE})
    private Set<Message> messagesAuthored;


    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "avatarReference", referencedColumnName = "documentIdentity", nullable = false, updatable = true) // TODO add to all join collouns
    private Document avatar;

    // constructors

    protected Person() {
        this(null);
    }

    public Person(Document avatar) {
        this.name = new Name();
        this.group = Group.USER;
        this.address = new Address();
        this.passwordHash = DEFAULT_HASH;
        this.avatar = avatar;
        this.messagesAuthored = Collections.emptySet();
        this.peopleObserving = Collections.emptySet();
        this.peopleObserved = new HashSet<>();
    }

    // methods

    @JsonbProperty
    @XmlAttribute
    public String getEmail() {
        return this.email;
    }

    public void setEmail(String mail) {
        this.email = mail;
    }

    @JsonbTransient
    @XmlAttribute
    @XmlIDREF
    public Document getAvatar() {
        return this.avatar;
    }

    public void setAvatar(Document doc) {
        this.avatar = doc;
    }

    @JsonbProperty
    @XmlTransient
    public long getAvatarReference() {
        if (avatar == null) return 0;
        return avatar.getIdentity();
    }

    @JsonbTransient
    @XmlElement
    @XmlIDREF
    public Set<Person> getPeopleObserving() {
        return this.peopleObserving;
    }

    protected void setPeopleObserving(Set<Person> peopleObserving) {
        this.peopleObserving = peopleObserving;
    }

    @JsonbProperty
    @XmlTransient
    public long[] getPeopleObservingReferences() {
        return peopleObserving.stream().mapToLong(Person::getIdentity).sorted().toArray();

    }

    @JsonbTransient
    @XmlElement
    @XmlIDREF
    public Set<Person> getPeopleObserved() {
        return this.peopleObserved;
    }

    protected void setPeopleObserved(Set<Person> peopleObserved) {
        this.peopleObserved = peopleObserved;
    }

    @JsonbProperty
    @XmlTransient
    public long[] getPeopleObservedReferences() {
        return peopleObserved.stream().mapToLong(Person::getIdentity).sorted().toArray();
    }

    @JsonbTransient
    @XmlTransient
    public byte[] getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(byte[] hash) {
        this.passwordHash = hash;
    }

    @JsonbTransient
    @XmlElement
    @XmlIDREF
    public Set<Message> getMessagesAuthored() {
        return this.messagesAuthored;
    }

    protected void setMessagesAuthored(Set<Message> messagesAuthored) {
        this.messagesAuthored = messagesAuthored;
    }


    @JsonbProperty
    @XmlTransient
    public long[] getMessagesAuthoredReferences() {
        return messagesAuthored.stream().mapToLong(Message::getIdentity).sorted().toArray();
    }

    @JsonbProperty
    @XmlElement
    public Group getGroup() {
        return this.group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    @JsonbProperty
    @XmlElement
    public Name getName() {
        return this.name;
    }

    @JsonbProperty
    @XmlElement
    public Address getAddress() {
        return this.address;
    }
}