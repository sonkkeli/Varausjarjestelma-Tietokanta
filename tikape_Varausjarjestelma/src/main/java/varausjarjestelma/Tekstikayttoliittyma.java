package varausjarjestelma;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Tekstikayttoliittyma {
    
    @Autowired
    private Varausjarjestelma varausjar;

    public void kaynnista(Scanner lukija) {
        while (true) {
            System.out.println("Komennot: ");
            System.out.println(" x - lopeta");
            System.out.println(" 1 - lisaa huone");
            System.out.println(" 2 - listaa huoneet");
            System.out.println(" 3 - hae huoneita");
            System.out.println(" 4 - lisaa varaus");
            System.out.println(" 5 - listaa varaukset");
            System.out.println(" 6 - tilastoja");
            System.out.println("");

            String komento = lukija.nextLine();
            if (komento.equals("x")) {
                break;
            }

            if (komento.equals("1")) {
                lisaaHuone(lukija);
            } else if (komento.equals("2")) {
                listaaHuoneet();
            } else if (komento.equals("3")) {
                haeHuoneita(lukija);
            } else if (komento.equals("4")) {
                lisaaVaraus(lukija);
            } else if (komento.equals("5")) {
                listaaVaraukset();
            } else if (komento.equals("6")) {
                tilastoja(lukija);
            }
        }
    }

    private void lisaaHuone(Scanner s) {
        System.out.println("Lisätään huone");
        System.out.println("");

        System.out.println("Minkä tyyppinen huone on?");
        String tyyppi = s.nextLine();
        System.out.println("Mikä huoneen numeroksi asetetaan?");
        int numero = Integer.valueOf(s.nextLine());
        System.out.println("Kuinka monta euroa huone maksaa yöltä?");
        int hinta = Integer.valueOf(s.nextLine());
        varausjar.lisaaHuone(tyyppi, numero, hinta);
    }

    private void listaaHuoneet() {
        System.out.println("Listataan huoneet");
        System.out.println("");
        List<String> huonelista = varausjar.listaaHuoneet();
        for (String huone : huonelista){
            System.out.println(huone);
        }
        System.out.println("");
    }

    private void haeHuoneita(Scanner s) {
        System.out.println("Haetaan huoneita");
        System.out.println("");

        System.out.println("Milloin varaus alkaisi (yyyy-MM-dd)?");;
        String alku = s.nextLine();
        System.out.println("Milloin varaus loppuisi (yyyy-MM-dd)?");
        String loppu = s.nextLine();
        System.out.println("Minkä tyyppinen huone? (tyhjä = ei rajausta)");
        String tyyppi = s.nextLine();
        System.out.println("Minkä hintainen korkeintaan? (tyhjä = ei rajausta)");
        String maksimihinta = s.nextLine();
        
        List<String> huoneet = varausjar.haeHuoneita(alku, loppu, tyyppi, maksimihinta);
        
        if (huoneet.size()>0){
            System.out.println("Vapaat huoneet: ");
            for (String huone : huoneet){
                System.out.println(huone);
            }
        } else {
            System.out.println("Ei vapaita huoneita.");
        }
        System.out.println("");
    }

    private void lisaaVaraus(Scanner s) {
        System.out.println("Haetaan huoneita");
        System.out.println("");

        System.out.println("Milloin varaus alkaisi (yyyy-MM-dd)?");;
        String alku = s.nextLine();
        System.out.println("Milloin varaus loppuisi (yyyy-MM-dd)?");
        String loppu = s.nextLine();
        System.out.println("Minkä tyyppinen huone? (tyhjä = ei rajausta)");
        String tyyppi = s.nextLine();
        System.out.println("Minkä hintainen korkeintaan? (tyhjä = ei rajausta)");
        String maksimihinta = s.nextLine();
        
        List<String> huoneet = varausjar.haeHuoneita(alku, loppu, tyyppi, maksimihinta);
        
        if (huoneet.size()>0){
            System.out.println("Huoneita vapaana: " + huoneet.size());
            System.out.println("");
            
            int huoneita = -1;
            while (true) {
                System.out.println("Montako huonetta varataan?");
                huoneita = Integer.valueOf(s.nextLine());
                if (huoneita >= 1 && huoneita <= huoneet.size()) {
                    break;
                }
                System.out.println("Epäkelpo huoneiden lukumäärä.");
            }
            
            List<String> lisavarusteet = new ArrayList<>();
            while (true) {
                System.out.println("Syötä lisävaruste, tyhjä lopettaa");
                String lisavaruste = s.nextLine();
                if (lisavaruste.isEmpty()) {
                    break;
                }
                lisavarusteet.add(lisavaruste);
            }
            
            System.out.println("Syötä varaajan nimi:");
            String nimi = s.nextLine();
            System.out.println("Syötä varaajan puhelinnumero:");
            String puhelinnumero = s.nextLine();
            System.out.println("Syötä varaajan sähköpostiosoite:");
            String sahkoposti = s.nextLine();
            
            varausjar.lisaaVaraus(huoneet, huoneita, alku, loppu, nimi, puhelinnumero, sahkoposti, lisavarusteet);
            
        } else {
            System.out.println("Ei vapaita huoneita.");            
        }
    }

    private void listaaVaraukset() {
        System.out.println("Listataan varaukset");
        System.out.println("");
        varausjar.listaaVaraukset();
    }

    private void tilastoja(Scanner lukija) {
        System.out.println("Mitä tilastoja tulostetaan?");
        System.out.println("");

        // tilastoja pyydettäessä käyttäjältä kysytään tilasto
        System.out.println(" 1 - Suosituimmat lisävarusteet");
        System.out.println(" 2 - Parhaat asiakkaat");
        System.out.println(" 3 - Varausprosentti huoneittain");
        System.out.println(" 4 - Varausprosentti huonetyypeittäin");

        System.out.println("Syötä komento: ");
        int komento = Integer.valueOf(lukija.nextLine());

        if (komento == 1) {
            suosituimmatLisavarusteet();
        } else if (komento == 2) {
            parhaatAsiakkaat();
        } else if (komento == 3) {
            varausprosenttiHuoneittain(lukija);
        } else if (komento == 4) {
            varausprosenttiHuonetyypeittain(lukija);
        }
    }

    private void suosituimmatLisavarusteet() {
        System.out.println("Tulostetaan suosituimmat lisävarusteet");
        System.out.println("");

        // alla oletetaan, että lisävarusteita on vain muutama
        // mikäli tietokannassa niitä on enemmän, tulostetaan 10 suosituinta
        System.out.println("Teekannu, 2 varausta");
        System.out.println("Kahvinkeitin, 2 varausta");
        System.out.println("Silitysrauta, 1 varaus");
    }

    private void parhaatAsiakkaat() {
        System.out.println("Tulostetaan parhaat asiakkaat");
        System.out.println("");

        // alla oletetaan, että asiakkaita on vain 2
        // mikäli tietokannassa niitä on enemmän, tulostetaan asiakkaita korkeintaan 10
        System.out.println("Anssi Asiakas, anssi@asiakas.net, +358441231234, 1323 euroa");
        System.out.println("Essi Esimerkki, essi@esimerkki.net, +358443214321, 229 euroa");
    }

    private void varausprosenttiHuoneittain(Scanner lukija) {
        System.out.println("Tulostetaan varausprosentti huoneittain");
        System.out.println("");

        System.out.println("Mistä lähtien tarkastellaan?");
        LocalDateTime alku = LocalDateTime.parse(lukija.nextLine() + "-01 " + "16:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        System.out.println("Mihin asti tarkastellaan?");
        LocalDateTime loppu = LocalDateTime.parse(lukija.nextLine() + "-01 " + "10:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        // alla esimerkkitulostus
        System.out.println("Tulostetaan varausprosentti huoneittain");
        System.out.println("Excelsior, 604, 119 euroa, 0.0%");
        System.out.println("Excelsior, 605, 119 euroa, 0.0%");
        System.out.println("Superior, 705, 159 euroa, 22.8%");
        System.out.println("Commodore, 128, 229 euroa, 62.8%");
    }

    private void varausprosenttiHuonetyypeittain(Scanner lukija) {
        System.out.println("Tulostetaan varausprosentti huonetyypeittäin");
        System.out.println("");

        System.out.println("Mistä lähtien tarkastellaan?");
        LocalDateTime alku = LocalDateTime.parse(lukija.nextLine() + "-01 " + "16:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        System.out.println("Mihin asti tarkastellaan?");
        LocalDateTime loppu = LocalDateTime.parse(lukija.nextLine() + "-01 " + "10:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        // alla esimerkkitulostus
        System.out.println("Tulostetaan varausprosentti huonetyypeittän");
        System.out.println("Excelsior, 0.0%");
        System.out.println("Superior, 22.8%");
        System.out.println("Commodore, 62.8%");
    }

}
