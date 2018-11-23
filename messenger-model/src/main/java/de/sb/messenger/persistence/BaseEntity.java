package de.sb.messenger.persistence;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;
import javax.validation.constraints.*;

import de.sb.messenger.persistence.Message;

@Table(name = "BaseEntity", schema = "messenger")
@Entity
@Inheritance(strategy=InheritanceType.JOINED)
@DiscriminatorColumn(name="discriminator")
public class BaseEntity implements Comparable<BaseEntity>{
	
	@Id
	@NotNull
	@Column(name = "identity")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long identity;
	
	@NotNull
	@Positive
	@Column(name = "version")
	private int version;
	
	@NotNull
	@PositiveOrZero
	@Column(name = "creationTimestamp")
	private long creationTimestamp;

	@OneToMany
	@JoinColumn()
	public Set<Message> messagesCaused;
	
	protected BaseEntity() {
		this(0, 0, 0, new HashSet<Message>());
	}
	
	public BaseEntity(long identity, long creationTimestamp, int version, Set<Message> messagesCaused) {
		this.identity = identity;
		this.creationTimestamp = creationTimestamp;
		this.version = version;
		this.messagesCaused = messagesCaused;
	}
	
	public long getIdentity() {
		return this.identity;
	}

	protected void setIdentity(long identity) {
		this.identity = identity;
	}
	
	public int getVersion() {
		return this.version;
	} 
	
	protected void setVersion(int version) {
		this.version = version;
	}
	
	public long getCreationTimestamp() {
		return this.creationTimestamp;
	}
	
	protected void setCreationTimestamp(long creationTimestamp) {
		this.creationTimestamp = creationTimestamp;
	}
	
	public Set<Message> getMessagesCaused(){
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
