package org.feup.cpd.store.message;

import java.util.Queue;
import java.util.Set;

public class MembershipMessage extends Message {

    public MembershipMessage(Set<String> view, Queue<String> events) {
        super("MEMBERSHIP");

        body.append("VIEW").append(CRLF);
        for (String element : view)
            body.append(element).append(CRLF);

        body.append("LOGS").append(CRLF);
        for (String event : events)
            body.append(event).append(CRLF);
    }

    @Override
    public String toString() {
        return type + CRLF + body;
    }

    @Override
    public String getContent() {
        return body.toString();
    }
}
