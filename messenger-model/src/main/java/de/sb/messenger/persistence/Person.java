package de.sb.messenger.persistence;

import java.lang.Comparable;
import java.util.Set;
import javax.persistence.*;
import javax.validation.constraints.*;

import com.sun.istack.internal.Nullable;

@Table(name = "Person", schema = "SPers")
@Entity
@PrimaryKeyJoinColumn(name = "personIdentity")
public class Person extends BaseEntity{
	
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
	@JoinTable(name = "ObservationAssociation", joinColumns = 
	@JoinColumn(name = "observedReference", referencedColumnName = "personIdentity"), inverseJoinColumns = 
	@JoinColumn(name = "observingReference", referencedColumnName = "personIdentity"))
	private Set<Person> peopleObserved;
		
	@Embedded
	private Name name;
	
	@Embedded
	private Address address;
	
	@Embedded
	private Group group;
	
	@NotNull
	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "ObservationAssociation", joinColumns = 
	@JoinColumn(name = "observingReference", referencedColumnName = "personIdentity"), inverseJoinColumns = 
	@JoinColumn(name = "observedReference", referencedColumnName = "personIdentity"))
	public Set<Person> peopleObserving;
	
	@Nullable
	@OneToMany(cascade = CascadeType.REMOVE)
	public Set<Message> messagesAuthored;
	
	
	@NotNull
	@ManyToOne
	@JoinColumn(name = "avatarReference", referencedColumnName = "documentIdentity")
	public Document avatar;
	
	// constructors
	
	protected Person() {
		
	}
	
	public Person(Name name, Group group, Address address, String email, byte[] passwordHash, Document avatar) {
		this.name = name;
		this.group = group;
		this.address = address;
		this.email = email;
		this.passwordHash = passwordHash;
		this.avatar = avatar;
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
	
	protected void getAvatar(Document doc) {
		this.avatar = doc;
	}
	
	public Set<Person> getObserving(){
		return this.peopleObserving;
	}
	
	protected void setPeopleObserving(Set<Person> peopleObserving) {
		this.peopleObserving = peopleObserving;
	}
	
	public Set<Person> getObserved(){
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
	
	protected void setGroup(Group group) {
		this.group = group;
	}
	
	public Name getName() {
		return this.name;
	}
	
	protected void setName(Name name) {
		this.name = name;
	}
	
	public Address getAddress() {
		return this.address;
	}
	
	protected void setAddress(Address address) {
		this.address = address;
	}

	@Embeddable
	public static class Group implements Comparable<Group>{

		private static final int ADMIN_ID = 0;
		private static final int USER_ID = 1;

		public static final Group ADMIN = new Group(ADMIN_ID);
		public static final Group USER = new Group(USER_ID);

		private int id;

		public Group(int id) {
			this.id = id;
		}

		public Group() {
		}

		private int getId() {
			return this.id;
		}

		public int compareTo(Group group) {
			if (this.id == group.getId())
				return 1;
			return 0;
		}
	}

	@Embeddable
	class Name implements Comparable<Name>{
		
		@Size(min = 1, max = 31)
		@NotNull
		@Column(name = "surname")
		private String family;
		
		@Size(min = 1, max = 31)
		@NotNull
		@Column(name = "forename")
		private String given;

		public Name(String family, String given){
			this.family = family;
			this.given = given;
		}
		
		public int compareTo(Name arg0) {
			// TODO Auto-generated method stub
			return 0;
		} 
	}
	@Embeddable
	class Address implements Comparable<Address> {
		
		@Size(min = 1, max = 63)
		@Nullable
		@Column(name = "street")
		private String street;
		
		@Size(min = 1, max = 15)
		@Nullable
		@Column(name = "postcode")
		private String postcode;
		
		@Size(min = 1, max = 63)
		@NotNull
		@Column(name = "city")
		private String city;
		
		public Address(String street, String postcode, String city){
			this.street = street;
			this.postcode = postcode;
			this.city = city;
		}

		Address() {
		}

		public int compareTo(Address arg0) {
			// TODO Auto-generated method stub
			return 0;
		}
	}
}