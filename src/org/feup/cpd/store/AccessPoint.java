package org.feup.cpd.store;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;

public final class AccessPoint {

    private final InetAddress address;
    private final int port;

    public AccessPoint(String host, String port) throws UnknownHostException {
        this.address = InetAddress.getByName(host);
        this.port = Integer.parseUnsignedInt(port);
    }

    public InetAddress getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public int getKeyValuePort(){return port-1;}

    @Override
    public String toString() {
        return address.getHostAddress() + ':' + port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccessPoint that = (AccessPoint) o;
        return port == that.port && Objects.equals(address, that.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address, port);
    }
}
