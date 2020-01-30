package io.rocketbase.commons.dto.address;

import com.fasterxml.jackson.annotation.JsonValue;
import io.rocketbase.commons.translation.Translation;
import lombok.Getter;

import java.util.Locale;

public enum Gender {

    FEMALE("female", Translation.of(Locale.ENGLISH, "Ms").german("Frau")),
    MALE("male", Translation.of(Locale.ENGLISH, "Mr").german("Herr")),
    DIVERSE("diverse", Translation.of(Locale.ENGLISH, "Mx").german("Divers"));

    @JsonValue
    @Getter
    private String value;

    @Getter
    private Translation translation;

    Gender(String value, Translation translation) {
        this.value = value;
        this.translation = translation;
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
