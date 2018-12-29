package de.sb.messenger.persistence;

import de.sb.toolbox.bind.JsonProtectedPropertyStrategy;

import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbVisibility;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.*;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.Comparator;

@Embeddable
@JsonbVisibility(JsonProtectedPropertyStrategy.class)
@XmlRootElement
@XmlType
public class Name implements Comparable<Name> {
    private static final Comparator<Name> NAME_COMPARATOR = Comparator.comparing(Name::getFamily).thenComparing(Name::getGiven);

    @Size(min = 1, max = 31)
    @NotNull
    @NotEmpty
    @Column(name = "surname", nullable = false, updatable = true)
    private String family;

    @Size(min = 1, max = 31)
    @NotNull
    @NotEmpty
    @Column(name = "forename", nullable = false, updatable = true)
    private String given;

    @JsonbProperty
    @XmlAttribute
    public String getFamily() {
        return family;
    }

    public void setFamily(String family) {
        this.family = family;
    }

    @JsonbProperty
    @XmlAttribute
    public String getGiven() {
        return given;
    }

    public void setGiven(String given) {
        this.given = given;
    }

    @Override
    public int compareTo(Name other) {
        return NAME_COMPARATOR.compare(this, other);
    }
}