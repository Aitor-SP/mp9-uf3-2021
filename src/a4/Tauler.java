package a4;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jordi on 05/03/17.
 */
public class Tauler implements Serializable {
    public static final long serialVersionUID = 1L;
    public Map<String,Integer> map_jugadors;
    public int resultat = 3, acabats;

    public Tauler() {
        map_jugadors = new HashMap<>();
        acabats = 0;
    }
    public int getNumPlayers() {
        return map_jugadors.size();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Intents\n");
        map_jugadors.forEach((k,v) -> sb.append(k).append(" - ").append(v).append("\n"));
        return sb.toString();
    }
}

class Jugada implements Serializable {
    public static final long serialVersionUID = 1L;
    String Nom;
    int num;
}
