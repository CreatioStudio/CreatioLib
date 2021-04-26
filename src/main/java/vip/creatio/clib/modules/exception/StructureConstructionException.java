package vip.creatio.clib.modules.exception;

public class StructureConstructionException extends RuntimeException {
    public final DisabledReason reason;
    public StructureConstructionException(String msg, DisabledReason reason) {
        super(msg);
        this.reason = reason;
    }

    public enum DisabledReason {
        OVERLAPPED,
        NAMESPACE_NOT_EXIST,
        MISMATCH,
        EVENT_CANCELLED,
        CENTER_MISMATCH,
        REACH_WORLD_LIMIT,
        REACH_RANGE_LIMIT,
        DELETED;

        DisabledReason() {}
    }
}
