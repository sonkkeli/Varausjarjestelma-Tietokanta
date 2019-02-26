# Varausjarjestelma-Tietokanta

NOTE!!!! This database should modify to be working with DAO, rather than just SQL-queries, but it's still on going...

Tietokantojen perusteet, ohjelmointiprojekti

Tietokantojen perusteiden toinen laajempi harjoitustyö. Tehtävästä palautetaan sekä sovellus että PDF-muotoinen raportti. Sovellus palautetaan TMC:hen, raportti Moodleen. Raportti vertaisarvioidaan ja sovellusta tarkastelee kurssihenkilökunta sekä TMC.

Ohjelmointiprojektissa harjoitellaan tietokannan suunnittelua sekä konkreettisen tietokantaa käyttävän sovelluksen luomista. Tehtävä kannattaa jakaa osiin siten, että suunnittelet ensin tietokannan rakenteen ja tietokannassa suoritettavat kyselyt, ja keskityt vasta tämän jälkeen konkreettiseen toteutukseen. Tee sovellusta toteuttaessasi sovellukselta odotettava toiminnallisuus yksi osa kerrallaan. Testaa sovellustasi (manuaalisesti) kunkin osan toteutuksen jälkeen. Yksikkötestien kirjoittamista sovellukselta ei odoteta. Huom! Sovelluksen tulosteiden ja syötteiden lukemisen tulee toimia käyttöliittymäesimerkkien näyttämällä tavalla.

Tehtävänanto: Toteuta hotellihuoneiden varausjärjestelmä. Järjestelmä tarjoaa mahdollisuuden hotellihuoneiden lisäämiseen, huoneiden listaamiseen, huoneiden hakemiseen, varauksen lisäämiseen, varausten listaamiseen, sekä erilaisten tilastojen tulostamiseen. Alla toiminnallisuus on kuvattuna tarkemmin.

Hotellihuoneista tallennetaan vapaasti syötettävä huoneen tyyppi (esim. Superior, Excelsior, Commodore), huoneen numero (kerrosta ja huonetta kuvaava numero, esim 604 tai 612) ja huoneen päivähinta (esim 119). 

Huoneiden listaus listaa hotellissa olevien huoneiden tiedot. Listauksessa tulee näkyä huoneiden tiedot, kunkin huoneen tiedot omalla rivillään.

Huoneiden hakeminen tapahtuu syöttämällä alkupäivämäärä (muodossa yyyy-MM-dd, esim 2019-02-28), loppupäivämäärä (myös muodossa yyyy-MM-dd, esim 2019-03-01), huoneen tyyppi, sekä korkein hyväksyttävä päivähinta. Haku listaa ne huoneet, jotka ovat vapaana annetulla aikavälillä ja jotka sopivat muihin annettuihin kriteereihin (huoneen tyyppi, korkein hyväksyttävä päivähinta). Huoneen tyypin ja päivähinnan voi myös jättää tyhjäksi, jolloin hakua ei rajata niiden perusteella. 

Varauksen tekeminen tapahtuu syöttämällä alkupäivämäärä (muodossa yyyy-MM-dd, esim 2019-02-28), loppupäivämäärä (myös muodossa yyyy-MM-dd, esim 2019-03-01), huoneen tyyppi, sekä korkein hyväksyttävä päivähinta (tässäkin huoneen tyypin ja korkeimman hyväksyttävän päivähinnan saa jättää tyhjäksi). Tämän jälkeen järjestelmä listaa vapaiden huoneiden lukumäärän. Mikäli huoneita ei ole vapaana, varauksen tekeminen keskeytetään. Muulloin käyttäjältä kysytään varattavien huoneiden määrä (luvun tulee olla hyväksyttävä luku -- eli mikäli huoneita on vapaana 5, luvun tulee olla välillä [1,5]), riveittäin syötettäviä lisävarusteita, sekä varaajan tietoja (nimi, puhelinnumero, sähköpostiosoite). Kun tiedot on syötetty, järjestelmä varaa käyttäjälle kalleimmat kriteerit täyttävät huoneet.

Varausten listaaminen tulostaa kaikki varaukset varausten alkupäivämäärän perusteella järjestettynä aikaisin ensin. Varausten yhteydessä tulostetaan ensimmäiselle riville varaajan nimi ja sähköpostiosoite, varauksen alkupäivämäärä, varauksen loppupäivämäärä, varauksen kesto päivinä, lisävarusteiden lukumäärä ja varattujen huoneiden lukumäärä. Tätä seuraa varatut huoneet kukin huone omalla rivillään. Viimeisellä varaukseen liittyvällä rivillä tulostetaan varauksen yhteishinta. 

Tilastoja kysyttäessä järjestelmä kysyy käyttäjältä mitä tilastoja käyttäjä haluaa. Mahdollisuudet ovat seuraavat:

Suosituimmat lisävarusteet. Lisävarusteet listataan varauslukumäärän mukaan järjestettynä siten, että eniten varatut (eli useimmissa varauksissa esiintyvät) lisävarusteet listataan ensin. Listauksessa näkyy myös kunkin lisävarusteen kohdalla lisävarusteen varausten lukumäärä. Listauksessa näytetään korkeintaan 10 lisävarustetta. Täydennys 16.2: lisävarusteiden suosiota laskettaessa ei tarvitse ottaa huomioon varauksen pituutta tai huoneiden lukumäärää.

Parhaat asiakkaat. Asiakkaat listataan heidän varaustensa yhteissumman perusteella järjestettynä, eniten rahaa käyttänyt ensin. Listauksessa näytetään korkeintaan 10 asiakasta.

Varausprosentti huoneittain.  Käyttäjältä kysytään tarkastelun alku- ja loppukuukausi (muodossa yyyy-MM), jonka jälkeen huoneista lasketaan niiden varausprosentti. Varausprosentti lasketaan muodossa 100 * varatut päivät / kaikki päivät. Mikäli käyttäjä syöttää tarkastelun aluksi tammikuun ja lopuksi helmikuun ohjelma tulostaa tammikuun varausprosentin. Jos huone on ollut tammikuussa kaksitoista päivää varattuna, on sen varausprosentti tammikuulle noin 39 (100*12/31).

Varausprosentti huonetyypeittäin. Käyttäjältä kysytään tarkastelun alku- ja loppukuukautta (muodossa yyyy-MM), jonka jälkeen huoneista lasketaan niiden varausprosentti. Varausprosentti lasketaan muodossa 100 * varatut päivät yhteensä / kaikki päivät yhteensä. Esimerkiksi jos käyttäjä syöttää tarkastelun alkukuukaudeksi tammikuun ja loppukuukaudeksi maaliskuun (2019), ja hotellissa on kaksi Excelsior-tyypin huonetta joista toinen on tammikuussa kaksitoista päivää varattuna ja toinen helmikuussa neljä päivää varattuna, on Excelsior-huoneiden varausprosentti 100 * (12+4) / (2*31 + 2*28) eli noin 14.

Kaikkien huoneiden varaukset alkavat klo 16:00 ja päättyvät klo 10:00.
