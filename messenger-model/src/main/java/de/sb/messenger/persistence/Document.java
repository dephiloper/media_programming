package de.sb.messenger.persistence;

import javax.persistence.*;
import javax.validation.constraints.*;

@Table(name = "Document", schema = "messenger")
@Entity
@PrimaryKeyJoinColumn(name = "documentIdentity")
public class Document extends BaseEntity {
	
	@Size(min = 32, max = 32)
	@NotNull
	@Column(name = "contentHash")
	private byte[] contentHash;
	
	@NotNull
	@Column(name = "content")
	private byte[] content;

	@Size(min = 1, max = 63)
	@NotNull
	@Column(name = "contentType")
	private String contentType;
	
	protected Document() {
		this(null, null, null);
	}
	
	public Document(byte[] contentHash, byte[] content, String contentType) {
		this.contentHash = contentHash;
		this.content = content;
		this.contentType = contentType;
	}
	
	public byte[] scaledImageContent(String fileType, byte[] content, int width, int height) {
		//TODO
		byte[] imageContent = new byte[0];
		return imageContent;
	}
	
	public byte[] getContentHash() {
		return this.contentHash;
	}
	
	public String getContentType() {
		return this.contentType;
	}
	
	public byte[] getContent() {
		return this.content;
	}

	protected void setContentHash(byte[] contentHash) {
		this.contentHash = contentHash;
	}

	protected void setContent(byte[] content) {
		this.content = content;
	}

	protected void setContentType(String contentType) {
		this.contentType = contentType;
	}
	
}
