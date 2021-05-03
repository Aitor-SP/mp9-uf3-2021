package a4;

import a3.NombreSecret;

import java.io.*;
import java.net.*;

public class ServidorAdivinaUDP_Obj {
/** Servidor que es pensa un número i ClientAdivinaUDP_Obj.java l'ha d'encertar.
 ** Comunicació UDP amb transmissió d'objectes.
 ** Reb un Jugada i envia un Tauler de puntuacions
 ** Accepta varis clients i es tanca la comunicació quan tots els clients ja hagin encertat el número.
 **/

	DatagramSocket socket;
	int port, fi, acabats, multiport=5557;
	NombreSecret ns;
	boolean acabat;
	Tauler tauler;
	MulticastSocket multisocket;
	InetAddress multicastIp;
	//InetSocketAddress groupMulticast;
	//NetworkInterface netIf;

	public ServidorAdivinaUDP_Obj(int port, int max) {
		try {
			socket = new DatagramSocket(port);
			multisocket = new MulticastSocket(multiport);
			multicastIp = InetAddress.getByName("224.0.0.10");
			//groupMulticast = new InetSocketAddress(multicastIp,multiport);
			//netIf = NetworkInterface.getByName("wlp0s20f3");
			NetworkInterface.networkInterfaces().forEach(i -> System.out.println(i.toString()));
		} catch (IOException e) {
			e.printStackTrace();
		}

		this.port = port;
		ns = new NombreSecret(max);
		tauler = new Tauler();
		acabat = false;
		acabats = 0;
		fi=-1;
	}

	public void runServer() throws IOException{
		 byte [] receivingData = new byte[1024];
		 byte [] sendingData;
		 InetAddress clientIP;
		 int clientPort;
		  
		 //el servidor atén el port mentre hi hagi jugadors
		 while(acabats < tauler.map_jugadors.size() || acabats==0){
			 DatagramPacket packet = new DatagramPacket(receivingData, receivingData.length);
			 socket.receive(packet);
			 sendingData = processData(packet.getData(), packet.getLength());
			 clientIP = packet.getAddress();
			 clientPort = packet.getPort();
			 packet = new DatagramPacket(sendingData, sendingData.length,
					 clientIP, clientPort);
			 socket.send(packet);
			 //A cada jugada també enviem les dades del tauler per multicast
			 //perquè el clients que ja hagin acabat puguin seguir el jic
			 DatagramPacket multipacket = new DatagramPacket(sendingData, sendingData.length,
					multicastIp,multiport);
			 multisocket.send(multipacket);
		 }
		 socket.close();
	}

	//Processar la Jugada: Nom i numero
	private byte[] processData(byte[] data, int length) {
		Jugada j = null;
		ByteArrayInputStream in = new ByteArrayInputStream(data);
		try {
			ObjectInputStream ois = new ObjectInputStream(in);
			j = (Jugada) ois.readObject();
			System.out.println("jugada:" + j.Nom + " " + j.num);
			//Si no existeix el jugador a la llista és un jugador nou
			//per tant l'afegim i inicialitzem les tirades
			if(!tauler.map_jugadors.containsKey(j.Nom)) tauler.map_jugadors.put(j.Nom, 1);
			else {
				//Si el judador ja esxiteix, actualitzem la quatitat de tirades
				int tirades = tauler.map_jugadors.get(j.Nom) + 1;
				tauler.map_jugadors.put(j.Nom, tirades);
			}
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}

		//comprobar la jugada
		fi = tauler.resultat = ns.comprova(j.num);
		if(fi==0) {
			acabat = true;
			//augmentem la quantitat de jugadors que l'han encertat/acabat
			acabats++;
			tauler.acabats++;
		}
		//La resposta és el tauler amb les dades de tots els jugadors
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(os);
			oos.writeObject(tauler);
		} catch (IOException e) {
			e.printStackTrace();
		}
		byte[] resposta = os.toByteArray();
		return resposta;
	}
	
	public static void main(String[] args) throws SocketException, IOException {
		ServidorAdivinaUDP_Obj sAdivina = new ServidorAdivinaUDP_Obj(5556, 100);
				
		try {
			sAdivina.runServer();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Fi Servidor");
		
		

	}

}
