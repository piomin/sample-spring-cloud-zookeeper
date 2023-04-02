package pl.piomin.services.account.exception;

public class BalanceNotEnoughException extends Exception {

    public BalanceNotEnoughException(String message) {
        super(message);
    }
}
