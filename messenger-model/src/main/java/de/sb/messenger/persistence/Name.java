package de.sb.messenger.persistence;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Embeddable
public class Name implements Comparable<Name>{
	@Size(min = 1, max = 31)
	@NotNull
	@Column(name = "surname", nullable=false, updatable= true)
	private String family;
	
	@Size(min = 1, max = 31)
	@NotNull
	@Column(name = "forename", nullable=false, updatable= true) // TODO overdo insertable when updatable = false
	private String given;

	public String getFamily() {
		return family;
	}

	public void setFamily(String family) {
		this.family = family;
	}

	public String getGiven() {
		return given;
	}

	public void setGiven(String given) {
		this.given = given;
	}

	@Override
	public int compareTo(Name arg0) {
		String name = family + given;
		String nameComp = arg0.family + arg0.given;
		for (int i = 0; i < name.length(); i++) {
			if(name.charAt(i) < nameComp.charAt(i))
				return -1;
			if(name.charAt(i) > nameComp.charAt(i))
				return 1;
		}

		if (name.length()< nameComp.length())
			return -1;
		if (name.length()> nameComp.length())
			return 1;
		return 0;
		
		// possible with String.compareToString
		
	} 
}