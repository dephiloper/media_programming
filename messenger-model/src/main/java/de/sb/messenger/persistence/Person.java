package de.sb.messenger.persistence;

import java.util.Collections;
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

    // attributes

    @Size(min = 1, max = 128)
    @NotNull
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


    // TODO auf jeden Fall REFRESH, MERGE, und DETACH, falls in der Datenbank so definiert auch REMOVE: die DB definiert hier restricted remove aber es gibt keine OneToMany gegenseite.. wo definiert man es dann?
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
    @XmlAttribute
    @XmlIDREF
    public Document getAvatar() {
        return this.avatar;
    }

    public void setAvatar(Document doc) {
        this.avatar = doc;
    }

    /*
        TODO pls doublecheck!
        Die Referenz-Getter für Relationen sollten dann mit @XmlTransient annotiert werden, während die entsprechenden
        Entity-Getter @XmlAttribute und @XmlIDREF erhalten. Beachtet aber dass der Hauptvorteil dieser Vorgehensweise
        (Marshaling von Objekten mit Graph- statt Baum-Topologie) erst zum Tragen käme sobald ein passendes XML-Schema
        definiert und eingesetzt würde
     */

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
    public HashSet<Long> getPeopleObservingReferences() {
        // magic lambda magic is magic
        // map returns a stream which contains the results of the function Person.getIdentity() of all the set elements
        // collect allows repacking elements into a specified data structure
        // Collectors.toList or Collectors.toSet does not specify a particular implementation therefore toCollection
        // is used w/ the command to use the HashSet implementation pretty neat!
        // https://stackoverflow.com/a/30082600/10547035 <3
        return peopleObserving.stream().map(Person::getIdentity).collect(Collectors.toCollection(HashSet::new));

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
    public HashSet<Long> getPeopleObservedReferences() {
        return peopleObserved.stream().map(Person::getIdentity).collect(Collectors.toCollection(HashSet::new));
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


    // TODO in der Beschreibung taucht diese Methode nicht auf
    @JsonbProperty
    @XmlTransient
    public HashSet<Long> getMessagesAuthoredReferences() {
        return messagesAuthored.stream().map(Message::getIdentity).collect(Collectors.toCollection(HashSet::new));
    }

    @JsonbProperty
    @XmlAttribute // TODO xml element?
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