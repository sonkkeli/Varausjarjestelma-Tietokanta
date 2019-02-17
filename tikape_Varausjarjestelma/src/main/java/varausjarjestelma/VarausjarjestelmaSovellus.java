package varausjarjestelma;

import java.sql.*;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class VarausjarjestelmaSovellus implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(VarausjarjestelmaSovellus.class);
    }

    @Autowired
    Tekstikayttoliittyma tekstikayttoliittyma;

    @Override
    public void run(String... args) throws Exception {
        Scanner lukija = new Scanner(System.in);
        // alustetaan tietokanta näillä tarvittaessa
        luoHotellihuone();
        luoVarauskalenteri();
        luoVaraus();
        luoLisavarusteet();        
        luoVarausHuone();
        
        tekstikayttoliittyma.kaynnista(lukija);
    }

    private static void luoHotellihuone(){
        try (Connection conn = DriverManager.getConnection("jdbc:h2:./hotelliketju", "sa", "")) {

            String createTableHotellihuone = "CREATE TABLE Hotellihuone("
                + "numero INTEGER PRIMARY KEY NOT NULL, "
                + "tyyppi VARCHAR(25) NOT NULL, "
                + "hinta INTEGER NOT NULL );";
            conn.prepareStatement("DROP TABLE Hotellihuone IF EXISTS;").executeUpdate();
            conn.prepareStatement(createTableHotellihuone).executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(VarausjarjestelmaSovellus.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            System.out.println("Hotellihuone luominen onnistui.");
        }
    }
    
    private static void luoVaraus(){
        try (Connection conn = DriverManager.getConnection("jdbc:h2:./hotelliketju", "sa", "")) {
            
        String createTableVaraus = "CREATE TABLE Varaus("
                + "id INTEGER PRIMARY KEY, "
                + "alkupvm DATE NOT NULL, "
                + "loppupvm DATE NOT NULL, "
                + "kesto VARCHAR(15), "
                + "nimi VARCHAR(100) NOT NULL, "
                + "puhnro VARCHAR(25) NOT NULL, "
                + "sahkoposti VARCHAR(50) NOT NULL, "
                + "lisavarusteita VARCHAR(20), "
                + "huoneita VARCHAR(15) NOT NULL );";
            conn.prepareStatement("DROP TABLE Varaus IF EXISTS;").executeUpdate();
            conn.prepareStatement(createTableVaraus).executeUpdate();
            
        } catch (SQLException ex) {
            Logger.getLogger(VarausjarjestelmaSovellus.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            System.out.println("Varaus luominen onnistui.");
        }
    }
        
    private static void luoLisavarusteet(){
        try (Connection conn = DriverManager.getConnection("jdbc:h2:./hotelliketju", "sa", "")) {
            
        String createTableLisavarusteet = "CREATE TABLE Lisavaruste("
                + "varaus_id INTEGER NOT NULL, "
                + "lisavaruste VARCHAR(255) NOT NULL, "
                + "FOREIGN KEY (varaus_id) REFERENCES Varaus(id) );" ;
            conn.prepareStatement("DROP TABLE Lisavaruste IF EXISTS;").executeUpdate();
            conn.prepareStatement(createTableLisavarusteet).executeUpdate();
            
        } catch (SQLException ex) {
            Logger.getLogger(VarausjarjestelmaSovellus.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            System.out.println("Lisävaruste luominen onnistui.");
        }
    }
        
    private static void luoVarauskalenteri(){
        try (Connection conn = DriverManager.getConnection("jdbc:h2:./hotelliketju", "sa", "")) {
            
        String createTableVarauskalenteri = "CREATE TABLE Varauskalenteri("
                + "hotellihuone_numero INTEGER NOT NULL, "
                + "pvm DATE NOT NULL, "
                + "FOREIGN KEY (hotellihuone_numero) REFERENCES Hotellihuone(numero));" ;
            conn.prepareStatement("DROP TABLE Varauskalenteri IF EXISTS;").executeUpdate();
            conn.prepareStatement(createTableVarauskalenteri).executeUpdate();
            
        } catch (SQLException ex) {
            Logger.getLogger(VarausjarjestelmaSovellus.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            System.out.println("Varauskalenteri luominen onnistui.");
        }
    }
    
    private static void luoVarausHuone(){
        try (Connection conn = DriverManager.getConnection("jdbc:h2:./hotelliketju", "sa", "")) {
            
        String createTableVarausHuone = "CREATE TABLE VarausHuone("
                + "varaus_id INTEGER NOT NULL, "
                + "hotellihuone_numero INTEGER NOT NULL, "
                + "PRIMARY KEY (varaus_id,hotellihuone_numero), "
                + "FOREIGN KEY (varaus_id) REFERENCES Varaus(id), "
                + "FOREIGN KEY (hotellihuone_numero) REFERENCES Hotellihuone(numero));" ;
            conn.prepareStatement("DROP TABLE VarausHuone IF EXISTS;").executeUpdate();
            conn.prepareStatement(createTableVarausHuone).executeUpdate();
            
        } catch (SQLException ex) {
            Logger.getLogger(VarausjarjestelmaSovellus.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            System.out.println("VarausHuone luominen onnistui.");
        }
    }

}
