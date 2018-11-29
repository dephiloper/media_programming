package de.sb.messenger.persistence;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbTransient;
import javax.json.bind.annotation.JsonbVisibility;
import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.*;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

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
public class Person extends BaseEntity {

    private static final byte[] DEFAULT_HASH = HashTools.sha256HashCode("default");

    // attributes

    @Size(min = 1, max = 128)
    @NotNull
    @Column(nullable = true, updatable = true)
    private String email;

    @Size(min = 32, max = 32)
    @NotNull
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
    @ManyToMany(mappedBy = "peopleObserved", cascade = {CascadeType.REMOVE, CascadeType.REFRESH})
    private Set<Person> peopleObserving;

    @OneToMany(mappedBy = "author", cascade = {CascadeType.REMOVE, CascadeType.REFRESH})
    private Set<Message> messagesAuthored;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "avatarReference", referencedColumnName = "documentIdentity")
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
    @XmlTransient
    public Document getAvatar() {
        return this.avatar;
    }

    public void setAvatar(Document doc) {
        this.avatar = doc;
    }

    @JsonbProperty
    @XmlAttribute
    public long getAvatarReference() {
        if (avatar == null) return 0;
        return this.avatar.getIdentity();
    }

    @JsonbTransient
    @XmlTransient
    public Set<Person> getPeopleObserving() {
        return this.peopleObserving;
    }

    protected void setPeopleObserving(Set<Person> peopleObserving) {
        this.peopleObserving = peopleObserving;
    }

    @JsonbProperty
    @XmlElement
    public HashSet<Long> getPeopleOvservingReference() {
        HashSet<Long> references = new HashSet<>();
        for (Person p : peopleObserved)
            references.add(p.getIdentity());
        return references;
    }

    @JsonbTransient
    @XmlTransient
    public Set<Person> getPeopleObserved() {
        return this.peopleObserved;
    }

    protected void setPeopleObserved(Set<Person> peopleObserved) {
        this.peopleObserved = peopleObserved;
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
    @XmlTransient
    public Set<Message> getMessagesAuthored() {
        return this.messagesAuthored;
    }

    protected void setMessagesAuthored(Set<Message> messagesAuthored) {
        this.messagesAuthored = messagesAuthored;
    }

    @JsonbProperty
    @XmlElement
    public HashSet<Long> getMessagesAuthoredReferences() {
        HashSet<Long> references = new HashSet<>();
        for (Message m : messagesAuthored)
            references.add(m.getIdentity());
        return references;
    }

    @JsonbProperty
    @XmlAttribute
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