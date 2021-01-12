# SnakeFX & SnakeServer
Dies ist das Repository des Moduls Patterns und Frameworks im WiSe 2020.
Dieses Repository beinhaltet die Planungsdokumente, UML-Diagramme und den Quelltext des Projekts.

Das gesamte Projekt ist in der Programmiersprache Java entwickelt worden.
Alle Module des Projekts werden mit Java 8 - mittels Oracle's Java Development Kit kompiliert, getestet und ausgeführt.

Als Build-Management-System des Projekts kommt Apache Maven 3 zum Einsatz. Jedes Modul enthält eine pom.xml welche das jeweilige Project-Object-Model beschreibt.

## Modul-Struktur
**SnakeCore** \
Enthält die grundlegenden und gemeinsamen Aspekte des Projekts, wie beispielsweise Modelle welche in allen weiteren Projekten verwendet werden.

**SnakeFX** \
Das Frontend-Modul des Projekts - enthält das User-Interface der Anwendung.

**SnakeServer** \
Das Backend-Modul des Projekts - enthält den Applikations-Server der Anwendung.

**SnakeTest** \
Das Test-Projekt des Projekts - enthält Abhängigkeiten zu allen Sub-Modulen des Projekts
Enthält Test-Fälle für verschiedene Szenarien, z.B. um den Anwendungsserver und eine Menge von Clients zu initialisieren, miteinander zu verbinden, etc. - dient der Simulation von Echt-Welt-Interaktionen.

## Datei-Struktur
- docs - Enthält die Dokumentation zur Einrichtung und Nutzung des Projekts
- docs/Prototype - Enthält prototypische Dokumentationen zu ausgewählten Aspekten
- docs/UML - Enthält die UML-Diagramme zu verschiedenen Aspekten des Projekts
- workspace 
  - Enthält den Quelltext des Clients- als auch des Java-Backends
  - Enthält eine gemeinsame Projektbeschreibung (siehe /workspace/readme.md)

## API / Kommunikation
Die folgenden URLs werden vom Backend bereitgestellt und können von den Clients konsumiert und verarbeitet werden.

### WebSocket / STOMP
Die folgenden Befehle werden für den Datenaustausch verwendet:
- **connect** - Stellt eine Verbindung mittels StompClient auf einen HTTP-Handshake Endpoint her. Die dabei aufgebaute Session wird für den im folgenden beschriebenen Informationsaustausch verwendet.
- **subscribe** - Führt eine Subscription (dt. etwa "Anmeldung") auf ein bestimmtes Topic (dt. etwa "Thema") aus - der StompClient bspw. subscribed auf den "/topic/players"-Endpoint - im Anschluss werden alle Änderungen des Topics via Publish-Mechanismus an den Client übertragen und dort mittels Callbacks genutzt um das User-Interface zu aktualisieren. 
- **unsubscribe** - Führt eine Unsubscription (dt. etwa "Abmeldung") auf ein bestimmtes Topic aus.
- **send** - Sendet eine Nachricht in einem bestimmten Format an das Backend / den Server - die Nachrichten-Inhalte werden mittels Jackson im JSON-Format serialisiert und erzeugt, und auf der jeweiligen Endseite deserialisiert (Un/marshalling).

#### STOMP-Endpoints
Die folgenden URLs dienen als Adresse / Endpoints für die Kommunikation mittels WebSockets
- ws://localhost:13373/snakeserver - HTTP-Handshake und WebSocket / STOMP Upgrade
- ws://localhost:13373/app/games/{gameId} - Übertragung von Spielerdaten an das Backend - wird gebroadcastet an alle
  verbundenen Clients
- ws://localhost:13373/app/games/{gameId}/{playerId} - Bekanntgabe von Spielern aus der Lobby welche einem Spiel
  beitreten
- ws://localhost:13373/app/games/ - Veröffentlichung neuer Spiele
- ws://localhost:13373/app/players/ - Bekanntgabe des Beitritts von Spielern in die Lobby

#### HTTP / REST
Die folgenden URLs dienen als Adresse für die Kommunikation mittels HTTP-Protokoll

- http://localhost:13373/h2 (user: sa | pass: <none>)
- http://localhost:13373/api/spieler/
- http://localhost:13373/api/spieler/name
- http://localhost:13373/api/lobby
- http://localhost:13373/api/historie

## Architektur: Frontend
**BaseController** \
Das FrontEnd besitzt eine auf Vererbung basierende Controller-Hierarchie.
Jeder im Front-End verwendete Controller erbt von **BaseController** - einer abstrakten Oberklasse welche verschiedene Methoden besitzt und mittels Vererbung jedem ableitenden Controller zur Verfügung stellt.
Dazu zählen Beispielsweise:
- das Laden von Szenen (Scenes) mittels FXML-Datei (JavaFX Markup Language) in der selben Stage / Window. 
- verschiedene UI-bezogene Mechanismen wie das Dekorieren der Stage mittels Icon oder dem Aktualisieren des Stage-Titles, etc. 
- Eine Referenz auf die (JavaFX-)Application selbst welche im folgenden beschrieben wird.

<img src="docs/UML/Benni/BaseController.png">   


**BaseApplication** \
JavaFX-Anwendung besitzen als kleinsten gemeinsamen Nenner eine Vererbung zur Application-Klasse - diese Klasse selbst kann jedoch auch eine 
abgeleitete Klasse von Application sein. Dieser Sachverhalt kann genutzt um einen einzelnen Bezugspunkt von logisch zusammenhängender Mechanismen zu bilden.

Aus diesem Grund wird im SnakeFX-Projekt die Ausgangsklasse von BaseApplication abgeleitet.
Diese Klasse besitzt, ähnlich wie die BaseController, verschiedene Mechanismen welche jedoch unabhängig vom User-Interface verwendet werden können.
Darunter fallen Beispielsweise:
- Eine Referenz auf den StompClient welcher genutzt werden kann um mittels WebSockets mit dem Backend zu kommunizieren.
- Der UserConfig welche die Anmelde- und Sessiondaten des aktuellen Users besitzt.
- uvw.

Mithilfe dieser Vererbungshierarchie ist es auf einfache Art und Weise möglich mittels Betätigung einer Schaltfläche (bspw. der Betätigung eines Buttons) einen HTTP- oder eine WebSocket-Message an das Backend zu initiieren, 
Operationen im selben Fenster auszuführen, etc. - und da jeder Controller von BaseController erbt und dieser eine Referenz auf die BaseApplication besitzt können auch verschiedene Aspekte des Frontends gesteuert und kontrolliert werden. 

<img src="docs/UML/Benni/AppSnakeFX2.png">  

## Architektur: Backend
Das Backend setzt auf verschiedene Module des Spring-Ökosystems:
- Spring Data JPA - zum Aufbau und Management des Persitence-Layers
- Spring Web - zur Bereitstellung von Web-Inhalten und REST-Services 
- Spring Security - zur Bereitstellung und Verwendung von Sicherheits-Mechanismen
- Spring Messaging - zur Bereitstellung und Kommunikation mittels WebSockets / STOMP

In Kombination mit Spring Data JPA wird die H2-Datenbank verwendet welche eine in der Programmiersprache Java entwickelte RDBMS darstellt - der Vorteil vom Einsatz von H2 ist unter anderem, dass keine Einrichtung (Installation, Konfiguration, Startup) für einen dedizierten Datenbank-Server durchgeführt werden muss - da der RDBMS beim Applikations automatisch gestartet wird - gerade bei kleineren Datenmengen trägt H2 nur wenige zusätzliche Millisekunden Bootup-Time zur Anwendung bei.  

Im Package **controller** existieren verschiedene Spring-Controller welche Endpunkte für RESTful Webservices/STOMP-Endpoints darstellen.

Im Package **persistance** existieren verschiedene Definitionen von Repository-Interfaces, welche, wie der Name vermuten lässt, für konkrete Implementierungen des Repository-Patterns genutzt werden - Spring Data generiert diese anhand von Interface-Definitionen selbstständig wodurch der Implementierungsaufwand für ORM-bezogene Tätigkeiten stark reduziert werden kann.

Im Package **runner** befinden sich verschiedene Ausprägungen von Spring-ApplicationRunnern welche bspw. genutzt werden um Initialisierungsaufgaben durchzuführen (z.B. Test-Daten in der Datenbank zu initialisieren).

Im Package **ws.server** befinden sich verschiedene Spring-Konfigurationsklassen welche zur Initialisierung von WebSockets und Security-Aspekten genutzt werden.

## Tasks
Die folgende Liste enthält Aufgaben (Tasks) welche im Zuge des Moduls realisiert wurden oder noch realisiert werden müssen.

### Frontend
- Neue UI "LoginScreen"
  - Refactoring des RegisterScreens in den LoginScreen [DONE]
  - Impl. des User-Interfaces [DONE]
  - Anfrage an den Server -> Login [DONE]
  - Registrierung mittels Frontend am Backend [DONE]
  - Login mittels Frontend am Backend [DONE]
- Neue UI "Homescreen"
  - Impl. des User-Interfaces [DONE]
  - Wechsel von Login auf Homescreen (Scene-Handling) [DONE] 
  - Neue UI "Lobby" - ist Teil vom Homescreen [DONE]
  - Button: Betrete existierendes Spiel 
- Neue UI "Erstelle Spiel" 
  - Impl. des User-Interfaces [DONE]
  - Definition einer Spielrunde [DONE]
  - Senden der SpielDefinition an das Backend  [DONE]
- Neue UI "Spielhistorie" [DONE]
  - Impl. des User-Interfaces [DONE]
  - Bezug bisheriger Spielerunden vom Backend [DONE]
  - Anbindung von Front- and Backend und vice versa [DONE]
  - Impl. der ListCells [DONE]
- Neue UI "Spielrunde" o. "Aktives Spiel"
  - Beinhaltet alle Spieler einer Spielrunde [DONE]
  - Stellt die Spieler, deren Figuren, das Spielfeld und den Punktestand dar 
    - Die Spielerfiguren müssen bewegt werden [DONE]
    - PowerUps dargestellt werden
    - Die Map, evtl. Hindernisse oder sonstiges
    - Punktestand jedes Spielers
- Spieler kann Ende eines anderen Spielers abbeißen
- Erzeugung eines Spiels anhand der Spieldefinition [DONE]
- Teilnahme an Spiel in Lobby [DONE]
- UI zum starten des Spiels (nur für Admin d. Spiels) [DONE]
- Generation des STOMP-Path's für Running-Games i.d. Lobby
- Teilnahme an Spiel anhand von Stomp-Path [DONE]
- Freigabe des Admin-Buttons zum starten des Spiels [DONE]
- Starten des Spiels [DONE]
  - Initialisierung des Spiels auf allen Clients [DONE]
  - Sync. des Spiels [DONE]
  - Beendigung des Spiels
  - Persistenz d. Runde in DB
  - Return to Lobby
  - Cleanup
    - Unsubscribe von Lobby-Topics
    - Re-Subscribe nach erneuten beitreten eines Spiels
- ...

### Backend
- Akzeptanz von Registrierungs-Anforderungen (API)  [DONE]
  - Erstelle einen neuen Benutzer / Spieler im Backend (DB)  [DONE]
- Akzeptanz von Login-Anforderungen (API, Session-Handling)  [DONE]
  - Abgleich mit Usern in der Datenbank, erstelle eine Session falls b vorhanden  [DONE]
- Absicherung des Logins (z.B. mit bCrypt & Salts)
- Senden der Lobbydaten für Lobby auf dem Client  [DONE]
  - Verwaltung der Lobby  [DONE]
  - Empfang neuer Spielrunden-Definition vom Backend  [DONE]
- Bezug der Spielhistorie-Daten (Spielrunden) vom Backend zum Client (Auf Anfrage) (API) [DONE]
- Spieler tritt einem existierenden Spiel in der Lobby bei (Joining) [In Progress]
- Spieler verlässt Spiel
- ...

### Weitere Tasks
**Frontend und Backend**
- Auf SpielEnde / GameOver eines Clients reagieren
  - Sieger definieren
  - Persistenz des aktuellen Spiels für die SpielHistorie
  - switch zur Lobby zurück
  - unsubscribe des letzten game topics
- Regeln des Spiels einhalten
- Das Spielfeld muss mit der Spieldefinition gekoppelt werden:
  - wenn im Frontend bspw. eine Map-Höhe von 16 und eine Map-Breite von 48 eingestellt wird muss das im Spiel wiedergespiegelt werde
  - Über TODOs drüber schauen und behandeln
- WebSecurity impl.
  
**Frontend / SnakeGame**
- Spieler müssen einander das Ende des jeweils anderen abbeißen und "gut geschrieben" bekommen
- Spieler müssen sich durch Wände teleportieren können
- Food / PowerUps generieren - Design Patterns anwenden [in progress]
  - Event für Konsum wird versendet und empfangen [done]
  - Position der Elemente muss valide sein [done]
  - Es darf nur eine initiale Position generiert werden <-> unabhängig vom Client [done]
  - Food / PowerUps werden generiert und synchronisiert [done]
  - Konsum an Backend übertragen -> initial [done]
  - Konsum an Backend übertragen -> continouos [in progress]

**Backend**
- Valide Endpoints für Spiele in der Lobby generieren und an RunningGames anhängen 
  
**Allgemein**
- Abschließende Integration der Design-Pattern
- sout statements minimieren
- Magic Strings in konstanten refactoren
- Präsentation ausarbeiten
- Dokumentation ausarbeiten
- Test-Szenario: Ein Server -> drei Clients realisieren
- Test-Szenario: Mehrere Spiele hintereinander durchführen
- Code refactoren und vereinfachen


**Bugs**
- Positionen in nicht-admin Spielen sind um eine Zeile versetzt.


## Changelog
- 09.10.2020 - Sichtung der Anforderung, erste Besprechung der Aufgaben
- 09.10.2020 - Erster Entwurf der Diagramme, Update der Ressourcen
- 18.10.2020 - Überarbeitung der Diagramme
- 23.10.2020 - Entwurf von UI-Mockups, Ergänzung der Diagramme
- 24.10.2020 - Ergänzung der Diagramme
- 25.10.2020 - Partielle Implementierung der Schnittstelle
- 27.10.2020 - Vollständige Implementierung des Front-Ends (UI, ohne Logik - Szenenwechsel ist möglich)
- 28.10.2020 - Partielle Verbindung des Front- mit dem Back-Ends
- xy.11.2020
  - Implementierung Snake
  - Implementierung Snake, lokaler Multiplayer
  - Implementierung Snake, lokale KI (NPC)
- 28.11.2020 - Demonstration des aktuellen Standes, Vorschau des Prototypen
- 29.11.2020 - Überarbeitung und Einarbeitung des Feedbacks, sowohl von Gruppe als auch Dozent
- 03.12.2020 - Refactoring des Projekts, Impl. d. Relationen, Refactoring des Datenmodels,
- 17.12.2020 - Refactoring des Projekts, Impl. d. Relationen, Refactoring des Datenmodels,
- 23.12.2020
  - Refactoring des Projekts, Anbindung der UI an Backend, Abschluss d. Spielhistorie-Screens
  - Integration des STOMP-Servers & STOMP-Clients, Abschluss d. Home-Screens zu 75%
- 24.12.2020
  - Impl. des Chats mittels STOMP-Service
  - Realisierung der Lobby-Mechaniken, Abschluss zu 85%
- 26.12.2020 - Refactoring des Projekts, Update des Stomp-Services, Anbindung an das Frontend
- 27.12.2020 - Implementierung der Lobby-Mechaniken (Spieler-Join, Update der UI, Pulling und Polling)
- 28.12.2020 - Implementierung der Spieler-Eingaben Synchronisierung
  - Ein Spiel kann nun definiert und in der Lobby veröffentlicht werden
  - Solange die definierte max. Anzahl der Spieler nicht überschritten ist können beliebige Spieler dem Spiel beitreten
  - Sobald der Spiel-Admin / Game-Master / ... das Spiel startet wechseln alle Clients in den Spiel-Modus
- ... siehe [done] in der obigen Liste