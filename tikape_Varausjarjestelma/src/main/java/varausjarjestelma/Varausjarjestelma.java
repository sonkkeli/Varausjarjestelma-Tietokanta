package varausjarjestelma;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import java.sql.*;
import java.time.LocalDate;
import org.springframework.stereotype.Component;

/**
 *
 * @author sonja
 */
@Component
public class Varausjarjestelma {
    
    private int seuraavaVarausId = 1;

    @Autowired
    private JdbcTemplate jbtemp;
    
    // tänne kaikki tietokantaa sorkkivat metodit, joita kutsutaan tekstikäyttöliittymästä
    
    public void lisaaHuone(String tyyppi, int numero, int hinta){
        jbtemp.update("INSERT INTO Hotellihuone (numero, tyyppi, hinta) "
                + "VALUES (?,?,?);", numero, tyyppi, hinta);
    }
    
    public List<String> listaaHuoneet(){
        List<String> lista = new ArrayList<>();
        
        lista = jbtemp.query(
                "SELECT * FROM Hotellihuone;",
                (rs, rowNum) -> 
                        rs.getString("tyyppi")+ ", " + 
                        rs.getString("numero") + ", " + 
                        rs.getString("hinta") + " euroa");
        return lista;
    }
    
    public List<String> haeHuoneita(String alku, String loppu, String tyyppi, String maksimihinta){
        List<String> lista = new ArrayList<>();
        
        if (maksimihinta.isEmpty()){
            maksimihinta = "100000";
        }
        int maxhinta = Integer.valueOf(maksimihinta);
        
        // Komennot listaavat kaikki vapaat huoneet määritetyillä ehdoilla rajattuna ja
        // antaa tulokset hintajärjestyksessä kallein ensin, mikä helpottaa myöhemmin
        
        if (tyyppi.isEmpty()){
            lista = jbtemp.query(
                "SELECT tyyppi, numero, hinta FROM Hotellihuone "
                + "WHERE hinta <= ? AND "
                + "numero NOT IN (SELECT hotellihuone_numero FROM Varauskalenteri "
                + "WHERE pvm >= ? AND pvm < ?) "
                + "GROUP BY numero ORDER BY hinta DESC;",
                (rs, rowNum) -> 
                        rs.getString("tyyppi")+ ", " + 
                        rs.getString("numero") + ", " + 
                        rs.getString("hinta") + " euroa", maxhinta, alku, loppu);
        } else {
            lista = jbtemp.query(
                "SELECT tyyppi, numero, hinta FROM Hotellihuone "
                + "WHERE tyyppi = ? AND hinta <= ? AND "
                + "numero NOT IN (SELECT hotellihuone_numero FROM Varauskalenteri "
                + "WHERE pvm >= ? AND pvm < ?) "
                + "GROUP BY numero ORDER BY hinta DESC;",
                (rs, rowNum) -> 
                        rs.getString("tyyppi")+ ", " + 
                        rs.getString("numero") + ", " + 
                        rs.getString("hinta") + " euroa", tyyppi, maxhinta, alku, loppu);
        }
        return lista;
    }
    
    public void lisaaVaraus(List<String> huoneet, int huoneita, String alku, String loppu, 
        String nimi, String puhelinnumero, String sahkoposti, List<String> lisavarusteet){
        
        // lasketaan kesto päivinä:
        int kesto = laskePaivatValilla(alku, loppu);
        // lasketaan lisävarusteiden lukumäärä
        int lisavarusteitaLkm = lisavarusteet.size();
        
        // lisätään varaus ja sen tiedot
        jbtemp.update("INSERT INTO Varaus (id, alkupvm, loppupvm, kesto, nimi, "
                + "puhnro, sahkoposti, lisavarusteita, huoneita) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);", 
                seuraavaVarausId, alku, loppu, kesto, nimi, puhelinnumero, 
                sahkoposti, lisavarusteitaLkm, huoneita);
        
        // lisätään lisävarusteet
        for (String lisavaruste : lisavarusteet){
            jbtemp.update("INSERT INTO Lisavaruste (varaus_id, lisavaruste) "
                    + "VALUES (?, ?);", seuraavaVarausId, lisavaruste);
        }
        
        // lisätään tiedot yhdistelmätauluun VarausHuone sekä varauskalenteriin
        int i = 0;
        while (i < huoneita){
            // selvitetään kalleimman ehdot täyttävän huoneen numero
            String kalleimmanHuoneenTiedot = huoneet.get(i);
            String[] palat = kalleimmanHuoneenTiedot.split(", ");
            int huoneenNumero = Integer.valueOf(palat[1]);
            
            // lisätään tiedot yhdistelmätauluun 
            jbtemp.update("INSERT INTO VarausHuone (varaus_id, hotellihuone_numero) "
                    + "VALUES (?,?);", seuraavaVarausId, huoneenNumero);
            
            LocalDate alkupvm = LocalDate.parse(alku);
            LocalDate loppupvm = LocalDate.parse(loppu);
            
            // lisätään tiedot varauskalenteriin
            while (!alkupvm.isEqual(loppupvm)){
                jbtemp.update("INSERT INTO Varauskalenteri (hotellihuone_numero, pvm) "
                        + "VALUES (?,?);", huoneenNumero, alkupvm);
                alkupvm = alkupvm.plusDays(1);
                // System.out.println("jäit luuppiin pelle");
            }
            i++;
        }        
        seuraavaVarausId++;
    }
    
    public void listaaVaraukset(){
        
        // haetaan varausten tiedot
        List<String> varaukset = new ArrayList<>();
        varaukset = jbtemp.query(
                "SELECT * FROM Varaus ORDER BY id;",
                (rs, rowNum) -> 
                        rs.getString("nimi")+ ";" + 
                        rs.getString("sahkoposti") + ";" + 
                        rs.getString("alkupvm") + ";" + 
                        rs.getString("loppupvm") + ";" + 
                        rs.getString("kesto") + ";" + 
                        rs.getString("lisavarusteita") + ";" + 
                        rs.getString("huoneita"));
        
        // tulostetaan jokaisen varauksen hotellihuoneiden tiedot
        int idnumero = 1;
        while ((idnumero -1) < varaukset.size()){
            String[] palat = varaukset.get(idnumero-1).split(";");
            String tuloste = palat[0] + ", " + palat[1] + ", " + palat[2] 
                    + ", "  + palat[3] + ", " + palat[4] + " päivä";
            
            if (Integer.valueOf(palat[4]) > 1){
                tuloste += "ä";
            }
            
            tuloste += ", " + palat[5] + " lisävaruste";
            
            if (Integer.valueOf(palat[5]) != 1){
                tuloste += "tta";
            }
            
            tuloste += ", " + palat[6] + " huone";
            
            if (Integer.valueOf(palat[6]) != 1){
                tuloste += "tta";
            }
            
            tuloste += ". Huoneet:";
            
            System.out.println(tuloste);
            
            List<String> hotellihuoneet = jbtemp.query(
                "SELECT tyyppi, numero, hinta FROM Hotellihuone "
                + "JOIN VarausHuone ON VarausHuone.hotellihuone_numero = Hotellihuone.numero "
                + "WHERE varaus_id = ?;",
                (rs, rowNum) -> 
                        rs.getString("tyyppi")+ ";" + 
                        rs.getString("numero") + ";" + 
                        rs.getString("hinta"), idnumero);
            
            int summa = 0 ;
            for (String hotellihuone : hotellihuoneet){
                String[] palat1 = hotellihuone.split(";");
                System.out.println("\t" + palat1[0] + ", " + palat1[1] + ", " + palat1[2] + " euroa");
                summa += Integer.valueOf(palat1[2]) * Integer.valueOf(palat[4]);
            }
            System.out.println("\tYhteensä: " + summa + " euroa");
            System.out.println("");
            idnumero++;
        }
    }
    
    private int laskePaivatValilla(String alkupvm, String loppupvm){
        
        LocalDate alkupvmDate = LocalDate.parse(alkupvm);
        LocalDate loppupvmDate = LocalDate.parse(loppupvm);
        int paivatValilla = 0;
        while (!alkupvmDate.isEqual(loppupvmDate)){
            alkupvmDate = alkupvmDate.plusDays(1);
            paivatValilla++;
        }        
        return paivatValilla;        
    }
    
    public List<String> varausprosenttiHuoneittain(String alku, String loppu){
        List<String> lista = new ArrayList<>();
        
        lista = jbtemp.query(
                "SELECT tyyppi, numero, hinta, COUNT(pvm) AS varauksia FROM Hotellihuone "
                + "LEFT OUTER JOIN Varauskalenteri ON "
                + "Varauskalenteri.hotellihuone_numero = Hotellihuone.numero GROUP BY numero;",
                (rs, rowNum) -> 
                        rs.getString("tyyppi")+ ";" + 
                        rs.getString("numero") + ";" + 
                        rs.getString("hinta") + ";" + 
                        rs.getString("varauksia"));
        
        int paivia = laskePaivatValilla(alku,loppu);
        int varauksia = 0;
        double prosentti = 0;
        List<String> palautettavaLista = new ArrayList<>();
        
        for (String rivi : lista){
            String[] palat = rivi.split(";");
            varauksia = Integer.valueOf(palat[3]);
            prosentti = 100 * (double) varauksia / paivia;
            palautettavaLista.add(palat[0] + ", " + palat[1] + ", " + palat [2] + " euroa, " + prosentti + "%");
        }
        
        return palautettavaLista;        
    }
    
    public List<String> varausprosenttiHuonetyypeittain(String alku, String loppu){
        List<String> varaustenLkm = new ArrayList<>();
        List<String> tyyppistenHuoneidenLkm = new ArrayList<>();
        
        varaustenLkm = jbtemp.query(
                "SELECT tyyppi, COUNT(pvm) AS varauksia FROM Hotellihuone "
                + "LEFT OUTER JOIN Varauskalenteri ON "
                + "Varauskalenteri.hotellihuone_numero = Hotellihuone.numero GROUP BY tyyppi ORDER BY tyyppi;",
                (rs, rowNum) -> 
                        rs.getString("tyyppi")+ ";" +
                        rs.getString("varauksia"));
        
        tyyppistenHuoneidenLkm = jbtemp.query(
                "SELECT tyyppi, COUNT(tyyppi) AS tyyppisiaHuoneita FROM Hotellihuone "
                + "GROUP BY tyyppi ORDER BY tyyppi;",
                (rs, rowNum) -> 
                        rs.getString("tyyppi")+ ";" +
                        rs.getString("tyyppisiaHuoneita"));
        
        List<String> palautettavaLista = new ArrayList<>();
        
        if (varaustenLkm.size() != tyyppistenHuoneidenLkm.size()){
            palautettavaLista.add("Virhe, tyyppejä on eri määrä");
            return palautettavaLista;
        }
        
        int paivia = laskePaivatValilla(alku,loppu);
        int varauksia = 0;
        int huoneita = 0;
        double prosentti = 0;
        
        int i = 0;
        while (i < varaustenLkm.size()){
            String[] palatVaraustenLkm = varaustenLkm.get(i).split(";");
            String[] palatTyyppistenHuoneidenLkm = tyyppistenHuoneidenLkm.get(i).split(";");
            varauksia = Integer.valueOf(palatVaraustenLkm[1]);
            huoneita = Integer.valueOf(palatTyyppistenHuoneidenLkm[1]);
            prosentti = 100 * (double) varauksia / (paivia * huoneita);
            palautettavaLista.add(palatVaraustenLkm[0] + ", " + prosentti + "%");
            i++;
        }
        
        return palautettavaLista;
    }
    public List<String> suosituimmatLisavarusteet() {
        List<String> lisavarusteet = new ArrayList<>();
        
        lisavarusteet = jbtemp.query(
                "SELECT lisavaruste, COUNT(lisavaruste) AS lkm FROM Lisavaruste "
                        + "GROUP BY lisavaruste ORDER BY lkm DESC;",
                (rs, rowNum) -> 
                        rs.getString("lisavaruste")+ ";" +
                        rs.getString("lkm"));
        
        List<String> palautettavaLista = new ArrayList<>();
        for (String rivi : lisavarusteet){
            String[] palat = rivi.split(";");
            if (Integer.valueOf(palat[1]) == 1 ){
                palautettavaLista.add(palat[0] + ", " + palat[1] + " varaus");
            } else {
                palautettavaLista.add(palat[0] + ", " + palat[1] + " varausta");
            }            
        }
        return palautettavaLista;
    }
    public List<String> parhaatAsiakkaat(){
        List<String> parhaatAsiakkaat = new ArrayList<>();
        
        parhaatAsiakkaat = jbtemp.query(
                "SELECT id, nimi, sahkoposti, puhnro, kesto * SUM(hinta) AS summa FROM Varaus "
                + "LEFT OUTER JOIN VarausHuone ON VarausHuone.varaus_id = Varaus.id "
                + "LEFT OUTER JOIN Hotellihuone ON VarausHuone.hotellihuone_numero = Hotellihuone.numero "
                + "GROUP BY id ORDER BY summa DESC;",
                (rs, rowNum) -> 
                        rs.getString("nimi")+ ";" +
                        rs.getString("sahkoposti") + ";" + 
                        rs.getString("puhnro") + ";" + 
                        rs.getString("summa"));
        
        List<String> parhaatAsiakkaatTuplatPois = new ArrayList<>();
        System.out.println(parhaatAsiakkaat);
        
        int i = 0; 
        while (i < parhaatAsiakkaat.size()){
            boolean loytyiko = false;
            String[] palat = parhaatAsiakkaat.get(i).split(";");
            int summa = Integer.valueOf(palat[3]);
            
            int j = 0;
            while (j < parhaatAsiakkaatTuplatPois.size()){
                String[] palat2 = parhaatAsiakkaatTuplatPois.get(j).split(", ");
                if (palat2[1].equals(palat[1])){
                    System.out.println("löytyy tupla");
                    String[] palat3 = palat2[3].split(" ");
                    summa += Integer.valueOf(palat3[0]);
                    String uusiArvo = palat[0] + ", " + palat[1] + ", " + palat[2] + ", "  + summa + " euroa";
                    parhaatAsiakkaatTuplatPois.set(j, uusiArvo);
                    loytyiko = true;
                }
                j++;
            }
            
            if (!loytyiko){
                parhaatAsiakkaatTuplatPois.add(palat[0] + ", " + palat[1] + ", " + palat[2] + ", " + summa + " euroa");
            }            
            i++;
        }
        // tässä on nyt vielä sellainen ongelma, että jos asiakkaan nimi toistuu ja summa kasvaa niin, että "ohittaisi" toisen
        // henkilön, näin ei tapahdu, koska listan sorttaus puuttuu... En sitä kuitenkaan osannut enää tehdä. Tässä olisi pitäny
        // sittenkin olla asikastiedot erillisinä, jotta olisi pystynyt tämän tiedon saamaan ulos analyysistä. En kuitenkaan näin
        // loppuvaiheessa projektia lähtenyt enää muokkaamaan asiaa näin, koska muutos olisi ollut niin iso...
        return parhaatAsiakkaatTuplatPois;
    }
}
