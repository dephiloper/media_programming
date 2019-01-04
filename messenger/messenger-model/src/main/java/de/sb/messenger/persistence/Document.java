package de.sb.messenger.persistence;

import de.sb.toolbox.bind.JsonProtectedPropertyStrategy;

import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbTransient;
import javax.json.bind.annotation.JsonbVisibility;
import javax.persistence.*;
import javax.validation.constraints.*;
import javax.xml.bind.annotation.*;

@Table(name = "Document", schema = "messenger")
@Entity
@PrimaryKeyJoinColumn(name = "documentIdentity")
@JsonbVisibility(JsonProtectedPropertyStrategy.class)
@XmlRootElement
@XmlType
public class Document extends BaseEntity {

    private static final byte[] EMPTY_CONTENT = new byte[0];
    private static final byte[] EMPTY_CONTENT_HASH = HashTools.sha256HashCode(EMPTY_CONTENT);

    @Size(min = 32, max = 32)
    @NotNull
    @NotEmpty
    @Column(nullable = false, updatable = true)
    private byte[] contentHash;

    @Size(min = 1)
    @NotNull
    @NotEmpty
    @Column(nullable = false, updatable = true)
    private byte[] content;

    @Size(min = 1, max = 63)
    @NotNull
    @NotEmpty
    @Column(nullable = false, updatable = true)
    private String contentType;

    public Document() {
        this.content = EMPTY_CONTENT;
        this.contentType = "application/octet-stream";
        this.contentHash = EMPTY_CONTENT_HASH;
    }

    public static byte[] scaledImageContent(String fileType, byte[] content, int width, int height) {
        // TODO load from moodle
        byte[] imageContent = new byte[0];
        return imageContent;
    }

    @JsonbProperty
    @XmlElement
    public byte[] getContentHash() {
        return this.contentHash;
    }

    protected void setContentHash(byte[] contentHash) {
        this.contentHash = contentHash;
    }

    @JsonbProperty
    @XmlAttribute
    public String getContentType() {
        return this.contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    @JsonbTransient
    @XmlTransient
    public byte[] getContent() {
        return this.content;
    }

    public void setContent(byte[] content) {
        this.content = content;
        this.contentHash = HashTools.sha256HashCode(content);
    }
}
