package org.feup.cpd.client;

import org.feup.cpd.interfaces.Membership;
import org.feup.cpd.store.AccessPoint;

import java.io.File;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class TestClient {

    public static void main(String[] args) throws UnknownHostException {
        if (args.length < 2) {
            System.err.println("Usage: java TestClient <node_ap> <operation> [<opnd>]");
            System.exit(1);
        }

        AccessPoint nodeAccessPoint = parseNodeAccessPoint(args[0]);
        String operation = args[1];

        switch (operation) {
            case "join":
            case "leave":
                handleMembershipOperation(nodeAccessPoint, operation);
                break;
            case "get":
            case"put":
            case"delete":
                String operationArgument = args[2];
                handleKeyValueOperation(nodeAccessPoint, operation, operationArgument);
                break;

            default:
                throw new IllegalArgumentException("Unexpected operation: " + operation);
        }
    }


    private static void handleMembershipOperation(AccessPoint nodeAccessPoint,
                                                  String operation) throws IllegalArgumentException {
        try {
            Registry registry = LocateRegistry.getRegistry(nodeAccessPoint.getPort());
            Membership membership = (Membership) registry.lookup("Membership");

            switch (operation) {
                case "join":
                    membership.join(); break;
                case "leave":
                    membership.leave(); break;
                default:
                    throw new IllegalArgumentException("Unexpected operation: " + operation);
            }
        } catch (RemoteException | NotBoundException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void handleKeyValueOperation(AccessPoint nodeAccessPoint,
                                                String operation, String argument) throws IllegalArgumentException {

        ClientKeyValueOperation keyValueOperation = new ClientKeyValueOperation(nodeAccessPoint);

        switch (operation) {
            case "put":
                String key = keyValueOperation.putClientSetup(argument);
                System.out.println("File saved with the following key: " + key);
                break;
            case "get":
                keyValueOperation.get(argument);
                System.out.println("File saved to the following location: bucket/" + nodeAccessPoint.toString());
                break;
            case "delete":
                keyValueOperation.delete(argument);
                System.out.println("Erased the following key: " + argument);
                break;

            default:
                throw new IllegalArgumentException("Unexpected argument: " + argument);

        }
    }

    private static AccessPoint parseNodeAccessPoint(String nodeAccessPoint) throws UnknownHostException {
        String host = nodeAccessPoint.substring(0, nodeAccessPoint.indexOf(':'));
        String port = nodeAccessPoint.substring(nodeAccessPoint.indexOf(':') + 1);

        return new AccessPoint(host, port);
    }
}
