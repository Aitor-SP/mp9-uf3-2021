package a5;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class ClientVelocimetre {

    private MulticastSocket multicastSocket;
    private InetAddress address;

    public ClientVelocimetre() {
        try {
            multicastSocket = new MulticastSocket();
            address = InetAddress.getByName("224.0.2.12");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String.

    public static void main(String[] args) {

    }
}
