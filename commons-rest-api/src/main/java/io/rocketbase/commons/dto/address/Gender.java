package io.rocketbase.commons.dto.address;

import com.fasterxml.jackson.annotation.JsonValue;
import io.rocketbase.commons.translation.Translation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.Locale;

@Schema(enumAsRef = true)
public enum Gender {

    FEMALE("female",
            Translation.of(Locale.ENGLISH, "Female").german("Frau"),
            Translation.of(Locale.ENGLISH, "Ms").german("Frau")),

    MALE("male", Translation.of(Locale.ENGLISH, "Male").german("Mann"),
            Translation.of(Locale.ENGLISH, "Mr").german("Herr")),

    DIVERSE("diverse", Translation.of(Locale.ENGLISH, "Diverse").german("Divers"),
            Translation.of(Locale.ENGLISH, "Mx").german("Sonsige"));

    private String value;

    @Getter
    private Translation translation;

    @Getter
    private Translation salutation;

    @JsonValue
    public String getValue() {
        return value;
    }

    Gender(String value, Translation translation, Translation salutation) {
        this.value = value;
        this.translation = translation;
        this.salutation = salutation;
    }

    public static Gender findByValue(String value) {
        if (value != null) {
            for (Gender v : Gender.values()) {
                if (v.getValue().equalsIgnoreCase(value)) {
                    return v;
                }
            }
        }
        return null;
    }
}
