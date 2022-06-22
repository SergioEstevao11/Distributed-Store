package org.feup.cpd.store.message;

import org.feup.cpd.store.AccessPoint;

public class GetMessage extends Message{

    private final AccessPoint accessPoint;
    private final String key;


    public  GetMessage(AccessPoint accessPoint, String key) {
        /**
         * accessPoint : origin node ip address
         * key : key
         * port : open tpc port to receive response, if -1 it means its the node that received the
         *        request from the user
         */
        super("GET");
        this.accessPoint = accessPoint;
        this.key = key;

        body.append(accessPoint).append(' ')
                .append(key);
    }

    @Override
    public String toString() {
        return type + CRLF + body.toString();
    }

    @Override
    public String getContent() {
        return body.toString();
    }
}
