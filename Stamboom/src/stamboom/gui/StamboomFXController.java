/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package stamboom.gui;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import stamboom.controller.StamboomController;
import stamboom.domain.Geslacht;
import stamboom.domain.Gezin;
import stamboom.domain.Persoon;
import stamboom.util.StringUtilities;

/**
 *
 * @author frankpeeters
 */
public class StamboomFXController extends StamboomController implements Initializable {

    //MENUs en TABs
    @FXML MenuBar menuBar;
    @FXML MenuItem miNew;
    @FXML MenuItem miOpen;
    @FXML MenuItem miSave;
    @FXML CheckMenuItem cmDatabase;
    @FXML MenuItem miClose;
    @FXML Tab tabPersoon;
    @FXML Tab tabGezin;
    @FXML Tab tabPersoonInvoer;
    @FXML Tab tabGezinInvoer;

    //PERSOON
    @FXML ComboBox cbPersonen;
    @FXML TextField tfPersoonNr;
    @FXML TextField tfVoornamen;
    @FXML TextField tfTussenvoegsel;
    @FXML TextField tfAchternaam;
    @FXML TextField tfGeslacht;
    @FXML TextField tfGebDatum;
    @FXML TextField tfGebPlaats;
    @FXML ComboBox cbOuderlijkGezin;
    @FXML ListView lvAlsOuderBetrokkenBij;
    @FXML Button btStamboom;

    //GEZIN
    @FXML ComboBox cbKiesGezin;
    @FXML TextField tfGezinNr;
    @FXML TextField tfGezinOuder1;
    @FXML TextField tfGezinOuder2;
    @FXML TextField tfGezinHuwelijk;
    @FXML TextField tfGezinScheiding;
    @FXML ListView lvGezinKinderen;

    //INVOER GEZIN
    @FXML ComboBox cbOuder1Invoer;
    @FXML ComboBox cbOuder2Invoer;
    @FXML TextField tfHuwelijkInvoer;
    @FXML TextField tfScheidingInvoer;
    @FXML Button btOKGezinInvoer;
    @FXML Button btCancelGezinInvoer;

    //INVOER PERSOON
    @FXML TextField tfAddVoornamen;
    @FXML TextField tfAddTussenVoegsel;
    @FXML TextField tfAddAchternaam;
    @FXML ComboBox cbAddGeslacht;
    @FXML TextField tfAddGebDatum;
    @FXML TextField tfAddGebPlaats;
    @FXML ComboBox cbAddOuderlijkGezin;
    @FXML Button btPersoonOkInvoer;
    @FXML Button btCancelPersoonInvoer;

    //opgave 4
    private boolean withDatabase;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initComboboxes();
        withDatabase = false;
    }

    private void initComboboxes() {
        cbOuderlijkGezin.setItems(getAdministratie().getGezinnen());
        cbKiesGezin.setItems(getAdministratie().getGezinnen());
        cbOuder1Invoer.setItems(getAdministratie().getPersonen());
        cbOuder2Invoer.setItems(getAdministratie().getPersonen());
        cbPersonen.setItems(getAdministratie().getPersonen());
        cbAddOuderlijkGezin.setItems(getAdministratie().getGezinnen());
    }

    public void selectPersoon(Event evt) {
        Persoon persoon = (Persoon) cbPersonen.getSelectionModel().getSelectedItem();
        showPersoon(persoon);
    }

    private void showPersoon(Persoon persoon) {
        if (persoon == null) {
            clearTabPersoon();
        } else {
            tfPersoonNr.setText(persoon.getNr() + "");
            tfVoornamen.setText(persoon.getVoornamen());
            tfTussenvoegsel.setText(persoon.getTussenvoegsel());
            tfAchternaam.setText(persoon.getAchternaam());
            tfGeslacht.setText(persoon.getGeslacht().toString());
            tfGebDatum.setText(StringUtilities.datumString(persoon.getGebDat()));
            tfGebPlaats.setText(persoon.getGebPlaats());
            if (persoon.getOuderlijkGezin() != null) {
                cbOuderlijkGezin.getSelectionModel().select(persoon.getOuderlijkGezin());
            } else {
                cbOuderlijkGezin.getSelectionModel().clearSelection();
            }

            //todo opgave 3
            if (persoon.getAlsOuderBetrokkenIn() != null) {
                lvAlsOuderBetrokkenBij.setItems(persoon.getAlsOuderBetrokkenIn());
            }
        }
    }

    public void setOuders(Event evt) {
        if (tfPersoonNr.getText().isEmpty()) {
            return;
        }
        Gezin ouderlijkGezin = (Gezin) cbOuderlijkGezin.getSelectionModel().getSelectedItem();
        if (ouderlijkGezin == null) {
            return;
        }

        int nr = Integer.parseInt(tfPersoonNr.getText());
        Persoon p = getAdministratie().getPersoon(nr);
        if (p == ouderlijkGezin.getOuder1() || p == ouderlijkGezin.getOuder2()) {
            showDialog("Warning", "Je kan niet ouder van jezelf worden");
            cbOuderlijkGezin.getSelectionModel().clearSelection();
        } else {
            if (getAdministratie().setOuders(p, ouderlijkGezin)) {
                showDialog("Success", ouderlijkGezin.toString()
                        + " is nu het ouderlijk gezin van " + p.getNaam());
            }
        }
    }

    public void selectGezin(Event evt) {
        // todo opgave 3
        Gezin gezin = (Gezin) cbKiesGezin.getSelectionModel().getSelectedItem();
        showGezin(gezin);

    }

    private void showGezin(Gezin gezin) {
        // todo opgave 3    
        if (gezin == null) {
            clearTabGezin();
        } else {
            tfGezinNr.setText(Integer.toString(gezin.getNr()));
            tfGezinOuder1.setText(gezin.getOuder1().getNaam());
            if (gezin.getOuder2() != null) {
                tfGezinOuder2.setText(gezin.getOuder2().getNaam());
            }
            if (gezin.isOngehuwd()) {
                tfGezinHuwelijk.setText("...");
            } else {
                tfGezinHuwelijk.setText(StringUtilities.datumString(gezin.getHuwelijksdatum()));
            }

            if (gezin.getScheidingsdatum() != null) {
                tfGezinScheiding.setText(StringUtilities.datumString(gezin.getScheidingsdatum()));
            } else {
                tfGezinScheiding.setText("...");
            }
            lvGezinKinderen.setItems((ObservableList) gezin.getKinderen());
        }
    }

    public void setHuwdatum(Event evt) {
        Calendar huwelijk = StringUtilities.datum(tfGezinHuwelijk.getText());
        int gezin = Integer.parseInt(tfGezinNr.getText());
        Gezin g = getAdministratie().getGezin(gezin);
        if (getAdministratie().setHuwelijk(g, huwelijk)) {
            showDialog("Popup", "huwelijk is toegevoegd");
            showGezin(g);
        } else {
            showDialog("Warning", "ongeldige huwelijksdatum.");
        }

    }

    public void setScheidingsdatum(Event evt) {
        Calendar scheiding = StringUtilities.datum(tfGezinScheiding.getText());
        int gezin = Integer.parseInt(tfGezinNr.getText());
        Gezin g = getAdministratie().getGezin(gezin);
        if (getAdministratie().setScheiding(g, scheiding)) {
            showDialog("Popup", "scheiding is toegevoegd");
            showGezin(g);
        } else {
            showDialog("Warning", "ongeldige scheidingsdatum.");
        }

    }

    public void cancelPersoonInvoer(Event evt) {
        clearTabPersoonInvoer();
    }

    public void okPersoonInvoer(Event evt) {
        String vn = tfAddVoornamen.getText();
        String an = tfAddAchternaam.getText();
        String tv = tfAddTussenVoegsel.getText();
        String wp = tfAddGebPlaats.getText();
        String tmpGeslacht;
        Geslacht cbSelected;
        String[] vnamen;

        try {
            tmpGeslacht = cbAddGeslacht.getValue().toString();
            cbSelected = Geslacht.valueOf(tmpGeslacht);
        } catch (Exception e) {
            showDialog("Warning", "U heeft geen geslacht gekozen");
            return;
        }

        if (!vn.matches("[a-zA-Z\\s]+")) {
            showDialog("Warning", "Voornaam is foutief ingevoerd");
            return;
        } else {
            vnamen = vn.split("\\s+");
        }

        if (!an.matches("[a-zA-Z\\s]+")) {
            showDialog("Warning", "Achternaam is foutief ingevoerd");
            return;
        }

        if (!tv.trim().equals("")) {
            if (!tv.matches("[a-zA-Z\\s]+")) {
                showDialog("Warning", "Tussenvoegsel is foutief ingevoerd");
                return;
            }
        }

        if (!wp.matches("[a-zA-Z\\s]+")) {
            showDialog("Warning", "Woonplaats is foutief ingevoerd");
            return;
        }

        Calendar c = Calendar.getInstance();
        DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        try {
            c.setTime(df.parse(tfAddGebDatum.getText()));
        } catch (ParseException ex) {
            showDialog("Warning", "Datum is foutief ingevoerd");
            return;
        }

        Gezin g = (Gezin) cbAddOuderlijkGezin.getSelectionModel().getSelectedItem();
        getAdministratie().addPersoon(cbSelected, vnamen, an, tv, c, wp, g);
        clearTabPersoonInvoer();
    }

    public void okGezinInvoer(Event evt) {
        Persoon ouder1 = (Persoon) cbOuder1Invoer.getSelectionModel().getSelectedItem();
        if (ouder1 == null) {
            showDialog("Warning", "eerste ouder is niet ingevoerd");
            return;
        }
        Persoon ouder2 = (Persoon) cbOuder2Invoer.getSelectionModel().getSelectedItem();
        Calendar huwdatum;
        try {
            huwdatum = StringUtilities.datum(tfHuwelijkInvoer.getText());
        } catch (IllegalArgumentException exc) {
            showDialog("Warning", "huwelijksdatum :" + exc.getMessage());
            return;
        }
        Gezin g;
        if (huwdatum != null) {
            g = getAdministratie().addHuwelijk(ouder1, ouder2, huwdatum);
            if (g == null) {
                showDialog("Warning", "Invoer huwelijk is niet geaccepteerd");
            } else {
                Calendar scheidingsdatum;
                try {
                    scheidingsdatum = StringUtilities.datum(tfScheidingInvoer.getText());
                    if (scheidingsdatum != null) {
                        getAdministratie().setScheiding(g, scheidingsdatum);
                    }
                } catch (IllegalArgumentException exc) {
                    showDialog("Warning", "scheidingsdatum :" + exc.getMessage());
                }
            }
        } else {
            g = getAdministratie().addOngehuwdGezin(ouder1, ouder2);
            if (g == null) {
                showDialog("Warning", "Invoer ongehuwd gezin is niet geaccepteerd");
            }
        }
        clearTabGezinInvoer();
    }

    public void cancelGezinInvoer(Event evt) {
        clearTabGezinInvoer();
    }

    public void showStamboom(Event evt) {
        String s = tfPersoonNr.getText();
        int i = Integer.parseInt(s);
        String stamboom = getAdministratie().getPersoon(i).stamboomAlsString();
        showDialog("Stamboom", stamboom);

    }

    public void createEmptyStamboom(Event evt) {
        this.clearAdministratie();
        clearTabs();
        initComboboxes();
    }

    public void openStamboom(Event evt) {
        try {
            if (withDatabase) {
                loadFromDatabase();
            } else {
                File stamboom = new File("stamboom.stam");
                if (stamboom.exists()) {
                    deserialize(stamboom);
                }
            }
            clearTabs();
            initComboboxes();
            // todo opgave 3
        } catch (IOException ex) {
            showDialog("Error", ex.getLocalizedMessage());
        }
    }

    public void saveStamboom(Event evt) {
        try {
            if (withDatabase) {
                saveToDatabase();
            } else {
                File stamboom = new File("stamboom.stam");
                if (stamboom.exists()) {
                    stamboom.delete();
                }
                serialize(stamboom);
            }
        } catch (IOException ex) {
            showDialog("Error", ex.getLocalizedMessage());
        }
    // todo opgave 3
    }

    public void closeApplication(Event evt) {
        saveStamboom(evt);
        getStage().close();
    }

    public void configureStorage(Event evt) {
        withDatabase = cmDatabase.isSelected();
    }

    public void selectTab(Event evt) {
        Object source = evt.getSource();
        if (source == tabPersoon) {
            clearTabPersoon();
        } else if (source == tabGezin) {
            clearTabGezin();
        } else if (source == tabPersoonInvoer) {
            clearTabPersoonInvoer();
        } else if (source == tabGezinInvoer) {
            clearTabGezinInvoer();
        }
    }

    private void clearTabs() {
        clearTabPersoon();
        clearTabPersoonInvoer();
        clearTabGezin();
        clearTabGezinInvoer();
    }

    private void clearTabPersoonInvoer() {
        tfAddVoornamen.clear();
        tfAddTussenVoegsel.clear();
        tfAddAchternaam.clear();
        cbAddGeslacht.getSelectionModel().clearSelection();
        tfAddGebDatum.clear();
        tfAddGebPlaats.clear();
        cbAddOuderlijkGezin.getSelectionModel().clearSelection();
    }

    private void clearTabGezinInvoer() {
        cbOuder1Invoer.getSelectionModel().clearSelection();
        cbOuder2Invoer.getSelectionModel().clearSelection();
        tfHuwelijkInvoer.clear();
        tfScheidingInvoer.clear();

    }

    private void clearTabPersoon() {
        cbPersonen.getSelectionModel().clearSelection();
        tfPersoonNr.clear();
        tfVoornamen.clear();
        tfTussenvoegsel.clear();
        tfAchternaam.clear();
        tfGeslacht.clear();
        tfGebDatum.clear();
        tfGebPlaats.clear();
        cbOuderlijkGezin.getSelectionModel().clearSelection();
        lvAlsOuderBetrokkenBij.setItems(FXCollections.emptyObservableList());
    }

    private void clearTabGezin() {
        cbKiesGezin.getSelectionModel().clearSelection();
        tfGezinNr.clear();
        tfGezinOuder1.clear();
        tfGezinOuder2.clear();
        tfGezinHuwelijk.clear();
        tfGezinScheiding.clear();
        lvGezinKinderen.getItems().clear();
    }

    private void showDialog(String type, String message) {
        Stage myDialog = new Dialog(getStage(), type, message);
        myDialog.show();
    }

    private Stage getStage() {
        return (Stage) menuBar.getScene().getWindow();
    }

}
