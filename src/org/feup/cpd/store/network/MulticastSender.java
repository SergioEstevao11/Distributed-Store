package org.feup.cpd.store.network;

import org.feup.cpd.store.AccessPoint;
import org.feup.cpd.store.message.Message;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;

public class MulticastSender {

    private final AccessPoint cluster;

    public MulticastSender(AccessPoint cluster) {
        this.cluster = cluster;
    }

    public void send(Message message) throws IOException {
        DatagramSocket socket = new DatagramSocket();

        byte[] msgBytes = message.toString().getBytes(StandardCharsets.UTF_8);
        DatagramPacket packet = new DatagramPacket(msgBytes, msgBytes.length, cluster.getAddress(), cluster.getPort());

        socket.send(packet);
        socket.close();

        //System.out.println("message = " + message.getContent());
    }
}
