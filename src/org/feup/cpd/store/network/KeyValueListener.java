package org.feup.cpd.store.network;

import org.feup.cpd.store.AccessPoint;
import org.feup.cpd.store.Node;

import java.io.IOException;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

public class KeyValueListener extends Thread{

    private final ExecutorService pool;
    private final ServerSocket server;
    private final Node node;
    private volatile boolean running;

    public KeyValueListener(ExecutorService pool, Node node) throws IOException {
        this.pool = pool;
        this.node = node;

        this.server = new ServerSocket(node.getAccessPoint().getKeyValuePort());
        this.running = true;
    }

    public void stopRunning() {
        this.running = false;
    }

    @Override
    public void run() {
        while (running) {
            try {
                Socket socket = server.accept();
                List<String> input = new String(socket.getInputStream().readAllBytes())
                        .lines().collect(Collectors.toList());

                pool.execute(new OperationDecoder(node, input));
                socket.close();

            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("KeyValueListener ioexception in while");
            }
        }

        try{
            server.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("KeyValueListener ioexception");
        }
    }
}
