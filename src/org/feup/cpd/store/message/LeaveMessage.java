package org.feup.cpd.store.message;

import org.feup.cpd.store.AccessPoint;

public final class LeaveMessage extends Message {

    private final AccessPoint accessPoint;
    private final long counter;

    public LeaveMessage(AccessPoint accessPoint, long counter) {
        super("LEAVE");
        this.accessPoint = accessPoint;
        this.counter = counter;

        body.append(accessPoint).append(' ').append(counter).append(' ');
    }

    @Override
    public String toString() {
        return type + CRLF + body;
    }

    @Override
    public String getContent() {
        return accessPoint + " " + counter;
    }
}
