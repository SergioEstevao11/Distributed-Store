package org.feup.cpd.store.network;

import org.feup.cpd.store.Node;
import org.feup.cpd.store.message.JoinMessage;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

public class MembershipInitializer extends Thread {

    private final ExecutorService pool;
    private final Node node;
    private final ServerSocket server;
    private final MulticastSender sender;
    private int retries, received;

    public MembershipInitializer(ExecutorService pool, Node node, MulticastSender sender) throws IOException {
        this.pool = pool;
        this.node = node;
        this.sender = sender;

        this.retries = 0;
        this.received = 0;

        this.server = new ServerSocket(0, 3, node.getAccessPoint().getAddress());
        this.server.setSoTimeout(1 * 1000);
    }


    @Override
    public void run() {


        JoinMessage join = new JoinMessage(node.getAccessPoint(), node.getCounter(), server.getLocalPort());

        do {

            try {
                sender.send(join);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            try {
                received = 0;
                do {
                    Socket socket = server.accept();
                    List<String> content = new String(socket.getInputStream().readAllBytes())
                            .lines().collect(Collectors.toList());

                    pool.execute(new OperationDecoder(node, content));
                    received++;
                    System.out.println("content = " + content);

                    socket.close();
                } while (received < 3);

                System.err.println("Initialization complete");
                break;

            } catch (SocketTimeoutException e) {
                retries++;
            } catch (IOException e) {
                e.printStackTrace();
            }

        } while (retries < 3);

        node.addMembershipEvent(join.getContent());
    }
}
