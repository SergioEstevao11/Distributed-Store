package org.feup.cpd.store.message;

import org.feup.cpd.store.AccessPoint;

public class PutMessage extends Message{

    private final AccessPoint accessPoint;
    private final String key;
    private final String value;


    public PutMessage(AccessPoint accessPoint, String key, String value) {
        /**
         * accessPoint : origin node ip address
         * key : key
         * value : value
         * port : open tpc port to receive response, if -1 it means its the node that received the
         *        request from the user
         */
        super("PUT");
        this.accessPoint = accessPoint;
        this.key = key;
        this.value = value;


        body.append(accessPoint).append(' ')
                .append(key).append(' ');

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
