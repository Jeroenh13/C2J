/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package stamboom.storage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import stamboom.domain.Administratie;
import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.ObservableList;
import stamboom.domain.Geslacht;
import stamboom.domain.Gezin;
import stamboom.domain.Persoon;
import stamboom.util.StringUtilities;

public class DatabaseMediator implements IStorageMediator {

    private Properties props;
    private Connection conn;

    public DatabaseMediator(Properties props) {
        configure(props);
    }

    @Override
    public Administratie load() throws IOException {
        if (!isCorrectlyConfigured()) {
            throw new RuntimeException("Configuratie is niet correct.");
        }

        ArrayList<Integer> ouders = new ArrayList<>();

        Administratie admin = new Administratie();
        try {
            initConnection();
            Statement stat = conn.createStatement();
            String getPersonen = "SELECT * FROM PERSONEN";
            String getGezinnen = "SELECT * FROM GEZINNEN";
            
            //ophalen van personen
            ResultSet rsp = stat.executeQuery(getPersonen);
            while (rsp.next()) {
                String anaam = rsp.getString("achternaam");
                String vnamen[] = rsp.getString("voornamen").split(" ");
                String tvoegsel = rsp.getString("tussenvoegsel");
                Calendar gebDat = StringUtilities.datum(rsp.getString("geboortedatum"));
                String gebPlaats = rsp.getString("geboorteplaats");
                String geslacht = rsp.getString("geslacht");
                Geslacht g;
                if ("MAN".equals(geslacht)) {
                    g = Geslacht.MAN;
                } else {
                    g = Geslacht.VROUW;
                }

                //Get de int van de ouders, deze al later worden gebruikt om de ouders toe te voegen aan de persoon
                ouders.add(rsp.getInt("ouders"));

                admin.addPersoon(g, vnamen, anaam, tvoegsel, gebDat, gebPlaats, null);
            }

            //ophalen van gezinnen
            ResultSet rsg = stat.executeQuery(getGezinnen);
            while (rsg.next()) {
                Persoon Ouder1 = admin.getPersoon(rsg.getInt("Ouder1"));
                Persoon Ouder2 = admin.getPersoon(rsg.getInt("Ouder2"));
                Calendar huwDat = StringUtilities.datum(rsg.getString("huwelijksdatum"));
                Calendar scheiDat = StringUtilities.datum(rsg.getString("scheidingsdatum"));

                if (huwDat != null) {
                    Gezin tmpGezin = admin.addHuwelijk(Ouder1, Ouder2, huwDat);
                    if (scheiDat != null) {
                        admin.setScheiding(tmpGezin, scheiDat);
                    }
                } else {
                    admin.addOngehuwdGezin(Ouder1, Ouder2);
                }
            }

            //ouders updaten
            for (int i = 0; i < ouders.size(); i++) {
                Gezin g = admin.getGezin(ouders.get(i));
                if (g != null) {
                    admin.setOuders(admin.getPersoon(i+1), g);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseMediator.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            closeConnection();
        }
        return admin;
    }

    @Override
    public void save(Administratie admin) throws IOException {
        if (!isCorrectlyConfigured()) {
            throw new RuntimeException("Configuratie is niet correct.");
        }
        try {
            initConnection();

            ClearDB(admin.getPersonen(), admin.getGezinnen());

            //Insert van alle personen, ouderlijk gezin is altijd null
            PreparedStatement persoonInsert = conn.prepareStatement("INSERT INTO PERSONEN VALUES(?,?,?,?,?,?,?,?);");
            List<Persoon> personen = admin.getPersonen();
            for (Persoon p : personen) {
                persoonInsert.setInt(1, p.getNr());
                persoonInsert.setString(2, p.getAchternaam());
                persoonInsert.setString(3, p.getVoornamen());
                if (p.getTussenvoegsel() == null) {
                    persoonInsert.setString(4, "");
                } else {
                    persoonInsert.setString(4, p.getTussenvoegsel());
                }
                persoonInsert.setString(5, StringUtilities.datumString(p.getGebDat()));
                persoonInsert.setString(6, p.getGebPlaats());
                if (p.getGeslacht().equals(Geslacht.MAN)) {
                    persoonInsert.setString(7, "MAN");
                } else {
                    persoonInsert.setString(7, "VROUW");
                }
                persoonInsert.setNull(8, java.sql.Types.NULL);
                persoonInsert.executeUpdate();
            }

            //Insert van de gezinnen
            PreparedStatement gezinInsert = conn.prepareStatement("INSERT INTO GEZINNEN VALUES(?,?,?,?,?);");
            List<Gezin> gezinnen = admin.getGezinnen();
            for (Gezin g : gezinnen) {
                gezinInsert.setInt(1, g.getNr());
                gezinInsert.setInt(2, g.getOuder1().getNr());
                if (g.getOuder2() == null) {
                    gezinInsert.setNull(3, java.sql.Types.NULL);
                } else {
                    gezinInsert.setInt(3, g.getOuder2().getNr());
                }
                if (g.getHuwelijksdatum() == null) {
                    gezinInsert.setNull(4, java.sql.Types.NULL);
                } else {
                    gezinInsert.setString(4, StringUtilities.datumString(g.getHuwelijksdatum()));
                }
                if (g.getScheidingsdatum() == null) {
                    gezinInsert.setNull(5, java.sql.Types.NULL);
                } else {
                    gezinInsert.setString(5, StringUtilities.datumString(g.getScheidingsdatum()));
                }
                gezinInsert.executeUpdate();
            }

            //Update de personen met een ouderlijk gezin
            for (Persoon p : personen) {
                PreparedStatement persoonUpdate = conn.prepareStatement("UPDATE PERSONEN SET OUDERS=? WHERE PERSOONSNUMMER=?;");
                if (p.getOuderlijkGezin() != null) {
                    persoonUpdate.setInt(1, p.getOuderlijkGezin().getNr());
                    persoonUpdate.setInt(2, p.getNr());
                    persoonUpdate.executeUpdate();
                }
            }

        } catch (SQLException ex) {
            Logger.getLogger(DatabaseMediator.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            closeConnection();
        }
    }

    /**
     * Laadt de instellingen, in de vorm van een Properties bestand, en
     * controleert of deze in de correcte vorm is, en er verbinding gemaakt kan
     * worden met de database.
     *
     * @param props
     * @return
     */
    @Override
    public final boolean configure(Properties props) {
        this.props = props;
        if (!isCorrectlyConfigured()) {
            System.err.println("props mist een of meer keys");
            return false;
        }

        try {
            initConnection();
            return true;
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
            this.props = null;
            return false;
        } finally {
            closeConnection();
        }
    }

    @Override
    public Properties config() {
        return props;
    }

    @Override
    public boolean isCorrectlyConfigured() {
        if (props == null) {
            return false;
        }
        if (!props.containsKey("driver")) {
            return false;
        }
        if (!props.containsKey("url")) {
            return false;
        }
        if (!props.containsKey("username")) {
            return false;
        }
        if (!props.containsKey("password")) {
            return false;
        }
        return true;
    }

    private void initConnection() throws SQLException {
        conn = null;
        try {
            conn = DriverManager.getConnection(props.getProperty("url"), props.getProperty("username"), props.getProperty("password"));
        } catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }
    }

    private void closeConnection() {
        try {
            conn.close();
            conn = null;
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
    }

    private void ClearDB(ObservableList<Persoon> personen, ObservableList<Gezin> gezinnen) throws SQLException {
        initConnection();
        PreparedStatement persoonDelete = conn.prepareStatement("DELETE FROM PERSONEN WHERE PERSOONSNUMMER=?;");
        PreparedStatement gezinDelete = conn.prepareStatement("DELETE FROM GEZINNEN WHERE GEZINSNUMMER=?");

        for (Gezin g : gezinnen) {
            gezinDelete.setInt(1, g.getNr());
            gezinDelete.executeUpdate();
        }

        for (Persoon p : personen) {
            persoonDelete.setInt(1, p.getNr());
            persoonDelete.executeUpdate();
        }

    }
}
