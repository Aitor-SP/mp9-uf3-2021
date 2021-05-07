package a6;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientTcpAdivina extends Thread {

	String hostname;
	int port;
	boolean continueConnected;

	public ClientTcpAdivina(String hostname, int port) {
		this.hostname = hostname;
		this.port = port;
		continueConnected = true;
	}

	public void run() {
		Socket socket;
		List<Integer> numeros = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			numeros.add((int)(Math.random() * 10));
		}
		Llista llista = new Llista("llista", numeros);
		System.out.println("Números para enviar al server:");
		for (Integer i:llista.getNumberList()) {
			System.out.print(i+" ");
		}
		System.out.println();
		try {
			socket = new Socket(InetAddress.getByName(hostname), port);
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
			ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
			//Enviamos la lista con los números random
			objectOutputStream.writeObject(llista);
			//Hay que hacer el flush, el tirar de la cadena
			objectOutputStream.flush();
			//Recibimos la respuesta del servidor
			Llista llistaFromServer = (Llista) objectInputStream.readObject();
			System.out.println("Respuesta del server:");
			for (Integer i:llistaFromServer.getNumberList()) {
				System.out.print(i+" ");
			}
			close(socket);
		} catch (UnknownHostException ex) {
			System.out.println("Error de connexió. No existeix el host: " + ex.getMessage());
		} catch (IOException ex) {
			System.out.println("Error de connexió indefinit: " + ex.getMessage());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void close(Socket socket){
		//si falla el tancament no podem fer gaire cosa, només enregistrar
		//el problema
		try {
			//tancament de tots els recursos
			if(socket!=null && !socket.isClosed()){
				if(!socket.isInputShutdown()){
					socket.shutdownInput();
				}
				if(!socket.isOutputShutdown()){
					socket.shutdownOutput();
				}
				socket.close();
			}
		} catch (IOException ex) {
			//enregistrem l'error amb un objecte Logger
			Logger.getLogger(ClientTcpAdivina.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public static void main(String[] args) {
		ClientTcpAdivina clientTcp = new ClientTcpAdivina("localhost",5558);
		clientTcp.start();
	}
}
