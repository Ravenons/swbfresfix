swbfresfix
==========

Star Wars Battlefront Resolution Fix (custom resolution through savegame editing)

## What is this? ##

A non-invasive way of setting an in-game custom resolution in this game (forget about menu resolution, who cares?).

It works modifying savegames (profiles, extension .profile) located at "Star Wars Battlefront\GameData\SaveGames".

None other way worked for me, so I edited my profile using a hex editor, looking for the resolution there. I found it, but the game seemed to detect modification.

OllyDBG is my friend... Breakpoint in CreateFile / ReadFile / WriteFile Windows API... Checksum function dumped... Just check the source code if you need more details, or send me a message someway.

## Usage ##

It gets an existing profile filename, horizontal and vertical resolution, and creates the same profile but with the resolution changed (a valid profile).

Main class, 4 arguments (original savegame filename, modified savegame filename, horizontal resolution and vertical resolution).

## Disclaimer ##

No error checking (it's not production code) and bad architecture (I tried to mimic the reverse-engineered code).

Do whatever you want, I take no responsabilities. Consider it beerware.
