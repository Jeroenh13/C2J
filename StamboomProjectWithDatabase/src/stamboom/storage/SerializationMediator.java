/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package stamboom.storage;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import stamboom.domain.Administratie;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SerializationMediator implements IStorageMediator {

    /**
     * bevat de bestandslocatie. Properties is een subclasse van HashTable, een
     * alternatief voor een List. Het verschil is dat een List een volgorde
     * heeft, en een HashTable een key/value index die wordt opgevraagd niet op
     * basis van positie, maar op key.
     */
    private Properties props;

    /**
     * creation of a non configured serialization mediator
     */
    public SerializationMediator() {
        props = null;
    }

    @Override
    public Administratie load() throws IOException {
        Administratie a = null;
        if (!isCorrectlyConfigured()) {
            throw new RuntimeException("Serialization mediator isn't initialized correctly.");
        }
        FileInputStream fis = new FileInputStream((File) props.get("file"));
        ObjectInputStream ois = new ObjectInputStream(fis);
        try {
            a = (Administratie) ois.readObject();
            a.InitObservables();
            ois.close();
            fis.close();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(SerializationMediator.class.getName()).log(Level.SEVERE, null, ex);

            // todo opgave 2
        }
        return a;
    }

    @Override
    public void save(Administratie admin) throws IOException {
        if (!isCorrectlyConfigured()) {
            throw new RuntimeException("Serialization mediator isn't initialized correctly.");
        }
            try {
                FileOutputStream fos = new FileOutputStream((File) props.get("file"));
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(admin);
                oos.close();
                fos.close();
            
        } catch (IOException e) {
        }
        // todo opgave 2
    }

    /**
     * Laadt de instellingen, in de vorm van een Properties bestand, en
     * controleert of deze in de juiste vorm is.
     *
     * @param props
     * @return
     */
    @Override
    public boolean configure(Properties props) {
        this.props = props;
        return isCorrectlyConfigured();
    }

    @Override
    public Properties config() {
        return props;
    }

    /**
     * Controleert of er een geldig Key/Value paar bestaat in de Properties. De
     * bedoeling is dat er een Key "file" is, en de Value van die Key een String
     * representatie van een FilePath is (eg. C:\\Users\Username\test.txt).
     *
     * @return true if config() contains at least a key "file" and the
     * corresponding value is formatted like a file path
     */
    @Override
    public boolean isCorrectlyConfigured() {
        if (props == null) {
            return false;
        }
        return props.containsKey("file")
                && props.get("file") instanceof File;
    }
}
