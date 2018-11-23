package de.sb.messenger.persistence;

import de.sb.toolbox.bind.JsonProtectedPropertyStrategy;

import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbTransient;
import javax.json.bind.annotation.JsonbVisibility;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import java.util.HashSet;
import java.util.Set;

@Table(name = "BaseEntity", schema = "messenger")
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "discriminator")
@XmlAccessorType(XmlAccessType.NONE)
@JsonbVisibility(JsonProtectedPropertyStrategy.class)
public class BaseEntity implements Comparable<BaseEntity> {

    @Id
    @NotNull
    @Column(nullable = false, updatable = false, insertable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long identity;

    @NotNull
    @Positive
    @Column(nullable = false, updatable = false, insertable = false)
    private int version;

    @NotNull
    @PositiveOrZero
    @Column(nullable = false, updatable = false, insertable = false)
    private long creationTimestamp;

    @OneToMany(mappedBy = "subject", cascade = {CascadeType.REMOVE, CascadeType.REFRESH})
    private Set<Message> messagesCaused;

    protected BaseEntity() {
        this(0, 0, 0, new HashSet<>());
    }

    public BaseEntity(long identity, long creationTimestamp, int version, Set<Message> messagesCaused) {
        this.identity = identity;
        this.creationTimestamp = creationTimestamp;
        this.version = version;
        this.messagesCaused = messagesCaused;
    }

    @JsonbProperty
    @XmlAttribute
    public long getIdentity() {
        return this.identity;
    }

    protected void setIdentity(long identity) {
        this.identity = identity;
    }

    @JsonbProperty
    @XmlAttribute
    public int getVersion() {
        return this.version;
    }

    protected void setVersion(int version) {
        this.version = version;
    }

    @JsonbProperty
    @XmlAttribute
    public long getCreationTimestamp() {
        return this.creationTimestamp;
    }

    protected void setCreationTimestamp(long creationTimestamp) {
        this.creationTimestamp = creationTimestamp;
    }

    @JsonbTransient
    @XmlTransient
    public Set<Message> getMessagesCaused() {
        return messagesCaused;
    }

    protected void setMessagesCaused(Set<Message> messagesCaused) {
        this.messagesCaused = messagesCaused;
    }

    @Override
    public String toString() {
        return this.getClass().getName() + "@" + this.identity;
    }

    @Override
    public int compareTo(BaseEntity base) {
        return Long.compare(this.identity, base.identity);
    }
}
