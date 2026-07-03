# BixisNavigator
Hotbar navigation plugin for YeditepeMC lobby.

## Features
- 4 persistent navigator items with full item protection via PersistentDataContainer
- Player visibility toggle (visual only, TAB list unaffected*)
- Builder mode support via /bixisnav toggle
- Automatic item restore on respawn and join

## Hotbar Layout
| Slot | Item | Action |
|------|------|--------|
| 0 | Recovery Compass | /oyunlar (server navigator) |
| 4 | Chest | /gmenu main (cosmetics) |
| 7 | Lime/Gray Dye | Player visibility toggle |
| 8 | Player Head | /profil |

## Commands
- `/bixisnav toggle` — enable/disable hotbar (builder mode)

## Requirements
- Paper 26.1.2
- Java 25

## Known Issues
- Player visibility toggle currently also hides players from TAB list (Paper API behavior, under investigation)

## Installation
Drop jar into plugins/ folder and restart.
