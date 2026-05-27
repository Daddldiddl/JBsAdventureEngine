# JBsAdventureEngine

## Introduction
This Adventure Game Engine (which sounds way too impressive already) is just a small fun project I setup for for learning to program Kotlin - its simplistic, but you can actually create adventures by creating a json file (documentation is included in the repo) and pointing the game engine towards it. No coding required. See the included documentation and the section on Runnning Adventures below for details.

Have fun to expand this yourself or build some adventures for it.

### Some notes to limit expectations:
It's a learning project for me and my first work on an text adventure engine (except for that 'Write Adventures in BASIC' book I read once in the 80s), so expect there to be non-optimal solutions as I now come from a mainly Java 8 background with little Kotlin knowledge. But then I'm not trying to write the best (or even a very good) adventure engine - I'm trying to learn the language and have fun doing so.

### Regarding AI use
I used Copilot to setup the base project (I can't be arsed to write a pom.xml, sorry!), as well as using AI-based code-completion and debugging throughout the development and for refactoring. Also the guides were AI generated/translated. The rest is my own stupidity, of which there is plenty to go around.

## Running adventures

Requires Java 21 or newer to run (all dependencies are included in the jar). Start with:

`java -jar jbs-adventure-engine.jar`

to use an external data file use:

`java -jar jbs-adventure-engine.jar --data ./path/to/my/data.json`

to enable debug mode use:

`java -jar jbs-adventure-engine.jar --consoleDebug`

A log file will be written for each run if you enable that with the `--log` parameter.

Of course you can combine the command line parameters as you like.

## Planned features

Currently planned/in the works:

- basic i8n support is in - commands, directions, help page etc. already work, but not all responses are translated yet and some mechanisms are still missing. Also no support for localized data files yet - the data file is simply in the language its written in.
- allow item on item usage (currently you can only use an item in a room)
- allow item usages to have multiple actions (e.g. change state and move the player, move some items around)
- separate exits from rooms (mostly done)
- add an item usage action which will replace an item with another (e.g. using a 'sword' in the mysterious pool will change it to the 'magic sword')
- add an action adding/removing exits to rooms
- have state changes trigger actions
- add container items (allowing for stuff like a 'treasure chest' that contains a 'heap of gold' and 'Blackbeard's clean underpants')
- allow item states to be reflected in the items name - currently that is only visible in the description
- separate actions out from item usages (that might also be the way for the above multi-action and state trigger ideas)

What may be coming in the far future:

- maybe add NPCs as additional object cathegory with dialog options?
- add a stats (and combat?) system to allow for a 'roleplay-like' experience

These are the basic ideas - this is not going to be a huge effort, but I'd like to expand it a bit. Focus at the moment is:

- I8N is currently the main focus of this rework as it affects the structure - but there's also ongoing work on the Actions (separating them out from the ItemUsage) and containers.
- The idea is to provide the features required for more elaborate puzzles  and for developing better stories.
- The included 'adventure' is really just a testbed for the implemented features and serves more as an example for implementing them in an actual game. There is no treasure to be found (yet!) and the texts are (apart from some corrections and guidance) just Copilot's code completion proposals when editing the data.json.

Enjoy!
