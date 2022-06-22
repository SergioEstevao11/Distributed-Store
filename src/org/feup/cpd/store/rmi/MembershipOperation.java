package org.feup.cpd.store.rmi;

import org.feup.cpd.interfaces.Membership;
import org.feup.cpd.store.AccessPoint;
import org.feup.cpd.store.Node;
import org.feup.cpd.store.message.LeaveMessage;
import org.feup.cpd.store.network.LeaderMulticastSender;
import org.feup.cpd.store.network.KeyValueListener;
import org.feup.cpd.store.network.MembershipInitializer;
import org.feup.cpd.store.network.MulticastListener;
import org.feup.cpd.store.network.MulticastSender;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.concurrent.ExecutorService;

public class MembershipOperation implements Membership {

    private final ExecutorService pool;
    private final AccessPoint cluster;
    private final Node node;

    private final MulticastSender sender;
    private final MulticastListener membershipListener;
    private final KeyValueListener keyValueListener;

    public MembershipOperation(ExecutorService pool, AccessPoint cluster, Node node) throws IOException {
        this.pool = pool;
        this.cluster = cluster;
        this.node = node;

        this.sender = new MulticastSender(cluster);
        this.membershipListener = new MulticastListener(pool, cluster, node);
        this.keyValueListener = new KeyValueListener(pool, node);
    }

    @Override
    public void join() throws RemoteException {
        if (node.getCounter() % 2 == 0)
            throw new RemoteException("Unable to call join() on a cluster already joined");

        node.incrementCounter();

        try {
            MembershipInitializer initializer = new MembershipInitializer(pool, node, sender);
            initializer.start();
            initializer.join();

            if (node.getView().isEmpty()) {
                node.addNodeToView(node.getAccessPoint().toString());
                node.setLeader(true);
            }

        } catch (InterruptedException | IOException e) {
            node.decrementCounter();
            e.printStackTrace();
            throw new RemoteException("Unable to initialize " + node.getAccessPoint() + " within " + cluster);
        }


        membershipListener.start();
        keyValueListener.start();
        System.out.println(node.getAccessPoint() + " is now a part of " + cluster);
        System.out.println("node view = " + node.getView());

        if (node.isLeader()) {
            Thread leader = new Thread(new LeaderMulticastSender(cluster, node));
            leader.start();
        }
    }

    @Override
    public void leave() throws RemoteException {
        if (node.getCounter() % 2 != 0)
            throw new RemoteException("Unable to call leave() on a cluster already left");

        node.incrementCounter();

        try {
            membershipListener.stopRunning();
            keyValueListener.stopRunning();
            LeaveMessage leave = new LeaveMessage(node.getAccessPoint(), node.getCounter());
            sender.send(leave);

            node.addMembershipEvent(leave.getContent());
            node.leaveKeyValueRebalance();


        } catch (IOException e) {
            node.decrementCounter();
            e.printStackTrace();
            throw new RemoteException("Unable to send LEAVE via multicast to " + cluster.getAddress());
        }

        node.clearMembershipView();
        System.out.println(node.getAccessPoint() + " is no longer a part of " + cluster);
    }
}
