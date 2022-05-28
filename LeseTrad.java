import java.util.concurrent.locks.Lock;
import java.util.Scanner;
import java.io.File;
import java.util.HashMap;
import java.io.FileNotFoundException;

public class LeseTrad implements Runnable{
    String fil;
    Monitor2 monitor;

    public LeseTrad(String filnavn, Monitor2 m) {
        fil = filnavn;
        monitor = m;
    }
    
    public void run(){
        String linje, subStreng;
        HashMap<String, Subsekvens> subSeqHash = new HashMap<>();

        try {
            Scanner leser = new Scanner(new File(fil));

            while (leser.hasNextLine()) {
                linje = leser.nextLine().trim();

                // Stopp programmet og gi tilbakemelding om en linje er kortere enn 3 tegn.
                if (linje.length() < 3) {
                    System.out.println("Det er en linje er kortere enn 3 tegn i filen.");
                    break;
                }
                // Hent ut alle substrenger fra immunrepertoarene i filen, og legge dem i hashmappen.
                for (int i = 0; i + 3 <= linje.length(); i++) {
                    subStreng = linje.substring(i, i + 3);
                    //putIfAbsent soerger for at en subskvensen skal ikke lagres paa nytt hvis det allerede finnes i hashmappen.
                    subSeqHash.putIfAbsent(subStreng, new Subsekvens(subStreng, 1)); 
                }
            }
            leser.close();
            
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }

        try {
            monitor.leggTilHmap(subSeqHash);
            
        } catch (InterruptedException e) {
            System.out.println("Interrupted");
        }
    
    }
    
}
