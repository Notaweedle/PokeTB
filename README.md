# PokeTB — Pokédex Tracker & Team Builder

A JavaFX desktop app for keeping track of Pokémon and planning teams. It pulls live data from [PokéAPI](https://pokeapi.co/), so species, stats, moves, abilities and sprites are always up to date.

Built as the final project for Open Source Platforms Development at Neumont University.

## Features

- **Tracker** — search any Pokémon by name or National Dex number, add it to your list, pick the game it's from, and keep a running count for each entry.
- **Team Builder** — fill six team slots with a species, shiny toggle, four moves, ability, nature, and IVs/EVs (with the 510 EV total enforced).
- **Autocomplete** search with input validation, so typos and out-of-range IV/EV values are caught before anything is sent to the API.
- **Saves automatically** to `Documents/PokeTracker/` as JSON (`tracker.json` and `team.json`), so your list and team are still there next time.
- **Prof.Bug** — an in-app helper chat panel (work in progress).

## Built with

- Java (Java Platform Module System)
- JavaFX 21 (controls and FXML), styled with CSS
- [Gson](https://github.com/google/gson) for JSON parsing and saving
- Java `HttpClient` for calls to PokéAPI
- Regular expressions for search and IV/EV input validation
- JUnit 5 for tests (API parsing, input validation and save/load)
- Maven (with the Maven Wrapper)

## Running it

You need JDK 17 or newer (JavaFX 21 requires it) and an internet connection for PokéAPI. Maven doesn't need to be installed; the wrapper downloads it.

```bash
./mvnw clean javafx:run
```

On Windows, use `mvnw.cmd clean javafx:run`.

To run the tests:

```bash
./mvnw test
```
