# Bau dein eigenes Abenteuer mit JB's Adventure Engine

Du willst dein eigenes Textadventure erstellen? Dann bist du hier genau richtig!  
Mit JB's Adventure Engine kannst du ein komplettes Spiel bauen, indem du einfach eine einzige JSON-Datei bearbeitest – kein Programmieren nötig. Du beschreibst die Welt, die Gegenstände und was passiert, wenn der Spieler etwas tut – der Rest läuft automatisch.

Diese Anleitung erklärt dir alles, was du brauchst.

---

## Wie das Spiel funktioniert (Der große Überblick)

Das Spiel ist ein klassisches **Textadventure**. Der Spieler tippt Befehle wie `go north`, `examine ladder` oder `use fuse pack` ein, und die Engine antwortet mit Text, der beschreibt, was passiert. Die gesamte Spielwelt steckt in einer einzigen Datei: `data.json`.

> **Hinweis:** Die Befehle im Spiel sind auf Englisch, da die Engine englische Schlüsselwörter verwendet. Die Texte in deinem Abenteuer – Raumbeschreibungen, Gegenstandsnamen, Nachrichten – kannst du aber auf Deutsch schreiben.

Der Spieler kann folgende Befehle eingeben:

| Befehl | Was er tut |
|---|---|
| `go north` / `n` / `north` | In eine Richtung gehen |
| `look` / `l` | Den aktuellen Raum beschreiben |
| `examine ladder` / `x ladder` | Die Beschreibung eines Gegenstands lesen |
| `use fuse pack` | Einen Gegenstand im aktuellen Raum benutzen |
| `take fuse pack` / `pickup` | Einen tragbaren Gegenstand aufheben |
| `drop fuse pack` | Einen mitgeführten Gegenstand ablegen |
| `inventory` / `i` | Mitgeführte Gegenstände auflisten |
| `save` / `load` | Spielstand speichern oder laden |
| `help` | Alle Befehle anzeigen |
| `quit` | Das Spiel beenden |

Es gibt vier Bausteine, aus denen du dein Abenteuer zusammensetzt:

1. **Rooms (Räume)** — die Orte, die der Spieler besuchen kann
2. **Items (Gegenstände)** — Objekte, die in der Welt existieren
3. **States (Zustände)** — Schalter, die den Zustand von Dingen festhalten
4. **ItemUsages (Gegenstandsaktionen)** — was passiert, wenn ein Gegenstand an einem bestimmten Ort benutzt wird

---

## Die Dateistruktur

`data.json` sieht immer so aus:

```json
{
    "title": "Mein cooles Abenteuer",
    "introductionMessage": "Willkommen, tapferer Entdecker!",
    "exitMessage": "Danke fürs Spielen!",
    "Rooms": [ ...Liste der Räume... ],
    "Items": [ ...Liste der Gegenstände... ],
    "States": [ ...Liste der Zustände... ]
}
```

| Feld | Pflichtfeld? | Beschreibung |
|---|---|---|
| `title` | Ja | Der Name deines Spiels, wird beim Start angezeigt |
| `introductionMessage` | Ja | Eine Begrüßungsnachricht vor dem Spielstart. Mit `\n` kannst du eine neue Zeile einfügen. |
| `exitMessage` | Ja | Eine Abschiedsnachricht, wenn der Spieler das Spiel beendet |
| `Rooms` | Ja | Die Liste aller Räume im Spiel |
| `Items` | Ja | Die Liste aller Gegenstände im Spiel |
| `States` | Nein | Optionale Liste von Zustandsschaltern (nur für Rätsel nötig) |

---

## Rooms (Räume)

Ein Raum ist jeder Ort, an dem sich der Spieler befinden kann – eine Kerker-Zelle, eine Waldlichtung, ein Raumschiffkorridor. Der Spieler startet immer im Raum mit der **id 1**.

```json
{
    "id": 1,
    "name": "Burgeingang",
    "description": "Du stehst vor einem massiven Eisentor.\nEin kalter Wind pfeift durch die Ritzen.",
    "exits": {
        "north": 2,
        "east": 3
    }
}
```

| Feld | Pflichtfeld? | Beschreibung |
|---|---|---|
| `id` | Ja | Eine eindeutige Zahl für diesen Raum. **Raum 1 ist immer der Startraum.** |
| `name` | Ja | Kurzer Name, der dem Spieler angezeigt wird (z. B. „Burgeingang") |
| `description` | Ja | Was der Spieler sieht, wenn er den Raum betritt oder `look` tippt. Mit `\n` Zeilenumbrüche einfügen. |
| `exits` | Ja | Welche Richtungen wohin führen. Kann leer `{}` sein für Sackgassen. |
| `ItemUsages` | Nein | Was passiert, wenn bestimmte Gegenstände hier benutzt werden (siehe Abschnitt ItemUsages) |
| `comment` | Nein | Eine Notiz für dich – das Spiel ignoriert dieses Feld vollständig |

### Ausgänge (exits)

Ausgänge sind eine einfache Zuordnung: Richtungsname → Raum-ID. Unterstützte Richtungen:

`north` (Norden), `south` (Süden), `east` (Osten), `west` (Westen), `up` (hoch), `down` (runter)

```json
"exits": {
    "north": 2,
    "west": 5,
    "up": 10
}
```

Wenn eine Richtung nicht aufgeführt ist, kann der Spieler dort nicht hindurch. Das war's!

> **Tipp:** Zeichne deine Karte zuerst auf Papier. Vergib jedem Ort eine Nummer, zeichne Pfeile für die Verbindungen und übersetze das dann in Ausgänge.

---

## Items (Gegenstände)

Ein Gegenstand ist jedes Objekt in der Spielwelt – ein Schlüssel, eine Fackel, ein magisches Schwert, eine kaputte Maschine. Gegenstände liegen in Räumen und warten darauf, dass der Spieler mit ihnen interagiert.

```json
{
    "id": 7,
    "name": "Sicherungspaket",
    "alternateNames": ["Sicherung", "Sicherungen", "Paket"],
    "description": "Ein kleines Paket mit Sicherungen. Es sind noch <numberOfUses> Sicherungen übrig.",
    "carriable": true,
    "numberOfUses": 2,
    "location": 3
}
```

| Feld | Pflichtfeld? | Beschreibung |
|---|---|---|
| `id` | Ja | Eine eindeutige Zahl für diesen Gegenstand |
| `name` | Ja | Der Hauptname des Gegenstands im Spiel |
| `alternateNames` | Ja | Andere Namen, die der Spieler tippen kann (sollte auch den Hauptnamen enthalten, wenn er funktionieren soll) |
| `description` | Ja | Wird angezeigt, wenn der Spieler `examine` eingibt |
| `location` | Ja | Die Raum-ID, in der der Gegenstand zu Spielbeginn liegt. `-1` legt ihn ins Inventar des Spielers. |
| `carriable` | Nein | `true`, wenn der Spieler den Gegenstand aufheben und tragen kann. Standard: `false` |
| `driveable` | Nein | `true`, wenn die Benutzung des Gegenstands den Spieler mit versetzt (z. B. ein Boot oder ein Aufzug). Standard: `false` |
| `usable` | Nein | `false` deaktiviert die Benutzung, bis etwas sie wieder aktiviert. Standard: `true` |
| `numberOfUses` | Nein | Wie oft der Gegenstand benutzt werden kann, bevor er verbraucht ist. Weglassen für unbegrenzte Nutzung. |
| `stateKey` | Nein | Verbindet diesen Gegenstand mit einem State – dessen Beschreibung wird beim Untersuchen angehängt |
| `comment` | Nein | Eine private Notiz, wird vom Spiel ignoriert |

### Der Platzhalter `<numberOfUses>`

Wenn du `numberOfUses` bei einem Gegenstand angibst, kannst du `<numberOfUses>` irgendwo in der `description` einbauen. Die Engine ersetzt es automatisch durch die tatsächliche Restanzahl:

```json
"description": "Eine Fackel. Sie hält noch <numberOfUses> Minuten."
```

### Gegenstände, die der Spieler von Anfang an dabei hat

Setze `"location": -1` und der Gegenstand liegt von Spielbeginn an im Inventar des Spielers.

---

## States (Zustände)

States sind wie Schalter oder Etiketten, die den Zustand von etwas in der Welt festhalten. Eine Tür kann zum Beispiel `verschlossen` oder `offen` sein; eine Maschine kann `kaputt` oder `betriebsbereit` sein.

```json
{
    "stateKey": "TUER_ZUSTAND",
    "description": "Die Tür sieht <currentValue> aus.",
    "possibleValues": ["verschlossen", "offen"],
    "currentValue": "verschlossen"
}
```

| Feld | Pflichtfeld? | Beschreibung |
|---|---|---|
| `stateKey` | Ja | Ein eindeutiger Name, den du dir ausdenkst (keine Leerzeichen). Wird von Items und Usages referenziert. Konvention: GROSSBUCHSTABEN, z. B. `TUER_ZUSTAND`. |
| `description` | Ja | Text, der an die Untersuchungsausgabe eines Gegenstands angehängt wird. `<currentValue>` wird durch den aktuellen Zustand ersetzt. |
| `possibleValues` | Ja | Die Liste aller möglichen Zustandswerte |
| `currentValue` | Ja | Der Startwert (muss in `possibleValues` enthalten sein) |
| `comment` | Nein | Eine private Notiz, wird vom Spiel ignoriert |

### Einen State mit einem Gegenstand verknüpfen

Füge das Feld `stateKey` zu einem Gegenstand hinzu. Wenn der Spieler diesen Gegenstand dann untersucht, wird die State-Beschreibung automatisch nach der eigentlichen Gegenstandsbeschreibung angezeigt:

```json
{
    "id": 3,
    "name": "Schaltpult",
    "alternateNames": ["Pult", "Steuerung", "Knopf"],
    "description": "Ein robustes Schaltpult mit einem Rufknopf. An der Seite ist eine Sicherungsfassung.",
    "location": 5,
    "stateKey": "AUFZUGPULT_ZUSTAND",
    "usable": false
}
```
Mit dem State `"currentValue": "kaputt"` zeigt das Untersuchen des Pults Folgendes:  
*„Ein robustes Schaltpult mit einem Rufknopf. An der Seite ist eine Sicherungsfassung.  
Das Aufzugpult sieht kaputt aus."*

---

## ItemUsages — Dinge passieren lassen

Hier liegt die eigentliche Magie. Eine `ItemUsage` sagt: *„Wenn der Spieler Gegenstand X benutzt, während er in Raum Y steht, passiert Z."*

`ItemUsages` werden **innerhalb des Raums** definiert, in dem sie gelten:

```json
{
    "id": 5,
    "name": "Maschinenraum",
    ...
    "ItemUsages": [
        {
            "itemId": 2,
            "description": "Du steigst in den Aufzug und ziehst den Hebel.\nDer Aufzug fährt ruhig zur unteren Ebene hinab.",
            "action": "MoveTo",
            "moveToRoomId": 6
        }
    ]
}
```

Jede ItemUsage hat eine Grundmenge von Feldern, plus zusätzliche Felder je nach `action`-Typ.

### Grundfelder (immer vorhanden)

| Feld | Pflichtfeld? | Beschreibung |
|---|---|---|
| `itemId` | Ja | Die ID des benutzten Gegenstands |
| `description` | Ja | Der Text, der dem Spieler bei der Benutzung angezeigt wird |
| `action` | Ja | Was tatsächlich passiert: `MoveTo`, `SetItemRoom` oder `ChangeState` |
| `consumeUsedItem` | Nein | `true` entfernt eine Verwendung (oder zerstört den Gegenstand, wenn `numberOfUses` auf null sinkt). Standard: `false` |

---

### Aktion: `MoveTo` — Den Spieler versetzen

Bewegt den Spieler in einen anderen Raum. Verwende das für Fahrzeuge, Leitern, Falltüren, Teleporter oder alles, was den Spieler transportiert.

```json
{
    "itemId": 1,
    "description": "Du steigst ins Boot und ruderst über den Untersee.",
    "action": "MoveTo",
    "moveToRoomId": 9
}
```

| Zusatzfeld | Pflichtfeld? | Beschreibung |
|---|---|---|
| `moveToRoomId` | Ja | Die Raum-ID, in die der Spieler versetzt wird |

> **Beispiele:** ein Boot, ein Aufzug, eine Seilleiter, ein magisches Portal.

---

### Aktion: `SetItemRoom` — Einen Gegenstand in einen Raum verschieben

Bewegt einen anderen Gegenstand in einen bestimmten Raum. Verwende das, wenn die Benutzung von etwas dazu führt, dass ein Objekt erscheint oder sich verlagert – zum Beispiel das Drücken eines Knopfs, um einen Aufzug auf deine Etage zu rufen.

```json
{
    "itemId": 3,
    "description": "Du drückst den Knopf. Der Aufzug summt und kommt auf dieser Ebene an.",
    "action": "SetItemRoom",
    "affectedItemId": 2,
    "moveToRoomId": 5
}
```

| Zusatzfeld | Pflichtfeld? | Beschreibung |
|---|---|---|
| `affectedItemId` | Ja | Die ID des Gegenstands, der verschoben wird |
| `moveToRoomId` | Ja | Der Raum, in den der betroffene Gegenstand verschoben wird |

> **Beispiele:** einen Aufzug rufen, eine Seilwinde, die eine Brücke herablässt (schiebt das Brücken-Item in den aktuellen Raum), ein Knopf, der einen Schlüssel erscheinen lässt.

---

### Aktion: `ChangeState` — Einen Zustandsschalter umlegen

Ändert einen State-Wert UND kann auch den `usable`-Schalter eines Gegenstands umschalten. Verwende das für Rätsel, bei denen das Reparieren oder Aktivieren von etwas neue Aktionen freischaltet.

```json
{
    "itemId": 7,
    "description": "Du steckst die Sicherung in die Fassung. Das Pult erwacht zum Leben!",
    "action": "ChangeState",
    "stateKey": "AUFZUGPULT_ZUSTAND",
    "oldStateValue": "kaputt",
    "newStateValue": "betriebsbereit",
    "affectedItemId": 3,
    "becomesUsable": true,
    "consumeUsedItem": true
}
```

| Zusatzfeld | Pflichtfeld? | Beschreibung |
|---|---|---|
| `stateKey` | Ja | Der State, der geändert wird |
| `oldStateValue` | Ja | Der Wert, den der State aktuell haben muss, damit diese Aktion ausgeführt wird (eine Bedingung) |
| `newStateValue` | Ja | Der Wert, auf den der State geändert wird |
| `affectedItemId` | Nein | Ein Gegenstand, dessen `usable`-Schalter durch diese Aktion umgelegt wird |
| `becomesUsable` | Nein | `true` aktiviert den betroffenen Gegenstand, `false` deaktiviert ihn |
| `consumeUsedItem` | Nein | `true` verbraucht eine Nutzung des benutzten Gegenstands (meistens `true` bei Einweggegenständen wie Sicherungen) |

> **Beispiele:** eine kaputte Maschine mit einem Ersatzteil reparieren, eine Tür mit einem Schlüssel aufschließen, ein magisches Artefakt aktivieren.

---

## Wie Räume, Gegenstände, Aktionen und Zustände zusammenspielen

Lass uns ein vollständiges Mini-Rätsel durchgehen, um zu sehen, wie alles zusammenhängt.

### Das Rätsel: Den Aufzug reparieren

Der Spieler muss zu einem unterirdischen See hinunter, aber der Aufzug funktioniert nicht, weil das Schaltpult kaputt ist. Um ihn zu reparieren, muss der Spieler ein Sicherungspaket finden und es dann am Pult benutzen.

**Schritt 1 — Den State definieren**

```json
{
    "stateKey": "PULT_ZUSTAND",
    "description": "Das Pult sieht <currentValue> aus.",
    "possibleValues": ["kaputt", "betriebsbereit"],
    "currentValue": "kaputt"
}
```

**Schritt 2 — Die Gegenstände definieren**

Das Sicherungspaket liegt in einem weit entfernten Raum. Das Schaltpult ist standardmäßig `usable: false` (der Spieler kann es erst benutzen, wenn es repariert ist).

```json
{ "id": 1, "name": "Sicherungspaket", "alternateNames": ["Sicherung", "Paket"],
  "description": "Ein Paket mit Ersatzsicherungen.", "carriable": true,
  "numberOfUses": 1, "location": 8 },

{ "id": 2, "name": "Schaltpult", "alternateNames": ["Pult", "Knopf"],
  "description": "Ein Schaltpult mit einer Sicherungsfassung.",
  "location": 5, "stateKey": "PULT_ZUSTAND", "usable": false },

{ "id": 3, "name": "Aufzug", "alternateNames": ["Fahrstuhl"],
  "description": "Ein solider Aufzugkäfig.", "driveable": true, "location": 5 }
```

**Schritt 3 — Definieren, was passiert, wenn die Sicherung benutzt wird (im Raum des Pults)**

```json
{
    "id": 5,
    "name": "Maschinenraum",
    "description": "Ein alter Maschinenraum. An der Wand hängt ein Schaltpult.",
    "exits": { "east": 4 },
    "ItemUsages": [
        {
            "itemId": 1,
            "description": "Du steckst die Sicherung ein. Das Pult summt wieder!",
            "action": "ChangeState",
            "stateKey": "PULT_ZUSTAND",
            "oldStateValue": "kaputt",
            "newStateValue": "betriebsbereit",
            "affectedItemId": 2,
            "becomesUsable": true,
            "consumeUsedItem": true
        },
        {
            "itemId": 2,
            "description": "Du drückst den Knopf. Der Aufzug fährt zur unteren Ebene.",
            "action": "MoveTo",
            "moveToRoomId": 6
        }
    ]
}
```

**Was der Spieler erlebt:**

1. Spieler findet das Sicherungspaket in Raum 8 und tippt `take Sicherungspaket`.
2. Spieler navigiert zum Maschinenraum (Raum 5).
3. Spieler tippt `examine Schaltpult` → sieht: *„Ein Schaltpult mit einer Sicherungsfassung. Das Pult sieht kaputt aus."*
4. Spieler tippt `use Sicherungspaket` → Sicherung wird verbraucht, State wechselt auf `betriebsbereit`, Pult wird benutzbar.
5. Spieler tippt `use Schaltpult` → der Aufzug bringt ihn zur unteren Ebene.

Das ist ein vollständiges Rätsel, gebaut aus ein paar Zeilen JSON!

---

## Tipps und häufige Fehler

**Jeder Raum braucht eine eindeutige ID, genauso jeder Gegenstand.**  
Wenn zwei Räume dieselbe ID haben, funktioniert einer davon einfach nicht richtig. Halte eine Liste auf Papier oder in einer Tabelle.

**Raum 1 ist immer der Startraum.**  
Der Spieler beginnt immer dort – also mach ihn zu einem sinnvollen Ausgangspunkt.

**`alternateNames` muss alles enthalten, was der Spieler tippen könnte.**  
Wenn der Name `"Sicherungspaket"` ist, du aber nicht `"Sicherung"` in `alternateNames` einträgst, funktioniert `use Sicherung` nicht.

**Verwende `\n` für Zeilenumbrüche in allen Textfeldern.**  
Das gilt für `description`, `introductionMessage`, `exitMessage` und ItemUsage-Beschreibungen.

**`ChangeState` wird nur ausgeführt, wenn der State aktuell `oldStateValue` hat.**  
Das bedeutet: eine Sicherung an einem bereits reparierten Pult zu benutzen, bewirkt nichts (und zeigt nichts an). Wenn du eine andere Nachricht möchtest, kannst du eine zweite ItemUsage für denselben Gegenstand hinzufügen – aber die Engine handelt nur bei der ersten, deren Bedingung erfüllt ist.

**Nutze `comment` nach Belieben.**  
Die Spiel-Engine ignoriert `comment`-Felder vollständig. Nutze sie für Notizen an dich selbst, z. B.: *„Das ist der Aufzug zwischen Räumen 5 und 6"*.

**Teste während des Bauens.**  
Füge einen Raum nach dem anderen hinzu, teste ihn, dann füge den nächsten hinzu. Probleme früh zu finden ist viel einfacher, als ein 30-Raum-Abenteuer auf einmal zu debuggen.

---

## Vollständige Kurzreferenz

### Oberste Ebene

```
title               string   — Spieltitel
introductionMessage string   — Angezeigt beim Start (\n = neue Zeile)
exitMessage         string   — Angezeigt beim Beenden
Rooms               array    — Liste der Room-Objekte
Items               array    — Liste der Item-Objekte
States              array    — Liste der State-Objekte (optional)
```

### Room (Raum)

```
id          integer  — Eindeutige Raum-ID (1 = Start)
name        string   — Kurzer Ortsname
description string   — Raumbeschreibung (\n = neue Zeile)
exits       object   — { "richtung": raumId, ... }
ItemUsages  array    — Liste der ItemUsage-Objekte (optional)
comment     string   — Deine private Notiz (wird ignoriert)
```

### Item (Gegenstand)

```
id             integer  — Eindeutige Gegenstand-ID
name           string   — Hauptname des Gegenstands
alternateNames array    — Weitere Namen, die der Spieler tippen kann
description    string   — Angezeigt bei examine (\n = neue Zeile, <numberOfUses> = Restanzahl)
location       integer  — Start-Raum-ID (-1 = Spielerinventar)
carriable      boolean  — Kann der Spieler ihn aufheben? (Standard: false)
driveable      boolean  — Versetzt die Benutzung den Spieler mit? (Standard: false)
usable         boolean  — Kann der Spieler ihn aktuell benutzen? (Standard: true)
numberOfUses   integer  — Nutzungen vor Verbrauch (weglassen für unbegrenzt)
stateKey       string   — Verknüpft diesen Gegenstand mit einem State
comment        string   — Deine private Notiz (wird ignoriert)
```

### State (Zustand)

```
stateKey       string   — Eindeutiger State-Name (z. B. "TUER_ZUSTAND")
description    string   — Bei examine angehängt (<currentValue> = ersetzt)
possibleValues array    — Alle gültigen Werte
currentValue   string   — Startwert (muss in possibleValues enthalten sein)
comment        string   — Deine private Notiz (wird ignoriert)
```

### ItemUsage (Gegenstandsaktion)

```
itemId          integer  — Gegenstand, der diese Aktion auslöst
description     string   — Text, der dem Spieler angezeigt wird
action          string   — "MoveTo" | "SetItemRoom" | "ChangeState"
consumeUsedItem boolean  — Eine Nutzung des itemId verbrauchen? (Standard: false)

--- Für MoveTo ---
moveToRoomId    integer  — Raum, in den der Spieler versetzt wird

--- Für SetItemRoom ---
affectedItemId  integer  — Zu verschiebender Gegenstand
moveToRoomId    integer  — Raum, in den der Gegenstand verschoben wird

--- Für ChangeState ---
stateKey        string   — Zu ändernder State
oldStateValue   string   — State muss diesen Wert haben, damit die Aktion ausgeführt wird
newStateValue   string   — State wird auf diesen Wert gesetzt
affectedItemId  integer  — Gegenstand, dessen usable-Schalter umgelegt wird (optional)
becomesUsable   boolean  — Neuer usable-Wert für den betroffenen Gegenstand (optional)
```

---

Jetzt leg los und bau etwas Tolles! Fang klein an – ein Drei-Räume-Dungeon mit einer gesperrten Tür ist ein großartiges erstes Projekt. Dann kannst du mehr Räume, mehr Rätsel und mehr Überraschungen hinzufügen. Die einzige Grenze ist deine Fantasie.
