package itstep.learning.rest;

public class RestResponseStatus {
    private boolean isSuccessful;
    private int code;
    private String phrase;

    public RestResponseStatus() {}

    public RestResponseStatus(int code) {
        switch (code){
            case 200: this.setSuccessful(true).setPhrase("OK").setCode(code); break;
            case 201: this.setSuccessful(true).setPhrase("Created").setCode(code); break;
            case 202: this.setSuccessful(true).setPhrase("Accepted").setCode(code); break;
            case 401: this.setSuccessful(false).setPhrase("Unauthorized").setCode(code); break;
            case 403: this.setSuccessful(false).setPhrase("Forbidden").setCode(code); break;
            case 404: this.setSuccessful(false).setPhrase("Not Found").setCode(code); break;
            case 409: this.setSuccessful(false).setPhrase("Conflict").setCode(code); break;
            case 415: this.setSuccessful(false).setPhrase("Unsupported media type").setCode(code); break;
            case 422: this.setSuccessful(false).setPhrase("Unprocessable entity").setCode(code); break;
            case 500: this.setSuccessful(false).setPhrase("Internal Server Error").setCode(code); break;
            case 501: this.setSuccessful(false).setPhrase("Not Acceptable").setCode(code); break;
            default: this.setSuccessful(false).setPhrase("Bad Request").setCode(code); break;

        }
    }

    public boolean isSuccessful() {
        return isSuccessful;
    }

    public RestResponseStatus setSuccessful(boolean successful) {
        isSuccessful = successful;
        return this;
    }

    public int getCode() {
        return code;
    }

    public RestResponseStatus setCode(int code) {
        this.code = code;
        return this;
    }

    public String getPhrase() {
        return phrase;
    }

    public RestResponseStatus setPhrase(String phrase) {
        this.phrase = phrase;
        return this;
    }
}
