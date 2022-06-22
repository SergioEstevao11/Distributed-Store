package org.feup.cpd.store.network;

import org.feup.cpd.store.AccessPoint;
import org.feup.cpd.store.Node;
//import org.feup.cpd.store.NodeState;

import java.io.IOException;
import java.net.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

public class MulticastListener extends Thread {

    private final ExecutorService pool;
    private final MulticastSocket socket;
    private final AccessPoint cluster;
    private final Node node;
    private volatile boolean running;

    public MulticastListener(ExecutorService pool, AccessPoint cluster, Node node) throws IOException {
        this.pool = pool;
        this.cluster = cluster;
        this.node = node;

        this.socket = new MulticastSocket(cluster.getPort());
        this.socket.setReuseAddress(true);
        this.running = true;
    }

    public void stopRunning() {
        this.running = false;
    }

    @Override
    public void run() {
        try {
            socket.joinGroup(InetAddress.getByName(cluster.getAddress().getHostAddress()));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        byte[] buffer = new byte[8192];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

        while (running) {
            try {
                socket.receive(packet);
                List<String> content = new String(packet.getData()).lines().collect(Collectors.toList());
                packet.setLength(buffer.length);
                pool.execute(new OperationDecoder(node, content));

            } catch (SocketTimeoutException e) {
                System.err.println(e.getMessage());

            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }

        try {
            socket.leaveGroup(InetAddress.getByName(cluster.getAddress().getHostAddress()));
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
