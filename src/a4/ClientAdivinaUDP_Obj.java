package a4;

import java.io.*;
import java.net.*;
import java.util.*;

public class ClientAdivinaUDP_Obj {
/** Client/Jugador que ha d'encertar un numero del ServidorAdivinaUDP_Obj.java -> Comunicació UDP
 *  El servidor no s'aturarà fins que tots els jugadors encertin el número
 *  Exemple de com enviar i rebre objectes en una comunicació UDP
 *  Envia una Jugada i reb un Tauler de puntuacions
 *
 *  Quan s'encerta el número es connecta al socket multicast per obtenir l'estat del joc de la
 *  resta de jugadors
**/

	private int portDesti;
	private int result;
	private String Nom, ipSrv;
	private int intents;
	private InetAddress adrecaDesti;
	private Tauler t;
	private Jugada j;

	private MulticastSocket multisocket;
	private InetAddress multicastIP;
	boolean continueRunning = true;
	InetSocketAddress groupMulticast;
	NetworkInterface netIf;

	public ClientAdivinaUDP_Obj(String ip, int port) {
		this.portDesti = port;
		result = -1;
		intents = 0;
		ipSrv = ip;
		j= new Jugada();
		try {
			multisocket = new MulticastSocket(5557);
			multicastIP = InetAddress.getByName("224.0.0.10");
			groupMulticast = new InetSocketAddress(multicastIP,5557);
			netIf = NetworkInterface.getByName("wlp0s20f3");
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			adrecaDesti = InetAddress.getByName(ipSrv);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
	
	public void setNom(String n) {
		Nom=n;
	}
	
	public int getIntents () {
		return intents;
	}
	
	public void runClient() throws IOException {
		byte [] receivedData = new byte[1024];
		int n;
		DatagramPacket packet;
		DatagramSocket socket = new DatagramSocket();
		//Missatge de benvinguda
		System.out.println("Hola " + Nom + "! Comencem!\n Digues un número: ");
		//Bucle de joc
		while(result!=0 && result!=-2) {
			Scanner sc = new Scanner(System.in);
			n = sc.nextInt();
			j.Nom = Nom;
			j.num = n;
			//byte[] missatge = ByteBuffer.allocate(4).putInt(n).array();
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(os);
			oos.writeObject(j);
			byte[] missatge = os.toByteArray();

			//creació del paquet a enviar
			packet = new DatagramPacket(missatge, missatge.length, adrecaDesti, portDesti);
			//creació d'un sòcol temporal amb el qual realitzar l'enviament
			//socket = new DatagramSocket();
			//Enviament del missatge
			socket.send(packet);
			//creació del paquet per rebre les dades
			packet = new DatagramPacket(receivedData, 1024);
			//espera de les dades
			socket.setSoTimeout(5000);
			try {
				socket.receive(packet);
				//processament de les dades rebudes i obtenció de la resposta
				result = getDataToRequest(packet.getData(), packet.getLength());
			}catch(SocketTimeoutException e) {
				System.out.println("El servidor no respòn: " + e.getMessage());
				result=-2;
			}

		}
		socket.close();
		//Si és l'últim jugador no cal connexió multicast per saber l'estat del joc perquè
		//el servidor també acaba amb l'encert de l'últim jugador
		if(t.acabats < t.map_jugadors.size()) {
			multisocket.joinGroup(groupMulticast,netIf);
			while (continueRunning) {
				DatagramPacket mpacket = new DatagramPacket(receivedData, 1024);
				multisocket.receive(mpacket);
				continueRunning = printData(mpacket.getData());
			}
			multisocket.leaveGroup(groupMulticast,netIf);
			multisocket.close();
		}
	}

	private void setTauler(byte[] data) {
		ByteArrayInputStream in = new ByteArrayInputStream(data);
		try {
			ObjectInputStream ois = new ObjectInputStream(in);
			t = (Tauler) ois.readObject();
			t.map_jugadors.forEach((k,v)-> System.out.println("Intents:" + k + "->" + v));
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

	}

	private boolean printData(byte[] data) {
		setTauler(data);
		if(t.acabats == t.map_jugadors.size()) return false;
		else return true;

	}

	private int getDataToRequest(byte[] data, int length) {
		setTauler(data);
		String msg = null;
		switch (t.resultat) {
			case 0: msg = "Correcte"; break;
			case 1: msg = "Més petit"; break;
			case 2: msg = "Més gran"; break;
		}
		System.out.println(msg);
		return t.resultat;
	}


	public int getResult() {
		return result;
	}

	public void setResult(int result) {
		this.result = result;
	}

	public static void main(String[] args) {
		String jugador, ipSrv;
				
		//Demanem la ip del servidor i nom del jugador
		System.out.println("IP del servidor?");
		Scanner sip = new Scanner(System.in);
		ipSrv = sip.next();
		System.out.println("Nom jugador:");
		jugador = sip.next();
		
		ClientAdivinaUDP_Obj cAdivina = new ClientAdivinaUDP_Obj(ipSrv, 5556);
				
		cAdivina.setNom(jugador);
		try {
			cAdivina.runClient();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if(cAdivina.getResult() == 0) {
			System.out.println("Fi, ho has aconseguit amb "+ cAdivina.t.map_jugadors.get(jugador) +" intents");
			cAdivina.t.map_jugadors.forEach((k,v)-> System.out.println(k + "->" + v));
		} else {
			System.out.println("Has perdut");
		}
	}
}
