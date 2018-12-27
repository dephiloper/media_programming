package de.sb.messenger.persistence;

import de.sb.toolbox.bind.JsonProtectedPropertyStrategy;

import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbTransient;
import javax.json.bind.annotation.JsonbVisibility;
import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.*;

@Table(name = "Message", schema = "messenger")
@Entity
@PrimaryKeyJoinColumn(name = "messageIdentity")
@JsonbVisibility(JsonProtectedPropertyStrategy.class)
@XmlRootElement
@XmlType
public class Message extends BaseEntity {

	@Column(nullable = true, updatable = true)
	@Size(min = 1, max = 2^13-3)
	@NotEmpty
	private String body;
	
	@ManyToOne
	@JoinColumn(name = "authorReference", nullable=false, updatable=false)
	private Person author;
	
	@ManyToOne
	@JoinColumn(name = "subjectReference", nullable=false, updatable=false)
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

	@JsonbTransient
	@XmlElement
	@XmlIDREF
	public Person getAuthor() {
		return author;
	}

	protected void setAuthor(Person author) {
		this.author = author;
	}

	// TODO S.14-29: Definiert dazu folgende zusätzliche Methoden, mit ?aufsteigender Reihenfolge bei Mengenergebnissen?
    // Collections sollen sortiert werden, bevor sie returned werden.
	@JsonbProperty
	@XmlTransient
	public long getAuthorReference() {
		if (author == null) return 0;
		return author.getIdentity();
	}

	@JsonbTransient
	@XmlElement
	@XmlIDREF
	public BaseEntity getSubject() {
		return subject;
	}

	protected void setSubject(BaseEntity subject) {
		this.subject = subject;
	}

	@JsonbProperty
	@XmlTransient
	public long getSubjectReference() {
		if (subject == null) return 0;
		return subject.getIdentity();
	}
}
