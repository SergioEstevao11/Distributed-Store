package org.feup.cpd.store.message;

public abstract class Message {

    protected final static String CRLF = "\r\n";

    protected final String type;
    protected final StringBuilder body;

    protected Message(String type) {
        this.type = type;
        this.body = new StringBuilder();
    }

    public abstract String getContent();
}

