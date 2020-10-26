package com.bc.fiduceo.reader.slstr;

class VariableType {

    enum Type {NADIR_1km, NADIR_500m, OBLIQUE_1km, OBLIQUE_500m};

    private boolean tiePoint;
    private Type type;

    public VariableType(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    boolean isTiePoint() {
        return tiePoint;
    }

    void setTiePoint(boolean tiePoint) {
        this.tiePoint = tiePoint;
    }
}
