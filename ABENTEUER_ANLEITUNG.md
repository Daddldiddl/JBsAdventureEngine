# Bau dein eigenes Abenteuer mit JB's Adventure Engine

Du willst ein eigenes Textadventure bauen? Sehr gute Idee.

Kein Stress: Du brauchst keinen riesigen Code-Skill, sondern vor allem gute Ideen.
Diese Anleitung zeigt dir das **aktuelle Datenmodell** - einfach und Schritt für Schritt.

## Kurzübersicht zum Modell (die Kurzfassung)

- Top-Level-Bereiche: `Rooms`, `Items`, `States`
- In einem `Room` können `itemUsages` definiert werden
- Jede `ItemUsage` enthält eine oder mehrere `actions`
- Container sind normale Einträge in `Items` (`isContainer: true`)
- Exits sind explizite Objekte (keine Richtungs-Map mehr)

## Befehle für Spieler

`go`, `look`, `examine`, `use`, `take`, `drop`, `open`, `close`, `inventory`, `save`, `load`, `help`, `quit`

---

## Top-Level-Struktur in JSON

```json
{
  "title": "Mein Abenteuer",
  "introductionMessage": "Willkommen!",
  "exitMessage": "Tschüss!",
  "Rooms": [ ... ],
  "Items": [ ... ],
  "States": [ ... ]
}
```

Wichtige Hinweise:
- Nutze `introductionMessage` für deinen Starttext.
- Es gibt keinen separaten Bereich `Containers` mehr.

---

## Namen sind jetzt Objekte

`name` ist nicht mehr nur ein String, sondern ein `Name`-Objekt:

```json
"name": {
  "name": "Schaltpult",
  "aliases": ["Pult", "Steuerung", "Knopf"]
}
```

Optionale Felder wie `genderKey` und `isPlural` werden unterstützt, aber in den meisten Fällen reichen `name` und `aliases` komplett aus.

---

## Rooms (Räume)

Ein Room ist einfach ein Ort auf deiner Karte.
Jeder Room hat eine ID, einen Namen, eine Beschreibung, Ausgänge und optional Item-Interaktionen.
Wenn deine Karte klar ist, fühlt sich dein Spiel direkt besser an.

```json
{
  "id": 5,
  "name": { "name": "Maschinenraum", "aliases": [] },
  "description": "Alte Maschinen laufen hier noch.",
  "exits": [
    { "direction": "east", "targetRoomId": 4 },
    { "direction": "north", "targetRoomId": 10 }
  ],
  "itemUsages": [ ... ]
}
```

`itemUsages` ist kleingeschrieben (`i`) und `exits` ist eine **Liste von Exit-Objekten**.
Klingt erstmal technisch, gibt dir aber deutlich mehr Kontrolle pro Ausgang.

### Exit-Objekt

```json
{
  "direction": "north",
  "name": { "name": "Eisentor", "aliases": ["Tor", "Tür"] },
  "targetRoomId": 2,
  "supportsOpenClose": false,
  "supportsLockUnlock": false,
  "open": true,
  "locked": false,
  "visible": true,
  "description": "Ein düsterer Höhleneingang."
}
```

Defaults (wenn du nichts angibst):
- Exits: `supportsOpenClose = false`, `supportsLockUnlock = false`

Tipp:
- Exits können ohne Namen funktionieren, aber ein eigener `name` ist meistens besser.
- Dann können Spieler z. B. `open tor` eingeben statt nur `open north`.

---

## Items (Gegenstände)

Items sind alle Dinge, die Spieler finden, anschauen, tragen oder benutzen können.

```json
{
  "id": 7,
  "name": { "name": "Sicherungspaket", "aliases": ["Sicherung", "Paket"] },
  "description": "Ein Paket mit Sicherungen.",
  "carriable": true,
  "numberOfUses": 2,
  "location": -2
}
```

Nützliche Location-Werte:
- `-1` = Inventar
- `-2` = in einem Container
- `0` = nicht zugewiesen / versteckt

### Container sind Items

```json
{
  "id": 12,
  "name": { "name": "Schatztruhe", "aliases": ["Truhe"] },
  "description": "Eine stabile alte Truhe.",
  "location": 9,
  "isContainer": true,
  "containedItems": [9],
  "open": false,
  "locked": true
}
```

Container-Defaults im Modell:
- `supportsOpenClose = true`
- `supportsLockUnlock = false` (vorerst; lock/unlock kann später über Actions gesteuert werden)

---

## States (Zustände)

States sind kleine Welt-Schalter:
z. B. "kaputt/betriebsbereit", "an/aus", "geschlossen/offen".

```json
{
  "stateKey": "ELEVATORPANEL_STATE",
  "description": "Das Schaltpanel wirkt <currentValue>.",
  "possibleValues": ["kaputt", "betriebsbereit"],
  "currentValue": "kaputt"
}
```

`stateKey` wird von Actions referenziert.

---

## ItemUsages und Actions (neues Modell)

Hier passiert die eigentliche Magie deiner Rätsel.
Wenn der Spieler Item X in Raum Y benutzt, laufen eine oder mehrere Actions.
Wenn mal etwas nicht so funktioniert wie gedacht, schau zuerst hier nach.

`itemUsages` stehen in Räumen und verwenden jetzt **`actions: []`**.

```json
{
  "itemId": 7,
  "consumeUsedItem": true,
  "becomesUsable": true,
  "actions": [
    {
      "type": "ChangeState",
      "description": "Du setzt die Sicherung ein.",
      "changedStateKey": "ELEVATORPANEL_STATE",
      "newStateValue": "betriebsbereit",
      "preconditions": [
        {
          "requiredStateKey": "ELEVATORPANEL_STATE",
          "requiredStateValues": ["kaputt"]
        }
      ]
    }
  ]
}
```

### Unterstützte Action-Typen

#### `MoveTo`

```json
{
  "type": "MoveTo",
  "description": "Du fährst mit dem Aufzug.",
  "moveToRoomId": 6
}
```

#### `SetItemRoom`

```json
{
  "type": "SetItemRoom",
  "description": "Du rufst den Aufzug.",
  "affectedItemIds": [2],
  "moveToRoomIdForItems": 5
}
```

#### `ChangeState`

```json
{
  "type": "ChangeState",
  "description": "Das Schaltpult erwacht zum Leben.",
  "changedStateKey": "ELEVATORPANEL_STATE",
  "newStateValue": "betriebsbereit"
}
```

#### `TransformIntoItem`

```json
{
  "type": "TransformIntoItem",
  "description": "Du schnitzt eine kleine Figur aus dem Holzklotz!",
  "affectedItemIds": [9],
  "transformsIntoItemIds": [10]
}
```

#### `ModifyExit`

```json
{
  "type": "ModifyExit",
  "description": "Ein Mechanismus klickt - und plötzlich ist der Weg frei.",
  "roomId": 5,
  "direction": "north",
  "open": true,
  "locked": false,
  "blocked": false,
  "visible": true
}
```

---

## Praktische Tipps

- Halte alle IDs in `Items` eindeutig (inkl. Container), sonst wird's chaotisch.
- Baue in kleinen Schritten: ein Raum, ein Rätsel, kurz testen, weiter.
- Nutze `comment` für deine Notizen - die Engine ignoriert das Feld.
- Nutze in Actions `description`, damit Spieler beim `use` direkt gutes Feedback bekommen.

## Dein erstes Mini-Abenteuer in 10 Minuten

Wenn du sofort loslegen willst: Kopier dieses Mini-Beispiel in deine `data.json`.
Das gibt dir 2 Räume und 1 Gegenstand zum Untersuchen.

```json
{
  "title": "Mein erstes Abenteuer",
  "introductionMessage": "Willkommen in deinem allerersten Spiel!",
  "exitMessage": "Stark! Du hast dein erstes Adventure gebaut.",
  "Rooms": [
    {
      "id": 1,
      "name": { "name": "Start-Raum", "aliases": [] },
      "description": "Du bist in einem kleinen Raum mit nur einer Tür.",
      "exits": [
        {
          "direction": "north",
          "name": { "name": "Holztür", "aliases": ["Tür"] },
          "targetRoomId": 2,
          "supportsOpenClose": true
        }
      ]
    },
    {
      "id": 2,
      "name": { "name": "Schatzraum", "aliases": [] },
      "description": "Du hast den Schatzraum gefunden. Nice!",
      "exits": [
        {
          "direction": "south",
          "name": { "name": "Holztür", "aliases": ["Tür"] },
          "targetRoomId": 1,
          "supportsOpenClose": true
        }
      ]
    }
  ],
  "Items": [
    {
      "id": 1,
      "name": { "name": "Notiz", "aliases": ["Zettel"] },
      "description": "Darauf steht: Weiter so, Spiele-Designer!",
      "carriable": true,
      "location": 1
    }
  ],
  "States": []
}
```

Dann probier diese Befehle:
- `look`
- `examine notiz`
- `take notiz`
- `go north`
- `go south`

Boom - dein erstes spielbares Abenteuer.
Jetzt noch einen Raum, ein Item und ein kleines Rätsel dazu - fertig.

Viel Spaß beim Bauen - fang klein an, dann mach's richtig cool.
