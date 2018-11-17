package com.chatbot.translate;

public class Simpanan {
    private String id_person;
    private String key;
    private String value;

    public Simpanan(String id_person, String key, String value) {
        this.id_person = id_person;
        this.key = key;
        this.value = value;
    }

    public String getId_person() {
        return id_person;
    }

    public void setId_person(String id_person) {
        this.id_person = id_person;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
