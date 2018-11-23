package de.sb.messenger.persistence;

import javax.json.bind.annotation.JsonbProperty;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAttribute;

import com.sun.istack.internal.Nullable;

import java.util.Comparator;

@Embeddable class Address implements Comparable<Address> {
	private static final Comparator<Address> ADDRESS_COMPARATOR = Comparator.comparing(Address::getPostcode)
			.thenComparing(Address::getCity)
			.thenComparing(Address::getStreet);

	@Size(min = 1, max = 63)
	@Nullable
	@Column(nullable = false, updatable = true)
	private String street;
	
	@Size(min = 1, max = 15)
	@Nullable
	@Column(nullable = false, updatable = true)
	private String postcode;
	
	@Size(min = 1, max = 63)
	@NotNull
	@Column(nullable=false, updatable = true)
	private String city;

	@JsonbProperty @XmlAttribute
	public String getStreet() {
		return street;
	}

    public void setStreet(String street) {
        this.street = street;
    }

	@JsonbProperty @XmlAttribute
	public String getPostcode() {
		return postcode;
	}

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

	@JsonbProperty @XmlAttribute
	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}
	
	@Override
	public int compareTo(Address other) {
		return ADDRESS_COMPARATOR.compare(this, other);
	}
}