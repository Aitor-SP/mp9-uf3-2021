package a5;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;

public class ClientVelocimetre {

    private InetAddress multicastIP;
    boolean continueRunning = true;
    InetSocketAddress groupMulticast;
    NetworkInterface netIf;
    private int velocidadMedia;
    private float resultado;
    private int contador;

    public ClientVelocimetre() {
        velocidadMedia = 0;
        contador = 0;
        try {
            multicastIP = InetAddress.getByName("AQUI VA LA IP");
            groupMulticast = new InetSocketAddress(multicastIP,5557);
            netIf = NetworkInterface.getByName("wlp0s20f3");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void runClient() throws IOException {
        byte [] receivedData = new byte[1024];
        //Hay que meterlo aqui para que funcione
        MulticastSocket socket = new MulticastSocket(5557);
        DatagramPacket packet;
        socket.joinGroup(groupMulticast, netIf);

        while(continueRunning){
            packet = new DatagramPacket(receivedData, 1024);
            socket.setSoTimeout(5000);
            try{
                socket.receive(packet);
                velocidadMedia = velocidadMedia + ByteBuffer.wrap(packet.getData()).getInt();
                System.out.println(ByteBuffer.wrap(packet.getData()).getInt());
                contador++;
                if(contador == 5){
                    contador = 0;
                    resultado = (float) velocidadMedia / 5;
                    System.out.println("MEDIA: " + resultado);
                    velocidadMedia = 0;
                }
            }catch (SocketTimeoutException e){
                System.out.println(e.getMessage());
                continueRunning = false;
            }
        }
        socket.leaveGroup(groupMulticast,netIf);
    }

    public static void main(String[] args) {
        try{
            ClientVelocimetre clientVelocimetre = new ClientVelocimetre();
            clientVelocimetre.runClient();
        }catch (IOException e){
            System.out.println(e.getMessage());
        }
    }
}
