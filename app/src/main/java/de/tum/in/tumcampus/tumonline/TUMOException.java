package de.tum.in.tumcampus.tumonline;

public class TUMOException extends Exception {

    private static final long serialVersionUID = 4897016156482062448L;
    public final String errorMessage;

    public TUMOException(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
