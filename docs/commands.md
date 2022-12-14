# Commands
Commands are an important part of Bots. That's also what Allium makes use of, and not a small portion of it too.

This Document shall help if you ever need help with Commands.

## Modrinth Commands
The Modrinth Extension is a fairly useful one. It contains Commands to search Modrinth Projects or Users.
The User Search is a bit stricter as it tries to get a valid response.

Info: Arguments in ``<>`` are mandatory, while arguments in ``()`` are optional

---

``/modrinth user <query>``

- Query: User to search. returns a "Not found" if the API responds with a 404 code.

---
``/modrinth project <query> (limit)``

- Query: Project to search. Returns a paginator when multiple results are found.
- Limit: Limits the amount of results in the paginator. Default: 5

---

---

## About Command

The about command is a command to see information about the bot. Also contains the current build.

---

## Mapping Commands

This Bot also has the associated mapping commands from the [KordEx Mappings Extension](https://github.com/Kord-Extensions/kord-extensions/tree/root/extra-modules/extra-mappings)

---

---

### More commands to come...