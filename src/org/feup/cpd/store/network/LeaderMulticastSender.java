package org.feup.cpd.store.network;

import org.feup.cpd.store.AccessPoint;
import org.feup.cpd.store.Node;
import org.feup.cpd.store.message.MembershipMessage;

import java.io.IOException;

public class LeaderMulticastSender extends MulticastSender implements Runnable {

    private final Node node;
    private volatile boolean running = true;

    public LeaderMulticastSender(AccessPoint cluster, Node node) {
        super(cluster);
        this.node = node;
    }

    public void stopRunning() {
        this.running = false;
    }

    @Override
    public void run() {
        while (running) {
            try {
                send(new MembershipMessage(node.getView(), node.getEvents()));
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
