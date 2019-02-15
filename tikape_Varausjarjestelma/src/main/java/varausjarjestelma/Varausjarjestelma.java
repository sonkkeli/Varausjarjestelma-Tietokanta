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
        jbtemp.update("INSERT INTO Hotellihuone (numero, tyyppi, hinta) VALUES (?,?,?);", numero, tyyppi, hinta);
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
        LocalDate alotus = LocalDate.parse(alku);
        LocalDate lopetus = LocalDate.parse(loppu);
        int kesto = 0;
        while (!alotus.isEqual(lopetus)){
            alotus = alotus.plusDays(1);
            kesto++;
        }
        String kestoString = kesto + " päivä";
        if (kesto > 1){
            kestoString += "ä";
        }
        
        String lisavarusteita = "0 lisävarustetta";
        if (lisavarusteet.size() == 1){
            lisavarusteita = "1 lisävaruste";
        } else if (lisavarusteet.size()>0){
            lisavarusteita = lisavarusteet.size() + " lisävarustetta";
        }
        String huonelkm = "1 huone";
        if (huoneita > 1){
            huonelkm = huoneita + " huonetta";
        }
        
        // lisätään varaus ja sen tiedot
        jbtemp.update("INSERT INTO Varaus (id, alkupvm, loppupvm, kesto, nimi, "
                + "puhnro, sahkoposti, lisavarusteita, huoneita) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);", 
                seuraavaVarausId, alku, loppu, kestoString, nimi, puhelinnumero, 
                sahkoposti, lisavarusteita, huonelkm);
        
        // lisätään lisävarusteet
        for (String lisavaruste : lisavarusteet){
            jbtemp.update("INSERT INTO Lisavaruste (varaus_id, lisavaruste) "
                    + "VALUES (?, ?);", seuraavaVarausId, lisavaruste);
        }
        
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
                jbtemp.update("INSERT INTO Varauskalenteri (hotellihuone_numero, pvm) VALUES (?,?);", huoneenNumero, alkupvm);
                alkupvm = alkupvm.plusDays(1);
                // System.out.println("jäit luuppiin pelle");
            }
            i++;
        }        
        seuraavaVarausId++;
    }
    
    public void listaaVaraukset(){
        
        List<String> varaukset = new ArrayList<>();
        
        varaukset = jbtemp.query(
                "SELECT * FROM Varaus ORDER BY id;",
                (rs, rowNum) -> 
                        rs.getString("nimi")+ ", " + 
                        rs.getString("sahkoposti") + ", " + 
                        rs.getString("alkupvm") + ", " + 
                        rs.getString("loppupvm") + ", " + 
                        rs.getString("kesto") + ", " + 
                        rs.getString("lisavarusteita") + ", " + 
                        rs.getString("huoneita") + ". Huoneet:");
        
        
        int idnumero = 1;
        while ((idnumero -1) < varaukset.size()){
            
            System.out.println(varaukset.get(idnumero-1));
            
            List<String> hotellihuoneet = jbtemp.query(
                "SELECT tyyppi, numero, hinta FROM Hotellihuone "
                        + "JOIN VarausHuone ON VarausHuone.hotellihuone_numero = Hotellihuone.numero "
                        + "WHERE varaus_id = ?;",
                (rs, rowNum) -> 
                        rs.getString("tyyppi")+ ", " + 
                        rs.getString("numero") + ", " + 
                        rs.getString("hinta") + " euroa", idnumero);
            
            int summa = 0 ;
            for (String hotellihuone : hotellihuoneet){
                System.out.println("\t" + hotellihuone);
                
                String[] palat = hotellihuone.split(", ");
                String[] hintaeuroa = palat[2].split(" ");
                int hinta = Integer.valueOf(hintaeuroa[0]);
                summa += hinta;
            }
            System.out.println("\tYhteensä: " + summa + " euroa");
            System.out.println("");
            idnumero++;
        }
    }
}
