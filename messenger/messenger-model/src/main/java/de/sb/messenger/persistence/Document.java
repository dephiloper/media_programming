package de.sb.messenger.persistence;

import de.sb.toolbox.bind.JsonProtectedPropertyStrategy;

import javax.imageio.ImageIO;
import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbTransient;
import javax.json.bind.annotation.JsonbVisibility;
import javax.persistence.*;
import javax.validation.constraints.*;
import javax.xml.bind.annotation.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

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
    @NotNull(message = "little fucker")
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

    public static byte[] scaledImageContent(String fileType, byte[] content, int width, int height) throws IOException {
        try (ByteArrayInputStream in = new ByteArrayInputStream(content); ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            BufferedImage img = ImageIO.read(in);
            if (height == 0) height = (width * img.getHeight()) / img.getWidth();
            if (width == 0) width = (height * img.getWidth()) / img.getHeight();

            Image scaledImage = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            BufferedImage imageBuff = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            imageBuff.getGraphics().drawImage(scaledImage, 0, 0, new Color(0, 0, 0), null);

            ImageIO.write(imageBuff, fileType, buffer);
            return buffer.toByteArray();
        }
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
