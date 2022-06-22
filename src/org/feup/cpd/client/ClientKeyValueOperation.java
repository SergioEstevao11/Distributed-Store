package org.feup.cpd.client;

import org.feup.cpd.interfaces.KeyValue;
import org.feup.cpd.store.AccessPoint;
import org.feup.cpd.store.message.DeleteMessage;
import org.feup.cpd.store.message.GetMessage;
import org.feup.cpd.store.message.PutMessage;

import java.io.*;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.stream.Collectors;

public class ClientKeyValueOperation implements KeyValue {
    private final AccessPoint nodeAccessPoint;

    public ClientKeyValueOperation(AccessPoint AccessPoint) {
        this.nodeAccessPoint = AccessPoint;
    }

    public String putClientSetup(String fileName) {
        StringBuilder keyToBigEndian = new StringBuilder(64);
        String value = "";

        try {
            Path filePath = Path.of(fileName);
            value = Files.readString(filePath);

            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] hash = messageDigest.digest(value.getBytes(StandardCharsets.UTF_8));

            for (byte b : hash)
                keyToBigEndian.append(String.format("%02x", b));

        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        String key = keyToBigEndian.toString();
        put(key, value);

        return key;
    }

    @Override
    public void put(String key, String value) {

        System.out.println("Value = " + value);

        try {
            Socket socket = new Socket(nodeAccessPoint.getAddress(), nodeAccessPoint.getKeyValuePort());
            PutMessage putMessage = new PutMessage(nodeAccessPoint, key, value);
            socket.getOutputStream().write(putMessage.toString().getBytes(StandardCharsets.UTF_8));

        }catch (IOException e){
            e.printStackTrace();
            System.out.println("Put Socket IOException");
        }


    }

    @Override
    public void get(String key) {
        try {
            Socket socket = new Socket(nodeAccessPoint.getAddress(), nodeAccessPoint.getKeyValuePort());
            GetMessage getMessage = new GetMessage(nodeAccessPoint, key);
            socket.getOutputStream().write(getMessage.toString().getBytes(StandardCharsets.UTF_8));

        }catch (IOException e){
            e.printStackTrace();
            System.out.println("Put Socket IOException");
        }

    }

    @Override
    public void delete(String key) {
        try {
            Socket socket = new Socket(nodeAccessPoint.getAddress(), nodeAccessPoint.getKeyValuePort());
            DeleteMessage deleteMessage = new DeleteMessage(nodeAccessPoint, key);
            socket.getOutputStream().write(deleteMessage.toString().getBytes(StandardCharsets.UTF_8));



        }catch (IOException e){
            e.printStackTrace();
            System.out.println("Put Socket IOException");
        }
    }
}
