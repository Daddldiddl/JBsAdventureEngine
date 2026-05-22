# JBsAdventureEngine

## Introduction
This Adventure Game Engine (which sounds way too impressive already) is just a small fun project I setup for for learning to program Kotlin - its simplistic, but you can actually create adventures by creating a json file (documentation is included in the repo) and pointing the game engine towards it. No coding required. See the included documentation and the section on Runnning Adventures below for details.

Have fun to expand this yourself or build some adventures for it.

### Some notes to limit expectations:
It's a learning project for me and my first work on an text adventure engine (except for that 'Write Adventures in BASIC' book I read once in the 80s), so expect there to be non-optimal solutions as I now come from a mainly Java 8 background with little Kotlin knowledge. But then I'm not trying to write the best (or even a very good) adventure engine - I'm trying to learn the language and have fun doing so.

### Regarding AI use
I used Copilot to setup the base project (I can't be arsed to write a pom.xml, sorry!), as well using as AI-based code-completion and debug support throughout the development and for refactoring. The rest is my own stupidity, of which there is plenty to go around.

## Running adventures

Requires Java 21 or newer to run (all dependencies are included in the jar). Start with:

`java -jar jbs-adventure-engine.jar`

to use an external data file use:

`java -jar jbs-adventure-engine.jar --data ./path/to/my/data.json`

to enable debug mode use:

`java -jar jbs-adventure-engine.jar --consoleDebug`

A log file will be written for each run, but you can disable that with the `--nolog` parameter.

Of course you can combine the command line parameters as you like.

## Planned features

- allow commands in other languages than english: all game texts come from the data.json, but the commands are fixed in english currently. I guess 'take Goldfischglas' or 'use forno' sound weird if you want to write your adventure in another language...
- allow item on item usage (currently you can only use an item in a room)
- allow item usages to have multiple actions (e.g. change state and move the player, move some items around)
- add an item usage action which will replace an item with another (e.g. using a 'blunt sword' on a grinding wheel will make it a 'sharp sword')
- add an action adding/removing exits to rooms
- have state changes trigger actions
- maybe separate actions out from item usages (that might also be the way for the above multi-action and state trigger ideas)

These are the basic ideas - this is not going to be a huge effort, but I'd like to expand it a bit. I8N is probably the least prio, even though its first in the list. The others will allow for much more elaborate puzzles and to actually develop better stories. The included 'adventure' is really just a testbed for the implemented features. There is no treasure to be found (yet?) and the texts are (apart from some corrections and guidance) just Copilot's code completion proposals when editing the data.json.
