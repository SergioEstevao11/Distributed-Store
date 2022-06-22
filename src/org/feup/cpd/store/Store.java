package org.feup.cpd.store;

import org.feup.cpd.store.rmi.MembershipService;

import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class Store {

    public static void main(String[] args) {
        if (args.length != 4) {
            System.err.println("Usage: java Store <IP_mcast_addr> <IP_mcast_port> <node_id> <Store_port>");
            System.exit(1);
        }

        try {
            AccessPoint clusterAccessPoint = new AccessPoint(args[0], args[1]);
            AccessPoint nodeAccessPoint = new AccessPoint(args[2], args[3]);

            Node node = new Node(nodeAccessPoint);
            MembershipService membership = new MembershipService(clusterAccessPoint, node);
            membership.listen();

        } catch (UnknownHostException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
