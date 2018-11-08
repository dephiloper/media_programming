package de.sb.messenger.persistence;

import javax.persistence.*;

@Table(name = "Message", schema = "messenger")
@Entity
@PrimaryKeyJoinColumn(name = "messageIdentity")
public class Message extends BaseEntity{

	@Column(name = "body")
	private String body;
	
	@ManyToOne
	@JoinColumn(name = "authorReference", referencedColumnName = "personIdentity")
	private Person author;
	
	@ManyToOne
	@JoinColumn(name = "subjectReference", referencedColumnName = "identity")
	private BaseEntity subject;
	
	protected Message() {
		this(null, null, null);
	}
	
	public Message(String body, Person author, BaseEntity subject) {
		this.body = body;
		this.author = author;
		this.subject = subject;
	}
	
	public String getBody() {
		return body;
	}

	protected void setBody(String body) {
		this.body = body;
	}

	public Person getAuthor() {
		return author;
	}

	protected void setAuthor(Person author) {
		this.author = author;
	}

	public BaseEntity getSubject() {
		return subject;
	}

	protected void setSubject(BaseEntity subject) {
		this.subject = subject;
	}
}
