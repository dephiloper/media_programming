package de.sb.messenger.persistence;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.sun.istack.internal.Nullable;

@Embeddable
class Address implements Comparable<Address> {
    @Size(min = 1, max = 63)
    @Nullable
    @Column(name = "street", nullable = false, updatable = true)
    private String street;

    @Size(min = 1, max = 15)
    @Nullable
    @Column(name = "postcode", nullable = false, updatable = true)
    private String postcode;

    @Size(min = 1, max = 63)
    @NotNull
    @Column(name = "city", nullable = false, updatable = true)
    private String city;

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    @Override
    public int compareTo(Address arg0) {
        String address = postcode + city + street;
        String addressComp = arg0.postcode + arg0.city + arg0.street;
        for (int i = 0; i < address.length(); i++) {
            if (address.charAt(i) < addressComp.charAt(i))
                return -1;
            if (address.charAt(i) > addressComp.charAt(i))
                return 1;
        }

        if (address.length() < addressComp.length())
            return -1;
        if (address.length() > addressComp.length())
            return 1;
        return 0;

        // TODO possible with String.compareToString
    }
}