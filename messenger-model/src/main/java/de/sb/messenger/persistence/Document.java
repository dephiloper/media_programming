package de.sb.messenger.persistence;

import de.sb.toolbox.bind.JsonProtectedPropertyStrategy;

import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbTransient;
import javax.json.bind.annotation.JsonbVisibility;
import javax.persistence.*;
import javax.validation.constraints.*;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

@Table(name = "Document", schema = "messenger")
@Entity
@PrimaryKeyJoinColumn(name = "documentIdentity")
@JsonbVisibility(JsonProtectedPropertyStrategy.class) // TODO in alle Klassen
public class Document extends BaseEntity {

	private static final byte[] EMPTY_CONTENT = new byte[0];
	private static final byte[] EMPTY_CONTENT_HASH = HashTools.sha256HashCode(EMPTY_CONTENT);

	@Size(min = 32, max = 32)
	@NotNull
	@Column(nullable=false, updatable=true)
	private byte[] contentHash;
	
	@NotNull
	@Column(nullable=false, updatable=true)
	private byte[] content;

	@Size(min = 1, max = 63)
	@NotNull
	@Column(nullable=false, updatable=true)
	private String contentType;
	
	public Document() {
		this.content = EMPTY_CONTENT;
		this.contentType = "application/octet-stream";
		this.contentHash = EMPTY_CONTENT_HASH;
	}
	
	public byte[] scaledImageContent(String fileType, byte[] content, int width, int height) {
		// TODO
		byte[] imageContent = new byte[0];
		return imageContent;
	}

	@JsonbProperty @XmlElement
	public byte[] getContentHash() {
		return this.contentHash;
	}

	@JsonbProperty @XmlAttribute
	public String getContentType() {
		return this.contentType;
	}

	@JsonbTransient @XmlTransient
	public byte[] getContent() {
		return this.content;
	}

	protected void setContentHash(byte[] contentHash) {
		this.contentHash = contentHash;
	}

	public void setContent(byte[] content) {
		this.content = content;
		this.contentHash = HashTools.sha256HashCode(content);
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
}
