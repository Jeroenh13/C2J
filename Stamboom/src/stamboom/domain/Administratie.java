package stamboom.domain;

import java.util.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Administratie {

    //************************datavelden*************************************
    private int nextGezinsNr;
    private int nextPersNr;
    private final List<Persoon> personen;
    private final List<Gezin> gezinnen;
    private ObservableList<Persoon> observablePersonen;
    private ObservableList<Gezin> observableGezinnen;

    //***********************constructoren***********************************
    /**
     * er wordt een lege administratie aangemaakt.
     * personen en gezinnen die in de toekomst zullen worden gecreeerd, worden
     * (apart) opvolgend genummerd vanaf 1
     */
    public Administratie() {
        //todo opgave 1
        this.personen = new ArrayList<>();
        this.gezinnen = new ArrayList<>();
        nextGezinsNr = 1;
        nextPersNr = 1;
        observablePersonen = FXCollections.observableList(personen);
        observableGezinnen = FXCollections.observableList(gezinnen);
    }

    //**********************methoden****************************************
    /**
     * er wordt een persoon met de gegeven parameters aangemaakt; de persoon
     * krijgt een uniek nummer toegewezen, en de persoon is voortaan ook bij het
     * (eventuele) ouderlijk gezin bekend. Voor de voornamen, achternaam en
     * gebplaats geldt dat de eerste letter naar een hoofdletter en de resterende
     * letters naar kleine letters zijn geconverteerd; het tussenvoegsel is in
     * zijn geheel geconverteerd naar kleine letters; overbodige spaties zijn
     * verwijderd
     *
     * @param geslacht
     * @param vnamen vnamen.length>0; alle strings zijn niet leeg
     * @param anaam niet leeg
     * @param tvoegsel mag leeg zijn
     * @param gebdat
     * @param gebplaats niet leeg
     * @param ouderlijkGezin mag de waarde null (=onbekend) hebben
     *
     * @return de nieuwe persoon.
     * Als de persoon al bekend was (op basis van combinatie van getNaam(),
     * geboorteplaats en geboortedatum), wordt er null geretourneerd.
     */
    public Persoon addPersoon(Geslacht geslacht, String[] vnamen, String anaam,
            String tvoegsel, Calendar gebdat,
            String gebplaats, Gezin ouderlijkGezin) {
        
        if (vnamen.length == 0) {
            throw new IllegalArgumentException("ten minste 1 voornaam");
        }
        for (String voornaam : vnamen) {
            if (voornaam.trim().isEmpty()) {
                throw new IllegalArgumentException("lege voornaam is niet toegestaan");
            }
        }

        if (anaam.trim().isEmpty()) {
            throw new IllegalArgumentException("lege achternaam is niet toegestaan");
        }

        if (gebplaats.trim().isEmpty()) {
            throw new IllegalArgumentException("lege geboorteplaats is niet toegestaan");
        }
        
        
            
        for(Persoon p : personen)
        {
            Persoon tmpPersoon = getPersoon(vnamen,anaam,tvoegsel,gebdat,gebplaats);
            if(p == tmpPersoon)
            {
                return null;
            }
        }
        
        
        Persoon persoon = new Persoon(nextPersNr, vnamen, anaam, tvoegsel, gebdat, gebplaats, geslacht,  ouderlijkGezin);
        nextPersNr++;
        if(ouderlijkGezin != null)
        {
            ouderlijkGezin.breidUitMet(persoon);
        }
        personen.add(persoon);
        return persoon;
    }

    /**
     * er wordt, zo mogelijk (zie return) een (kinderloos) ongehuwd gezin met
     * ouder1 en ouder2 als ouders gecreeerd; de huwelijks- en scheidingsdatum
     * zijn onbekend (null); het gezin krijgt een uniek nummer toegewezen; dit
     * gezin wordt ook bij de afzonderlijke ouders geregistreerd;
     *
     * @param ouder1
     * @param ouder2 mag null zijn
     *
     * @return het nieuwe gezin. null als ouder1 = ouder2 of als een van de volgende
     * voorwaarden wordt overtreden:
     * 1) een van de ouders is op dit moment getrouwd
     * 2) het koppel vormt al een ander gezin
     */
    public Gezin addOngehuwdGezin(Persoon ouder1, Persoon ouder2) {
        if (ouder1 == ouder2) {
            return null;
        }

        if (ouder1.getGebDat().compareTo(Calendar.getInstance()) > 0) {
            return null;
        }
        if (ouder2 != null && ouder2.getGebDat().compareTo(Calendar.getInstance()) > 0) {
            return null;
        }

        Calendar nu = Calendar.getInstance();
        if(ouder2 != null)
        {
            if(ouder1.isGetrouwdOp(nu) || ouder2.isGetrouwdOp(nu))
            {
                return null;
            }
            if(ongehuwdGezinBestaat(ouder1,ouder2))
            {
                return null;
            }
        }
        
        if(ouder1.isGetrouwdOp(nu))
        {
            return null;
        }

        Gezin gezin = new Gezin(nextGezinsNr, ouder1, ouder2);
        nextGezinsNr++;
        gezinnen.add(gezin);

        ouder1.wordtOuderIn(gezin);
        if (ouder2 != null) {
            ouder2.wordtOuderIn(gezin);
        }

        return gezin;
    }

    /**
     * Als het ouderlijk gezin van persoon nog onbekend is dan wordt
     * persoon een kind van ouderlijkGezin, en tevens wordt persoon als kind
     * in dat gezin geregistreerd. Als de ouders bij aanroep al bekend zijn,
     * verandert er niets
     *
     * @param persoon
     * @param ouderlijkGezin
     * @return of ouderlijk gezin kon worden toegevoegd.
     */
    public boolean setOuders(Persoon persoon, Gezin ouderlijkGezin) {
        return persoon.setOuders(ouderlijkGezin);
    }

    /**
     * als de ouders van dit gezin gehuwd zijn en nog niet gescheiden en datum
     * na de huwelijksdatum ligt, wordt dit de scheidingsdatum. Anders gebeurt
     * er niets.
     *
     * @param gezin
     * @param datum
     * @return true als scheiding geaccepteerd, anders false
     */
    public boolean setScheiding(Gezin gezin, Calendar datum) {
        return gezin.setScheiding(datum);
    }

    /**
     * registreert het huwelijk, mits gezin nog geen huwelijk is en beide
     * ouders op deze datum mogen trouwen (pas op: het is niet toegestaan dat een
     * ouder met een toekomstige (andere) trouwdatum trouwt.)
     *
     * @param gezin
     * @param datum de huwelijksdatum
     * @return false als huwelijk niet mocht worden voltrokken, anders true
     */
    public boolean setHuwelijk(Gezin gezin, Calendar datum) {
        return gezin.setHuwelijk(datum);
    }

    /**
     *
     * @param ouder1
     * @param ouder2
     * @return true als dit koppel (ouder1,ouder2) al een ongehuwd gezin vormt
     */
    boolean ongehuwdGezinBestaat(Persoon ouder1, Persoon ouder2) {
        return ouder1.heeftOngehuwdGezinMet(ouder2) != null;
    }

    /**
     * als er al een ongehuwd gezin voor dit koppel bestaat, wordt het huwelijk
     * voltrokken, anders wordt er zo mogelijk (zie return) een (kinderloos)
     * gehuwd gezin met ouder1 en ouder2 als ouders gecreeerd; de
     * scheidingsdatum is onbekend (null); het gezin krijgt een uniek nummer
     * toegewezen; dit gezin wordt ook bij de afzonderlijke ouders
     * geregistreerd;
     *
     * @param ouder1
     * @param ouder2
     * @param huwdatum
     * @return null als ouder1 = ouder2 of als een van de ouders getrouwd is
     * anders het gehuwde gezin
     */
    public Gezin addHuwelijk(Persoon ouder1, Persoon ouder2, Calendar huwdatum) {
        //todo opgave 1
        boolean bestaat = false;
        Gezin hulpgezin = null;
        if(ouder1 == ouder2)
        {
            return null;   
        }
        if(gezinnen.isEmpty())
        {
            hulpgezin = new Gezin(nextGezinsNr, ouder1, ouder2);
            gezinnen.add(hulpgezin);                
            hulpgezin.setHuwelijk(huwdatum);
            ouder1.wordtOuderIn(hulpgezin);
            ouder2.wordtOuderIn(hulpgezin);
                
            nextGezinsNr++;
        }
        else
        {
            for(Gezin g : gezinnen)
            {
                if((g.getOuder1() == ouder1 || g.getOuder1() == ouder2) || (g.getOuder2() == ouder1 || g.getOuder2() == ouder2))
                {
                    if(!g.isOngehuwd())
                    {
                        if(g.getScheidingsdatum() != null)
                        {
                            if(huwdatum.after(g.getScheidingsdatum()))
                            {
                                hulpgezin = new Gezin(nextGezinsNr, ouder1, ouder2);
                                gezinnen.add(hulpgezin);                
                                hulpgezin.setHuwelijk(huwdatum);
                                ouder1.wordtOuderIn(hulpgezin);
                                ouder2.wordtOuderIn(hulpgezin);

                                nextGezinsNr++; 
                                return hulpgezin;
                            }
                            hulpgezin = null;
                            bestaat = true;
                        }
                        else{
                            hulpgezin = null;
                            bestaat = true;
                        }
                    }
                    if((g.getOuder1() == ouder1 || g.getOuder1() == ouder2) && (g.getOuder2() == ouder1 || g.getOuder2() == ouder2) && (g.isOngehuwd()))
                    {
                        g.setHuwelijk(huwdatum);
                        hulpgezin = g; 
                    }
                }
            }          
        }
        if(hulpgezin==null && bestaat == false)
        {
            
            hulpgezin = new Gezin(nextGezinsNr, ouder1, ouder2);
            gezinnen.add(hulpgezin);  
            if(hulpgezin.setHuwelijk(huwdatum))
            {
                hulpgezin.setHuwelijk(huwdatum);
                ouder1.wordtOuderIn(hulpgezin);
                ouder2.wordtOuderIn(hulpgezin);
                nextGezinsNr++;
            }
            else{
                hulpgezin = null;
            }
        }
        return hulpgezin;
       
    }

    /**
     *
     * @return het aantal geregistreerde personen
     */
    public int aantalGeregistreerdePersonen() {
        return nextPersNr - 1;
    }

    /**
     *
     * @return het aantal geregistreerde gezinnen
     */
    public int aantalGeregistreerdeGezinnen() {
        return nextGezinsNr - 1;
    }

    /**
     *
     * @param nr
     * @return de persoon met nummer nr, als die niet bekend is wordt er null
     * geretourneerd
     */
    public Persoon getPersoon(int nr) {
        //todo opgave 1
        //aanname: er worden geen personen verwijderd        
        for(Persoon p : personen)
        {
            if(p.getNr() == nr)
            {
                return p;
            }
        }
        return null;
    }

    /**
     * @param achternaam
     * @return alle personen met een achternaam gelijk aan de meegegeven
     * achternaam (ongeacht hoofd- en kleine letters)
     */
    public ArrayList<Persoon> getPersonenMetAchternaam(String achternaam) {
        String geformatteerdeNaam = achternaam.toUpperCase();
        ArrayList<Persoon> gevondenPersonen = new ArrayList<Persoon>();
        
        for(Persoon p : personen)
        {
            if(geformatteerdeNaam.equals(p.getAchternaam().toUpperCase()))
            {
                gevondenPersonen.add(p);
            }
        }
        
        return gevondenPersonen;
    }

    /**
     *
     * @return de geregistreerde personen
     */
    public ObservableList<Persoon> getPersonen() {
        return (ObservableList<Persoon>) FXCollections.unmodifiableObservableList(observablePersonen);
    }

    /**
     *
     * @param vnamen
     * @param anaam
     * @param tvoegsel
     * @param gebdat
     * @param gebplaats
     * @return de persoon met dezelfde initialen, tussenvoegsel, achternaam,
     * geboortedatum en -plaats mits bekend (ongeacht hoofd- en kleine letters),
     * anders null
     */
    public Persoon getPersoon(String[] vnamen, String anaam, String tvoegsel,
            Calendar gebdat, String gebplaats) {
        //todo opgave 1
        StringBuilder naam = new StringBuilder();
        for(int i = 0; i < vnamen.length;i++)
        {
            naam.append(vnamen[i].substring(0,1).toUpperCase()).append('.');
        }
        if(tvoegsel == "")
        {
            naam.append(tvoegsel.toUpperCase()).append(' ').append(anaam.toUpperCase()).append(gebplaats.toUpperCase()).append(gebdat);
        }
        else
        {
             naam.append(' ').append(tvoegsel.toUpperCase()).append(' ').append(anaam.toUpperCase()).append(gebplaats.toUpperCase()).append(gebdat);
        }
       
        
        for(Persoon p :personen)
        {
            StringBuilder pnaam = new StringBuilder();
            pnaam.append(p.getNaam().toUpperCase()).append(p.getGebPlaats().toUpperCase()).append(p.getGebDat());
            if(pnaam.toString().equals(naam.toString()))          
            {
                return p;
            }
        }
        return null;
    }

    /**
     *
     * @return de geregistreerde gezinnen
     */
    public ObservableList<Gezin> getGezinnen() {
        return (ObservableList<Gezin>) FXCollections.unmodifiableObservableList(observableGezinnen);
    }

    /**
     *
     * @param gezinsNr
     * @return het gezin met nummer nr. Als dat niet bekend is wordt er null
     * geretourneerd
     */
    public Gezin getGezin(int gezinsNr) {
        // aanname: er worden geen gezinnen verwijderd
        if (gezinnen != null && 1 <= gezinsNr && 1 <= gezinnen.size()) {
            return gezinnen.get(gezinsNr - 1);
        }
        return null;
    }
}
