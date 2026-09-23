package com.zamcan.madrassa.data.geography;

public class Country {

    public String code;
    public String name;
    public boolean defaultCountry;
    public boolean priorityCountry;
    public boolean structuredDataAvailable;

    /*
     * International dialling prefix, e.g. "+255".
     * Empty when the catalog entry has no known code yet —
     * the phone field then keeps its default prefix.
     */
    public String dialCode;

    public Country(
            String code,
            String name,
            boolean defaultCountry,
            boolean priorityCountry,
            boolean structuredDataAvailable
    ) {
        this(
                code,
                name,
                defaultCountry,
                priorityCountry,
                structuredDataAvailable,
                ""
        );
    }

    public Country(
            String code,
            String name,
            boolean defaultCountry,
            boolean priorityCountry,
            boolean structuredDataAvailable,
            String dialCode
    ) {
        this.code = code;
        this.name = name;
        this.defaultCountry = defaultCountry;
        this.priorityCountry = priorityCountry;
        this.structuredDataAvailable =
                structuredDataAvailable;
        this.dialCode =
                dialCode == null ? "" : dialCode.trim();
    }
}
