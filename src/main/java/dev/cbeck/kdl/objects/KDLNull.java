package dev.cbeck.kdl.objects;

public class KDLNull implements KDLValue {
    public static final KDLNull INSTANCE = new KDLNull();

    private KDLNull() {
    }

    public String toString() {
        return this.getClass().getSimpleName();
    }
}
