import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class FletteTrad implements Runnable {
    Monitor2 monitor;
    CountDownLatch count;
    int traadIndeks;
    Lock laas = new ReentrantLock();


    public FletteTrad(Monitor2 m, CountDownLatch count, int traadIndeks) {
        monitor = m;
        this.count = count;
        this.traadIndeks = traadIndeks;
    }

    public void run() {
        laas.lock();
        try {
            // tar ut forste to hashmapper fra monitor, fletter dem.
            HashMap<String, Subsekvens> res = flett(monitor.hentUtTo());
            // legg den flettethashmappen res til monitoren.
            monitor.leggTilFlettet(res);
            
            count.countDown();

        } catch (InterruptedException e) {
            System.out.println("Interrupted!");
        } finally {
            laas.unlock();
        }
        System.out.println("Traaden " + traadIndeks + " har fullfoert fletting. Stoerrelse til monitoren naa: "
                + monitor.antHmap());
    }
        


    // flett to hashmapper til en og returnerer den flettet hashmappen
    // listenMed To inneholder to hashmapper som skal flettes
    public HashMap<String, Subsekvens> flett(ArrayList<HashMap<String, Subsekvens>> listenMedTo) {

        Subsekvens hentaSub;
        HashMap<String, Subsekvens> subSeqHashNy = new HashMap<String, Subsekvens>();

        // Iterer alle subsekvens objekt i hashmapn1 i listenMed To
        for (Subsekvens sub1 : listenMedTo.get(0).values()) {
            // prove finne om det er noen susekvens i hashmap2 som har samme navn som hashmap1
            hentaSub = listenMedTo.get(1).remove(sub1.hentSubNavn());

            // if navnet til en subskvens i hashmap1 ikke finnes i hashmap2
            if (hentaSub == null) {
                // legg subskvensen til den nye hashmappen subSeqHashNy
                subSeqHashNy.put(sub1.hentSubNavn(), sub1);

                // hvis navnet til en subskvens i hashmap1 finnes allerede i hashmap2
            } else {
                // hent antall denne subsekvensen i hashmap2
                int ant = hentaSub.antallFrkmster();
                // plus antallet til samme subsekvensen i hashmap1
                sub1.leggTilAnt(ant);
                // put subsekvensen som er i hashmap1 til subSeqHashNy
                subSeqHashNy.put(sub1.hentSubNavn(), sub1);
            }
        }

        // legg alle restende subsekvenser i hashmap2 til subSeqHashNy
        for (Subsekvens sub2 : listenMedTo.get(1).values()) {
            subSeqHashNy.put(sub2.hentSubNavn(), sub2);
        }
        
        return subSeqHashNy;
    }
 
}
