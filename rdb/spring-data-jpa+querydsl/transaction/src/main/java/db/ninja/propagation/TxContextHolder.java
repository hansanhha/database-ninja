package db.ninja.propagation;


public class TxContextHolder {

    private static final ThreadLocal<Boolean> outerTxContext = new ThreadLocal<>();

    public static void setOuterTxContext(Boolean outerTxContext) {
        TxContextHolder.outerTxContext.set(outerTxContext);
    }

    public static Boolean isOuterTransactionActive() {
        return Boolean.TRUE.equals(outerTxContext.get());
    }

    public static void clear() {
        outerTxContext.remove();
    }

}
