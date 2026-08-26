package edu.neumont.Pwest.finalfinal;

import com.google.gson.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


public class APIController {

    final String BASE_URL = "https://pokeapi.co/api/v2/";
    final String POKE_URL = "https://pokeapi.co/api/v2/pokemon/";

    HttpClient client = HttpClient.newHttpClient();
    APIController(){}


    Pokemon fetchPokemon(String nameOrId) throws IOException, InterruptedException {

        // --- 1. sanitize input ---
        String cleaned = nameOrId.toLowerCase().trim();
//        IF cleaned fails your regex validation THEN
//        handle invalid input( throw /return empty)
//        END IF

        // --- 2. build + send request ---
        String  url = BASE_URL + "pokemon/" + cleaned;
        HttpRequest request =  HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET().build();


        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());



        // --- 3. check response ---
        if (response.statusCode() == 404) {
            throw new IOException("404 error" +response.statusCode());
        }else if(response.statusCode() != 200) {
            throw new IOException("status code " + response.statusCode());
        }


        // --- 4. parse JSON into your Pokemon ---
        String  json = (response.body());
        return buildPokemonFrom(json);


    }

    private int findStat(JsonArray statsArray, String statName) {
        for (JsonElement element : statsArray) {
            JsonObject entry = element.getAsJsonObject();
            String name = entry.get("stat").getAsJsonObject().get("name").getAsString();
            if (name.equals(statName)) {
                return entry.get("base_stat").getAsInt();
            }
        }
        throw new IllegalStateException("Stat not found: " + statName);
    }

    private List<String> extractNames(JsonArray array, String innerKey) {
        return StreamSupport.stream(array.spliterator(), false)
                .map(JsonElement::getAsJsonObject)
                .map(entry -> entry.get(innerKey).getAsJsonObject().get("name").getAsString())
                .collect(Collectors.toList());
    }

    Pokemon buildPokemonFrom(String json) {

        JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
        String name = obj.get("name").getAsString();
        int dexNumber = obj.get("id").getAsInt();
        String spriteUrl = obj.get("sprites").getAsJsonObject().get("front_default").getAsString();
        String shinyUrl = obj.get("sprites").getAsJsonObject().get("front_shiny").getAsString();

            JsonArray statsArray = obj.get("stats").getAsJsonArray();
            int hp = findStat(statsArray, "hp");
            int attack = findStat(statsArray, "attack");
            int defense = findStat(statsArray, "defense");
            int spAttack = findStat(statsArray, "special-attack");
            int spDefense = findStat(statsArray, "special-defense");
            int speed = findStat(statsArray, "speed");

        // both are lists of nested objects — map down to just the names
        List<String> abilities = extractNames(obj.get("abilities").getAsJsonArray(), "ability");
        List<String> moves     = extractNames(obj.get("moves").getAsJsonArray(), "move");

                // generation is NOT on this endpoint — see note below


          return  new Pokemon(name, dexNumber, hp, attack, defense,
                spAttack, spDefense, speed, abilities, moves,
                spriteUrl, shinyUrl);

    }

    List<String> allPokemonNames() throws IOException, InterruptedException {

        String url = BASE_URL + "pokemon-species?limit=100000&offset=0";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET().build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("status code " + response.statusCode());
        }

        JsonObject obj = JsonParser.parseString(response.body()).getAsJsonObject();
        JsonArray results = obj.get("results").getAsJsonArray();

        return StreamSupport.stream(results.spliterator(), false)
                .map(JsonElement::getAsJsonObject)
                .map(entry -> entry.get("name").getAsString())
                .collect(Collectors.toList());
    }

    String getPokeName(String nameOrId ) throws IOException, InterruptedException {

        String cleaned = nameOrId.toLowerCase().trim();
        String url = POKE_URL + cleaned;


        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET().build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());


        return null;
    }


}
