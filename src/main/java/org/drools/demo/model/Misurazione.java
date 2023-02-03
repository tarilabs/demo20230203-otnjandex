package org.drools.demo.model;

public class Misurazione extends Measurement {

    public Misurazione(String id, String val) {
        super(id, val);
    }

    @Override
    public String toString() {
        return "Misurazione{ "+ super.toString() +"}";
    }
}
