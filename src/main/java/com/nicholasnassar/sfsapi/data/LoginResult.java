package com.nicholasnassar.sfsapi.data;

public class LoginResult {
    private final String result, error;

    public LoginResult(String result, String error) {
        this.result = result;

        this.error = error;
    }

    public String getResult() {
        return result;
    }

    public String getError() {
        return error;
    }
}
