package com.zamcan.madrassa.data.model;

public class ClassGroup {

    public String id;
    public String madrassaId;

    /*
     * Administrative/structural code.
     *
     * Examples:
     * 1
     * 2
     * 1A
     * 1B
     * 3C
     * CAM
     */
    public String code;

    /*
     * Human-facing display name.
     *
     * Examples:
     * Class 1
     * Camels
     * ZamZam
     * An-Nur
     */
    public String name;

    public String teacherId;

    /*
     * Controls ordering in class lists.
     */
    public int orderIndex;

    public boolean active;
}
