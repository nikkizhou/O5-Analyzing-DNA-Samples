import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Monitor2 {
    Lock laas = new ReentrantLock();
    Condition ikkeTom = laas.newCondition();
    Condition harMinstTo = laas.newCondition();
    ArrayList<HashMap<String, Subsekvens>> alleH = new ArrayList<HashMap<String, Subsekvens>>();


    
    public void leggTilHmap(HashMap<String, Subsekvens> hmap) throws InterruptedException {
        alleH.add(hmap);
    }
    
    public void leggTilFlettet(HashMap<String, Subsekvens> hmap) throws InterruptedException {
        laas.lock();
        try {
            alleH.add(hmap);
            harMinstTo.signalAll();
            
        } finally {
            laas.unlock();
        }   
    }
    
    public int antHmap() {
        return alleH.size();
    }
    
    public ArrayList<HashMap<String, Subsekvens>> hentUtTo() throws InterruptedException {
        laas.lock();
        ArrayList<HashMap<String, Subsekvens>> listenMedToH = new ArrayList<>();
        try {
            // hvis monitoren har mindre enn to hashmapper, bruk await for aa vente
            while (alleH.size() < 2) {
                harMinstTo.await();
            }
            // legg forste to hashmapper som er i den store listen til listen som skal returneres.
            listenMedToH.add(alleH.remove(0));
            listenMedToH.add(alleH.remove(1));

            return listenMedToH;

        } finally {
            laas.unlock();
        }
    }

    public ArrayList<HashMap<String, Subsekvens>> hentArrayListen() {
        return alleH;
    }
   
    // ta ut foerste hashmappen i monitoren.
    public HashMap<String, Subsekvens> taUtHmap() throws InterruptedException {
        laas.lock();
        try {
            while (alleH.isEmpty()) {
                ikkeTom.await();
            }
        return alleH.get(0);
      
        } finally {
            laas.unlock();
        }
    }
    
}
