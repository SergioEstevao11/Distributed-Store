package org.feup.cpd.store.network;

import org.feup.cpd.store.AccessPoint;
import org.feup.cpd.store.Node;
import org.feup.cpd.store.message.*;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;

public class OperationDecoder implements Runnable {

    private final Node node;
    private final List<String> content;

    public OperationDecoder(Node node, List<String> content) {
        this.node = node;
        this.content = content;
    }

    private void decodeMembership() {
        int viewStart = content.indexOf("VIEW");
        int logsStart = content.indexOf("LOGS");

        List<String> view = content.subList(viewStart + 1, logsStart);
        for (String element : view)
            node.addNodeToView(element);

        List<String> events = content.subList(logsStart + 1, content.size()-1);
        for (String event : events)
            node.addMembershipEvent(event);
    }

    private void decodeJoin() {
        String[] fields = content.get(1).split("\\s+");


        node.addNodeToView(fields[0]);
        node.addMembershipEvent(fields[0] + " " + fields[1]);

        String hostname = fields[0].split(":")[0];
        int port = Integer.parseInt(fields[2].trim());

        try {
            System.out.println("Sending back membership message to " + fields[1]);
            Thread.sleep(new Random().nextInt(1000));

            Socket socket = new Socket(hostname, port);
            socket.shutdownInput();

            MembershipMessage msg = new MembershipMessage(node.getView(), node.getEvents());
            OutputStream out = socket.getOutputStream();
            out.write(msg.toString().getBytes(StandardCharsets.UTF_8));

            socket.close();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void decodeLeave() {
        String[] fields = content.get(1).split("\\s+");
        node.removeNodeFromView(fields[0]);
        node.addMembershipEvent(content.get(1));
    }

    private void decodePut() throws IOException {
        System.out.println("RECEIVED A PUT");
        String[] fields = content.get(1).split("\\s+");

        String key = fields[1];
        String value = content.get(2);

        String location_node = node.locateKeyValue(key);
        System.out.println("=============================================" + location_node);

        if (node.getAccessPoint().toString().equals(location_node)){
            node.putValue(key, value);
            System.out.println(location_node + " - Stored " + key + " : " + value);
        }
        else{
            String[] parts = location_node.split(":");
            String address = parts[0];
            String port = parts[1];

            System.out.println("=============================================" + address);
            System.out.println("=============================================" + port);


            AccessPoint accessPointRedirect = new AccessPoint(address, port);
            PutMessage putMessage = new PutMessage(accessPointRedirect, key, value);
            Socket socket = new Socket(accessPointRedirect.getAddress(), accessPointRedirect.getKeyValuePort());

            socket.getOutputStream().write(putMessage.toString().getBytes(StandardCharsets.UTF_8));
            socket.getOutputStream().flush();
            socket.close();
        }

    }

    private void decodeGet() throws IOException {
        String[] fields = content.get(1).split("\\s+");

        String accessPoint = fields[0];
        String key = fields[1];

        String location_node = node.locateKeyValue(key);
        String value = "";



        if (node.getAccessPoint().toString().equals(location_node)){
            value = node.getValue(key);
            if (!accessPoint.equals(node.getAccessPoint())){
                String[] parts = accessPoint.split(":");
                String address = parts[0];
                String port = parts[1];

                AccessPoint accessPointReturn = new AccessPoint(address, port);
                Socket socket = new Socket(accessPointReturn.getAddress(), accessPointReturn.getKeyValuePort());
                ReturnMessage returnMessage = new ReturnMessage(accessPointReturn, key, value);
                socket.getOutputStream().write(returnMessage.toString().getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                socket.close();
                System.out.println("=============================================SENT RETURN MESSAGE TO "+ accessPoint);

            }
            System.out.println(location_node + " - Get " + value);
        }
        else{
            String[] parts = location_node.split(":");
            String address = parts[0];
            String port = parts[1];

            AccessPoint accessPointRedirect = new AccessPoint(address, port);
            GetMessage getMessage = new GetMessage(node.getAccessPoint(), key);
            Socket socket = new Socket(accessPointRedirect.getAddress(), accessPointRedirect.getKeyValuePort());
            socket.getOutputStream().write(getMessage.toString().getBytes(StandardCharsets.UTF_8));
            socket.getOutputStream().flush();
            socket.close();

        }

    }

    private void decodeDel() throws IOException {
        String[] fields = content.get(1).split("\\s+");

        String accessPoint = fields[0];
        String key = fields[1];

        String location_node = node.locateKeyValue(key);

        if (node.getAccessPoint().toString().equals(location_node)){
            node.deleteValue(key);
            System.out.println(location_node + " - Del " + key);
        }
        else{
            String[] parts = location_node.split(":");
            String address = parts[0];
            String port = parts[1];

            AccessPoint accessPointRedirect = new AccessPoint(address, port);
            DeleteMessage deleteMessage = new DeleteMessage(accessPointRedirect, key);
            Socket socket = new Socket(accessPointRedirect.getAddress(), accessPointRedirect.getKeyValuePort());
            socket.getOutputStream().write(deleteMessage.toString().getBytes(StandardCharsets.UTF_8));
            socket.getOutputStream().flush();
            socket.close();
        }
    }

    private void decodeReturn() {
        String[] fields = content.get(1).split("\\s+");
        String origin = fields[0];
        String key = fields[1];
        String value = content.get(2);
        System.out.println("=============================================" + key);
        System.out.println("=============================================" + value);
        System.out.println("RECEIVED REDIRECT");

        node.putValue(key, value);

    }

    @Override
    public void run() {
        //System.out.println("RECEIVED A MESSAGE");
        try {
            switch (content.get(0)) {
                case "MEMBERSHIP":
                    decodeMembership();
                    break;
                case "JOIN":
                    decodeJoin();
                    break;
                case "LEAVE":
                    decodeLeave();
                    break;
                case "PUT":
                    decodePut();
                    break;
                case "GET":
                    decodeGet();
                    break;
                case "DEL":
                    decodeDel();
                    break;
                case "RETURN":
                    decodeReturn();
                    break;
                default:
                    System.err.println("Error while decoding message of type: " + content);
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
}
