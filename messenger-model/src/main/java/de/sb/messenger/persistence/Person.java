package de.sb.messenger.persistence;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.*;

import com.sun.istack.internal.Nullable;

@Table(name = "Person", schema = "messenger")
@Entity
@PrimaryKeyJoinColumn(name = "personIdentity")
public class Person extends BaseEntity{
	
	private static final byte[] DEFAULT_HASH = HashTools.sha256HashCode("default");
	
	// attributes
	
	@Size(min = 1, max = 128)
	@NotNull
	@Column(name = "email")
	private String email;
	
	@Size(min = 32, max = 32)
	@NotNull
	@Column(name = "passwordHash")
	private byte[] passwordHash;
	
	@NotNull
	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(
			schema = "messenger",
			name = "ObservationAssociation",
			joinColumns = @JoinColumn(name = "observedReference"),
			inverseJoinColumns = @JoinColumn(name = "observingReference"),
			uniqueConstraints = @UniqueConstraint(columnNames= {"observedReference", "observingReference"})
	)
	private Set<Person> peopleObserved;
		
	@Embedded
	@Valid
	private Name name;
	
	@Embedded
	@Valid
	private Address address;
	
	@Column(name= "groupAlias", nullable = false, updatable = true)
	@Enumerated(EnumType.STRING)
	private Group group;
	
	@NotNull
	@ManyToMany(mappedBy = "peopleObserved", cascade = {}) // TODO remove refresh ...
	public Set<Person> peopleObserving;
	
	@Nullable
	@OneToMany(mappedBy = "author", cascade = {CascadeType.REMOVE}) // TODO refresh ...
	public Set<Message> messagesAuthored;
	
	@NotNull
	@ManyToOne
	@JoinColumn(name = "avatarReference", referencedColumnName = "documentIdentity")
	public Document avatar; // TODO is public correct?
	
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
	
	public String getEmail() {
		return this.email;
	}
	
	protected void setEmail(String mail) {
		this.email = mail;
	}
	
	public Document getAvatar() {
		return this.avatar;
	}
	
	protected void setAvatar(Document doc) {
		this.avatar = doc;
	}
	
	public Set<Person> getPeopleObserving(){
		return this.peopleObserving;
	}
	
	protected void setPeopleObserving(Set<Person> peopleObserving) {
		this.peopleObserving = peopleObserving;
	}
	
	public Set<Person> getPeopleObserved(){
		return this.peopleObserved;
	}
	
	protected void setPeopleObserved(Set<Person> peopleObserved) {
		this.peopleObserved = peopleObserved;
	}
	
	public byte[] getPasswordHash() {
		return passwordHash;
	}
	
	protected void setPasswortHash(byte[] hash) {
		this.passwordHash = hash;
	}
	
	public Set<Message> getAuthoredMessages(){
		return this.messagesAuthored;
	}
	
	protected void setAuthoredMessages(Set<Message> messagesAuthored){
		this.messagesAuthored = messagesAuthored;
	}
	
	public Group getGroup() {
		return this.group;
	}
	
	public void setGroup(Group group) {
		this.group = group;
	}
	
	public Name getName() {
		return this.name;
	}
	
	public Address getAddress() {
		return this.address;
	}
}