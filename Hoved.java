import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.io.File;
import java.util.concurrent.CountDownLatch;

import javax.management.monitor.Monitor;

public class Oblig5Hele {
    static String metadataFilbane;
    static int antallTraader;
    static CountDownLatch count;
    static Monitor2 sykMonitor = new Monitor2();
    static Monitor2 fristMonitor = new Monitor2();
    
    
    public static void main(String[] args) throws Exception {
        antallTraader = Integer.parseInt(args[1]);
        count = new CountDownLatch(antallTraader);

        //DEL1 
        //lage filer til hashmapper og legge dem til tilsvarende monitorer.
        metadataFilbane = args[0] + "/" + "metadata.csv";

        // lag en hashmap listeMedSannhet som ta vare paa alle filnavn og sannhetverdi i metadata
        // eg: {fil1.csv,True; fil2.csv,True; fil3.csv,True;....]}
        HashMap<String, Boolean> listeMedSannhet = LagOverordnetListeFraMeta();

        // lag hver fil til en hashmap og legge den til tilsvarende monitor.
        leggHmapperTilMonitorer(listeMedSannhet, args[0]);
        System.out
                .println("\nFerdig med aa legg hashmappene til monitor. \nSykMonitor storrelse: " + sykMonitor.antHmap()
                        + "\nFristMonitor storrelse: " + fristMonitor.antHmap());

        //DEL2
        // flette to og to hashmapper om gangen med flere traader, for flere runder, paa hver monitor
        HashMap<String, Subsekvens> sisteHmapFrist = fristMonitor.taUtHmap();
        HashMap<String, Subsekvens> sisteHmapSyk = sykMonitor.taUtHmap();

        //flett alle hashmapper i sykMonitor
        if (sykMonitor.antHmap() >= 2) {
            System.out.println("\nFletting i SykMonitor:");
            flettAlt(sykMonitor, sykMonitor.hentArrayListen().size() - 1);
        }
        if (sykMonitor.antHmap() >= 2) {
            sisteHmapSyk = flettTo(sykMonitor.hentArrayListen());
            System.out.println("Ferdig med aa flette siste to hashmapper i sykMonitor UTEN traad.");
        }

        // flett alle hashmapper i fristMonitor
        if (fristMonitor.antHmap() >= 2) {
            System.out.println("\nFletting i FristMonitor:");
            flettAlt(fristMonitor, fristMonitor.hentArrayListen().size() - 1);
        } else {
            sisteHmapFrist = fristMonitor.taUtHmap();

        }
        if (fristMonitor.antHmap() >= 2) {
            sisteHmapFrist = flettTo(fristMonitor.hentArrayListen());
            System.out.println("Ferdig med aa flette siste to hashmapper i fristMonitor UTEN traad.");
        }

        //DEL3 
        //Finn dominante subsekvenser
        // 1. Finn dominante subsekvenser som har flest forekomst blant de syke proeve
        FinnDominMaks(sisteHmapSyk, sisteHmapFrist);
        // 2. Finn dominante subsekvenser som  har 7 eller flere forekomster blant de syke proeve
        FinnDominSyv(sisteHmapSyk, sisteHmapFrist);
    }
    


    // lag en hashmap listeMedSannhet som ta vare paa alle filnavn og sannhetverdi i metadata
    public static HashMap<String, Boolean> LagOverordnetListeFraMeta() {
        HashMap<String, Boolean> overordnetListe = new HashMap<>();

        try {
            Scanner leser = new Scanner(new File(metadataFilbane));
            // legge alle gyldig filnavn og sannhet i metadata.txt til overordnetListe
            while (leser.hasNextLine()) {
                String linje = leser.nextLine().trim();
                if (linje.indexOf("csv") != -1) {
                    String filNavn = linje.split(",")[0];
                    Boolean sannhet = linje.split(",")[1].equals("True");
                    overordnetListe.put(filNavn, sannhet);
                }
            }
            leser.close();

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        System.out.println("\nFerdig med aa lage en overordnetListe: \n" + overordnetListe);
        return overordnetListe;
    }



    public static void leggHmapperTilMonitorer(HashMap<String, Boolean> listeMedSannhet, String mappe)
            throws InterruptedException {
        for (String filNavn : listeMedSannhet.keySet()) {
            Boolean erSyke = listeMedSannhet.get(filNavn);

            if (erSyke) {
                Thread traad = new Thread(new LeseTrad(mappe + "/" + filNavn, sykMonitor));
                traad.start();
                traad.join();
            } else {
                Thread traad = new Thread(new LeseTrad(mappe + "/" + filNavn, fristMonitor));
                traad.start();
                traad.join();
            }
        }

    }
    
    
    public static void flettAlt(Monitor2 monitor, int antallFeltting) {
        int teller = 0;
        try {
            //etter hver fletting runde , hvis det er fortsatt flere enn 1 hashmap i monitoren, start neste runde
            while (monitor.antHmap() > 1) {
                // lag flere traad for aa fletter hashmapper, 
                for (int i = 0; i < antallTraader; i++) {
                    teller += 1;
                    if (teller == antallFeltting) {
                        return;
                    }

                    Thread traad = new Thread(new FletteTrad(monitor, count, i));
                    traad.start();
                    traad.join();
                }

            }
            // venter til alle traader i denne runden er ferdig med fletting og
            // settInFlettet.
            count.await();
            System.out.println("Venter til at alle traader i denne runden blir ferdig.");

        } catch (InterruptedException e) {
            System.out.println("Interrupted!");
        }
    }


    public static HashMap<String, Subsekvens> flettTo(ArrayList<HashMap<String, Subsekvens>> listenMedTo) {

        Subsekvens hentaSub;
        HashMap<String, Subsekvens> subSeqHashNy = new HashMap<String, Subsekvens>();

        for (Subsekvens sub1 : listenMedTo.get(0).values()) {
            hentaSub = listenMedTo.get(1).remove(sub1.hentSubNavn());

            if (hentaSub == null) {
                subSeqHashNy.put(sub1.hentSubNavn(), sub1);

            } else {
                int ant = hentaSub.antallFrkmster();
                sub1.leggTilAnt(ant);
                subSeqHashNy.put(sub1.hentSubNavn(), sub1);
            }
        }

        for (Subsekvens sub2 : listenMedTo.get(1).values()) {
            subSeqHashNy.put(sub2.hentSubNavn(), sub2);
        }
        return subSeqHashNy;
    }

    
    public static void FinnDominSyv(HashMap<String, Subsekvens> sisteHmapSyk, 
            HashMap<String, Subsekvens> sisteHmapFrist) {
        boolean fantNoe = false;
        HashMap<String, Subsekvens> flettetHmap = flettSykOgFristHmap(sisteHmapSyk, sisteHmapFrist);

        System.out.println("Dominante subsekvenser som  har 7 eller flere forekomster blant de syke proeve: ");
        // hent ut den eneste hashmappen i monitoren og iterer alle Subsekvens i hashmappen
        for (Subsekvens sub : flettetHmap.values()) {
            if (sub.antallFrkmster() >= 7) {
                System.out.println(sub);
                fantNoe = true;

            }
        }
        if (!fantNoe) {
            System.out.println("Ingen subsekvenser har 7 eller flere forekomster blant de syke proeve i denne testfil.");
        }
    }
    
    public static void FinnDominMaks(HashMap<String, Subsekvens> sisteHmapSyk,
            HashMap<String, Subsekvens> sisteHmapFrist) {
        int maks = 0;
        HashMap<String, Integer> maksListen = new HashMap<>();
        HashMap<String, Subsekvens> flettetHmap = flettSykOgFristHmap(sisteHmapSyk, sisteHmapFrist);

        for (Subsekvens s : flettetHmap.values()) {
            if (s.antallFrkmster() >= maks) {
                maks = s.antallFrkmster();
            }
        }

        for (Subsekvens s : flettetHmap.values()) {
            if (s.antallFrkmster() == maks) {
                maksListen.put(s.hentSubNavn(), maks);
            }
        }

        System.out.println("\nDominante subsekvenser som  har flest forekomst blant de syke proeve: " + maksListen);
    }
    
    
    // metoden skal feltte den siste syk hashmap og den siste frist hashmap
    // antall i subsekvens object skal vaere syk antall minus frisk antall
    public static HashMap<String, Subsekvens> flettSykOgFristHmap(HashMap<String, Subsekvens> sisteHmapSyk,
            HashMap<String, Subsekvens> sisteHmapFrist) {

        Subsekvens hentaSub;
        HashMap<String, Subsekvens> subSeqHashNy = new HashMap<String, Subsekvens>();

        for (Subsekvens subSyke : sisteHmapSyk.values()) {
            hentaSub = sisteHmapFrist.remove(subSyke.hentSubNavn());

            if (hentaSub == null) {
                subSeqHashNy.put(subSyke.hentSubNavn(), subSyke);
            } else {
                int ant = hentaSub.antallFrkmster();
                // her blir det minus 
                subSyke.minusAnt(ant);
                subSeqHashNy.put(subSyke.hentSubNavn(), subSyke);
            }
        }
        for (Subsekvens subFrist : sisteHmapFrist.values()) {
            subSeqHashNy.put(subFrist.hentSubNavn(), subFrist);
        }
        return subSeqHashNy;
    }
    
    
}
