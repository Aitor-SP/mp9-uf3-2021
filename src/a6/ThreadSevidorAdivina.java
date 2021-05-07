package a6;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ThreadSevidorAdivina implements Runnable {

	Socket clientSocket = null;
	Llista entrante;
	Llista saliente;
	boolean acabat;

	public ThreadSevidorAdivina(Socket clientSocket) throws IOException {
		this.clientSocket = clientSocket;
		acabat = false;
	}

	@Override
	public void run() {
		try {
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
			ObjectInputStream objectInputStream = new ObjectInputStream(clientSocket.getInputStream());
			while(!acabat) {
				entrante = (Llista)objectInputStream.readObject();
				saliente = generaResposta(entrante);
				objectOutputStream.writeObject(saliente);
				objectOutputStream.flush();
			}
		}catch(IOException | ClassNotFoundException e){
			System.out.println(e.getLocalizedMessage());
		}
		try {
			clientSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Llista generaResposta(Llista llistaOrdenada) {
		Set<Integer> set = new HashSet<>(llistaOrdenada.getNumberList());
		List<Integer> list = new ArrayList<>(set);
		llistaOrdenada.getNumberList().clear();
		llistaOrdenada.getNumberList().addAll(list);
		acabat = true;
		return llistaOrdenada;
	}

}