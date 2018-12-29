package de.sb.messenger.persistence;

import de.sb.toolbox.bind.JsonProtectedPropertyStrategy;
import de.sb.toolbox.bind.XmlLongToStringAdapter;

import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbTransient;
import javax.json.bind.annotation.JsonbVisibility;
import javax.persistence.*;
import javax.validation.constraints.*;
import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.HashSet;
import java.util.Set;

@Table(name = "BaseEntity", schema = "messenger")
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "discriminator")
@XmlAccessorType(XmlAccessType.NONE)
@XmlSeeAlso(value = {Person.class, Document.class, Message.class})
@JsonbVisibility(JsonProtectedPropertyStrategy.class)
public class BaseEntity implements Comparable<BaseEntity> {

    /** needs to be insertable otherwise:
        Exception [EclipseLink-46] (Eclipse Persistence Services - 2.6.0.v20150309-bf26070): org.eclipse.persistence.exceptions.DescriptorException
        Exception Description: There should be one non-read-only mapping defined for the primary key field [messenger.BaseEntity.IDENTITY].
        Descriptor: RelationalDescriptor(de.sb.messenger.persistence.BaseEntity --> [DatabaseTable(messenger.BaseEntity)])

        Exception [EclipseLink-46] (Eclipse Persistence Services - 2.6.0.v20150309-bf26070): org.eclipse.persistence.exceptions.DescriptorException
        Exception Description: There should be one non-read-only mapping defined for the primary key field [messenger.BaseEntity.IDENTITY].
        Descriptor: RelationalDescriptor(de.sb.messenger.persistence.Message --> [DatabaseTable(messenger.BaseEntity), DatabaseTable(messenger.Message)])

        Exception [EclipseLink-46] (Eclipse Persistence Services - 2.6.0.v20150309-bf26070): org.eclipse.persistence.exceptions.DescriptorException
        Exception Description: There should be one non-read-only mapping defined for the primary key field [messenger.BaseEntity.IDENTITY].
        Descriptor: RelationalDescriptor(de.sb.messenger.persistence.Person --> [DatabaseTable(messenger.BaseEntity), DatabaseTable(messenger.Person)])

        Exception [EclipseLink-41] (Eclipse Persistence Services - 2.6.0.v20150309-bf26070): org.eclipse.persistence.exceptions.DescriptorException
        Exception Description: A non-read-only mapping must be defined for the sequence number field.
        Descriptor: RelationalDescriptor(de.sb.messenger.persistence.Document --> [DatabaseTable(messenger.BaseEntity), DatabaseTable(messenger.Document)])

        Exception [EclipseLink-41] (Eclipse Persistence Services - 2.6.0.v20150309-bf26070): org.eclipse.persistence.exceptions.DescriptorException
        Exception Description: A non-read-only mapping must be defined for the sequence number field.
        Descriptor: RelationalDescriptor(de.sb.messenger.persistence.Message --> [DatabaseTable(messenger.BaseEntity), DatabaseTable(messenger.Message)])

        Exception [EclipseLink-41] (Eclipse Persistence Services - 2.6.0.v20150309-bf26070): org.eclipse.persistence.exceptions.DescriptorException
        Exception Description: A non-read-only mapping must be defined for the sequence number field.
        Descriptor: RelationalDescriptor(de.sb.messenger.persistence.Person --> [DatabaseTable(messenger.BaseEntity), DatabaseTable(messenger.Person)])
     **/
    @Id
    @NotNull
    @Column(nullable = false, updatable = false, insertable = true)
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

    @OneToMany(mappedBy = "subject", cascade = {CascadeType.REFRESH, CascadeType.MERGE, CascadeType.DETACH, CascadeType.REMOVE})
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
    @XmlID
    @XmlJavaTypeAdapter(type = long.class, value = XmlLongToStringAdapter.class)
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
