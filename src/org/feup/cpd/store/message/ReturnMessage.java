package org.feup.cpd.store.message;

import org.feup.cpd.store.AccessPoint;

public class ReturnMessage extends Message{
    private final AccessPoint accessPoint;
    private final String key;
    private final String value;

    public ReturnMessage(AccessPoint accessPoint, String key, String value) {
        super("RETURN");
        this.accessPoint = accessPoint;
        this.key = key;
        this.value = value;

        body.append(accessPoint).append(' ')
                .append(key);

    }

    @Override
    public String toString() {
        return type + CRLF + body.toString() + CRLF + value;
    }

    @Override
    public String getContent() {
        return body.toString();
    }
}
