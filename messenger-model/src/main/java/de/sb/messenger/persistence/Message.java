package de.sb.messenger.persistence;

import de.sb.toolbox.bind.JsonProtectedPropertyStrategy;

import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbTransient;
import javax.json.bind.annotation.JsonbVisibility;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

@Table(name = "Message", schema = "messenger")
@Entity
@PrimaryKeyJoinColumn(name = "messageIdentity")
@JsonbVisibility(JsonProtectedPropertyStrategy.class)
public class Message extends BaseEntity {

	@Column(name = "body")
	private String body;
	
	@ManyToOne
	@JoinColumn(name = "authorReference", nullable=false, updatable=false, insertable=true)
	private Person author;
	
	@ManyToOne
	@JoinColumn(name = "subjectReference", nullable=false, updatable=false, insertable=true)
	private BaseEntity subject;
	
	protected Message() {
		this(null, null);
	}
	
	public Message(Person author, BaseEntity subject) {
		this.author = author;
		this.subject = subject;
	}

	@JsonbProperty @XmlAttribute
	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	@JsonbTransient @XmlTransient
	public Person getAuthor() {
		return author;
	}

	protected void setAuthor(Person author) {
		this.author = author;
	}

	@JsonbTransient @XmlTransient
	public BaseEntity getSubject() {
		return subject;
	}

	// TODO getAuthorReference and getSubjectReference, wenn Value=null dann Reference=0

	protected void setSubject(BaseEntity subject) {
		this.subject = subject;
	}
}
