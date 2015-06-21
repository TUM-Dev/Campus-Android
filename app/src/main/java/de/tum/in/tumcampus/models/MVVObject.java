package de.tum.in.tumcampus.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by enricogiga on 16/06/2015.
 */
public class MVVObject implements Serializable {

    private static final long serialVersionUID = -8060210544600464481L;
    protected boolean isValid = true;
    protected boolean isSuggestion = false;
    protected boolean isDeparture = false;
    protected String exception = "";
    protected String errorCode = "";
    protected String message = "";

    private String departureHeader;
    private String departureServerTime;

    private List<MVVObject> resultList = new ArrayList<MVVObject>();


    public String getDepartureHeader() {
        return departureHeader;
    }

    public void setDepartureHeader(String departureHeader) {
        this.departureHeader = departureHeader;
    }

    public String getDepartureServerTime() {
        return departureServerTime;
    }

    public void setDepartureServerTime(String departureServerTime) {
        this.departureServerTime = departureServerTime;
    }
    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean isValid) {
        this.isValid = isValid;
    }

    public List<MVVObject> getResultList() {
        return resultList;
    }

    public void setResultList(List<MVVObject> resultList) {
        this.resultList = resultList;
    }

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuggestion() {
        return isSuggestion;
    }

    public void setSuggestion(boolean isSuggestion) {
        this.isSuggestion = isSuggestion;
    }

    public boolean isDeparture() {
        return isDeparture;
    }

    public void setDeparture(boolean isDeparture) {
        this.isDeparture = isDeparture;
    }
}
