package stamboom.domain;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import stamboom.util.StringUtilities;

public class Persoon {

    // ********datavelden**************************************
    private final int nr;
    private final String[] voornamen;
    private final String achternaam;
    private final String tussenvoegsel;
    private final Calendar gebDat;
    private final String gebPlaats;
    private Gezin ouderlijkGezin;
    private final List<Gezin> alsOuderBetrokkenIn;
    private final Geslacht geslacht;
    
    // ********constructoren***********************************
    /**
     * er wordt een persoon gecreeerd met persoonsnummer persNr en met als
     * voornamen vnamen, achternaam anaam, tussenvoegsel tvoegsel, geboortedatum
     * gebdat, gebplaats, geslacht g en een gegeven ouderlijk gezin (mag null
     * (=onbekend) zijn); NB. de eerste letter van een voornaam, achternaam en
     * gebplaats wordt naar een hoofdletter omgezet, alle andere letters zijn
     * kleine letters; het tussenvoegsel is zo nodig in zijn geheel
     * geconverteerd naar kleine letters.
     *
     */
    Persoon(int persNr, String[] vnamen, String anaam, String tvoegsel,
            Calendar gebdat, String gebplaats, Geslacht g, Gezin ouderlijkGezin) {
        nr = persNr;
        for(int i = 0; i < vnamen.length; i++)
        {
            vnamen[i] = StringUtilities.withFirstCapital(vnamen[i]).trim();
        }
        voornamen = vnamen;
        achternaam = StringUtilities.withFirstCapital(anaam).trim();
        tussenvoegsel = tvoegsel.toLowerCase().trim();
        gebDat = gebdat;
        gebPlaats = StringUtilities.withFirstCapital(gebplaats).trim();
        geslacht = g;
        this.ouderlijkGezin = ouderlijkGezin; 
        alsOuderBetrokkenIn = new ArrayList<>();
    }

    // ********methoden****************************************
    /**
     * @return de achternaam van deze persoon
     */
    public String getAchternaam() {
        return achternaam;
    }

    /**
     * @return de geboortedatum van deze persoon
     */
    public Calendar getGebDat() {
        return gebDat;
    }

    /**
     *
     * @return de geboorteplaats van deze persoon
     */
    public String getGebPlaats() {
        return gebPlaats;
    }

    /**
     *
     * @return het geslacht van deze persoon
     */
    public Geslacht getGeslacht() {
        return geslacht;
    }

    /**
     *
     * @return de voorletters van de voornamen; elke voorletter wordt gevolgd
     * door een punt
     */
    public String getInitialen() {
        StringBuilder initialen = new StringBuilder();
        for(int i = 0; i < voornamen.length;i++)
        {
            initialen.append(voornamen[i].substring(0,1)).append('.');
        }
        return initialen.toString();
    }

    /**
     *
     * @return de initialen gevolgd door een eventueel tussenvoegsel en
     * afgesloten door de achternaam; initialen, voorzetsel en achternaam zijn
     * gescheiden door een spatie
     */
    public String getNaam() {
        StringBuilder naam = new StringBuilder();
        naam.append(getInitialen());
        if(tussenvoegsel.equals(""))
        {
            naam.append(tussenvoegsel).append(' ').append(achternaam);
        }
        else
        {
            naam.append(' ').append(tussenvoegsel).append(' ').append(achternaam);
        }
        return naam.toString();
    }

    /**
     * @return het nummer van deze persoon
     */
    public int getNr() {
        return nr;
    }

    /**
     * @return het ouderlijk gezin van deze persoon, indien bekend, anders null
     */
    public Gezin getOuderlijkGezin() {
        return ouderlijkGezin;
    }

    /**
     * @return het tussenvoegsel van de naam van deze persoon (kan een lege
     * string zijn)
     */
    public String getTussenvoegsel() {
        return tussenvoegsel;
    }

    /**
     * @return alle voornamen onderling gescheiden door een spatie
     */
    public String getVoornamen() {
        StringBuilder init = new StringBuilder();
        for (String s : voornamen) {
            init.append(StringUtilities.withFirstCapital(s)).append(' ');
        }
        return init.toString().trim();
    }

    /**
     * @return de standaardgegevens van deze mens: naam (geslacht) geboortedatum
     */
    public String standaardgegevens() {
        return getNaam() + " (" + getGeslacht() + ") " + StringUtilities.datumString(gebDat);
    }

    @Override
    public String toString() {
        return standaardgegevens();
    }

    /**
     * @return de gezinnen waar deze persoon bij betrokken is
     */
    public List<Gezin> getAlsOuderBetrokkenIn() {
        return (List<Gezin>) Collections.unmodifiableList(alsOuderBetrokkenIn);
    }

    /**
     * Als het ouderlijk gezin van deze persoon nog onbekend is dan wordt deze
     * persoon een kind van ouderlijkGezin en tevens wordt deze persoon als kind
     * in dat gezin geregistreerd Als de ouders bij aanroep al bekend zijn,
     * verandert er niets
     *
     * @param ouderlijkGezin
     * @return of ouderlijk gezin kon worden toegevoegd
     */
    boolean setOuders(Gezin ouderlijkGezin) {
        if(this.ouderlijkGezin == null)
        {
            this.ouderlijkGezin = ouderlijkGezin;
            this.ouderlijkGezin.breidUitMet(this);
            return true;
        }else
        {
            return false;
        }
    }

    /**
     * @return voornamen, eventueel tussenvoegsel en achternaam, geslacht,
     * geboortedatum, namen van de ouders, mits bekend, en nummers van de
     * gezinnen waarbij deze persoon betrokken is (geweest)
     */
    public String beschrijving() {
        StringBuilder sb = new StringBuilder();

        sb.append(standaardgegevens());

        if (ouderlijkGezin != null) {
            sb.append("; 1e ouder: ").append(ouderlijkGezin.getOuder1().getNaam());
            if (ouderlijkGezin.getOuder2() != null) {
                sb.append("; 2e ouder: ").append(ouderlijkGezin.getOuder2().getNaam());
            }
        }
        if (!alsOuderBetrokkenIn.isEmpty()) {
            sb.append("; is ouder in gezin ");

            for (Gezin g : alsOuderBetrokkenIn) {
                sb.append(g.getNr()).append(" ");
            }
        }

        return sb.toString();
    }

    /**
     * als g nog niet bij deze persoon staat geregistreerd wordt g bij deze
     * persoon geregistreerd en anders verandert er niets
     *
     * @param g een nieuw gezin waarin deze persoon een ouder is
     *
     */
    void wordtOuderIn(Gezin g) {
        if (!alsOuderBetrokkenIn.contains(g)) {
            alsOuderBetrokkenIn.add(g);
        }
    }

    /**
     *
     *
     * @param andereOuder mag null zijn
     * @return het ongehuwde gezin met de andere ouder ; mits bestaand anders
     * null
     */
    public Gezin heeftOngehuwdGezinMet(Persoon andereOuder) {
        Gezin gezin = null;
        for(Gezin g : alsOuderBetrokkenIn)
        {
            if(g.getOuder2() == andereOuder || g.getOuder1() == andereOuder)
            {
                if(g.isOngehuwd())
                {
                    gezin = g;
                }
            }
            else
            {
                gezin = null;
            }
        }
        return gezin;
    }

    /**
     *
     * @param datum
     * @return true als persoon op datum getrouwd is, anders false
     */
    public boolean isGetrouwdOp(Calendar datum) {
        if(alsOuderBetrokkenIn.isEmpty())
        {
            return false;
        }
        for (Gezin gezin : alsOuderBetrokkenIn) {
            if (gezin.heeftGetrouwdeOudersOp(datum)) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @param datum
     * @return true als de persoon kan trouwen op datum, hierbij wordt rekening
     * gehouden met huwelijken in het verleden en in de toekomst
     * Alleen meerderjarige (18+) personen kunnen trouwen.
     */
    public boolean kanTrouwenOp(Calendar datum) {
        Calendar meerderjarigDatum = ((GregorianCalendar)this.gebDat.clone());
        meerderjarigDatum.add(Calendar.YEAR, 18);
        if(datum.compareTo(meerderjarigDatum) < 1){
            return false;
        }

        for (Gezin gezin : alsOuderBetrokkenIn) {
            if (gezin.heeftGetrouwdeOudersOp(datum)) {
                return false;
            } else {
                Calendar huwdatum = gezin.getHuwelijksdatum();
                if (huwdatum != null && huwdatum.after(datum)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     *
     * @param datum
     * @return true als persoon op datum gescheiden is, anders false
     */
    public boolean isGescheidenOp(Calendar datum) {
        for(Gezin g : alsOuderBetrokkenIn)
        {
            if(g.heeftGescheidenOudersOp(datum))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * ********* de rest wordt in opgave 2 verwerkt ****************
     */
    /**
     *
     * @return het aantal personen in de stamboom van deze persoon (ouders,
     * grootouders etc); de persoon zelf telt ook mee
     */
    public int afmetingStamboom() {
        //todo opgave 2
        int stamboomgrootte = 1;
        int ouder1 = 0;
        int ouder2 = 0;
        if(oudersBekend(this))
        {
            if(this.ouderlijkGezin.getOuder2()!= null)
            {
               ouder2 = this.ouderlijkGezin.getOuder2().afmetingStamboom();
            }
            ouder1 = this.ouderlijkGezin.getOuder1().afmetingStamboom();
        }
        stamboomgrootte = stamboomgrootte + ouder1 + ouder2;
        
        return stamboomgrootte;
    }
    
    boolean oudersBekend(Persoon p)
    {
        if(p.getOuderlijkGezin()!= null)
        {
            return true;
        }
        return false;
    }

    /**
     * de lijst met de items uit de stamboom van deze persoon wordt toegevoegd
     * aan lijst, dat wil zeggen dat begint met de toevoeging van de
     * standaardgegevens van deze persoon behorende bij generatie g gevolgd door
     * de items uit de lijst met de stamboom van de eerste ouder (mits bekend)
     * en gevolgd door de items uit de lijst met de stamboom van de tweede ouder
     * (mits bekend) (het generatienummer van de ouders is steeds 1 hoger)
     *
     * @param lijst
     * @param g >=0, het nummer van de generatie waaraan deze persoon is
     * toegewezen;
     */
    void voegJouwStamboomToe(ArrayList<PersoonMetGeneratie> lijst, int g) {
        //todo opgave 2
    }

    /**
     *
     * @return de stamboomgegevens van deze persoon in de vorm van een String:
     * op de eerste regel de standaardgegevens van deze persoon, gevolgd door de
     * stamboomgegevens van de eerste ouder (mits bekend) en gevolgd door de
     * stamboomgegevens van de tweede ouder (mits bekend); formattering: iedere
     * persoon staat op een aparte regel en afhankelijk van het
     * generatieverschil worden er per persoon 2*generatieverschil spaties
     * ingesprongen;
     *
     * bijv voor M.G. Pieterse met ouders, grootouders en overgrootouders,
     * inspringen is in dit geval met underscore gemarkeerd: <br>
     *
     * M.G. Pieterse (VROUW) 5-5-2004<br>
     * __L. van Maestricht (MAN) 27-6-1953<br>
     * ____A.G. von Bisterfeld (VROUW) 13-4-1911<br>
     * ______I.T.M.A. Goldsmid (VROUW) 22-12-1876<br>
     * ______F.A.I. von Bisterfeld (MAN) 27-6-1874<br>
     * ____H.C. van Maestricht (MAN) 17-2-1909<br>
     * __J.A. Pieterse (MAN) 23-6-1964<br>
     * ____M.A.C. Hagel (VROUW) 12-0-1943<br>
     * ____J.A. Pieterse (MAN) 4-8-1923<br>
     */    
    public String stamboomAlsString() {
        StringBuilder builder = new StringBuilder();
        builder = builder.append(this.getNaam()).append(' ').append(this.getGeslacht());
        if(oudersBekend(this))
        {
            builder.append("\r\n").append("  ");
            builder.append(this.ouderlijkGezin.getOuder1().stamboomAlsString());
            if(this.ouderlijkGezin.getOuder2()!= null)
            {
               builder.append("\r\n").append("  ");
               builder.append(this.ouderlijkGezin.getOuder2().stamboomAlsString());
            }
        }
        //todo opgave 2

        return builder.toString();
    }
}
