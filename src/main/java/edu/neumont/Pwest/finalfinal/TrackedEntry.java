package edu.neumont.Pwest.finalfinal;

import java.io.Serializable;
import java.util.List;

public class TrackedEntry implements Serializable {
    public String speciesName;
    public int dexNumber;
    public List<String> availableGames;
    public String game;
    public int count;

    public TrackedEntry(String speciesName, int dexNumber, List<String> games) {
        this.speciesName = speciesName;
        this.dexNumber = dexNumber;
        this.availableGames = games;
        this.count = 0;
    }

    public String getSpeciesName() { return speciesName; }
    public int getDexNumber() { return dexNumber; }
    public List<String> getAvailableGames() { return availableGames; }
    public String getGame() { return game; }
    public void setAvailableGames(List<String> availableGames) { this.availableGames = availableGames; }
    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }

    public void setGame(String newGame) { this.game = newGame; }

}