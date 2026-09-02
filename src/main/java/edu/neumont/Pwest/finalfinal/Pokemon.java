package edu.neumont.Pwest.finalfinal;

import java.util.List;

public class Pokemon {
    private String name;
    private int nationalDexNumber;
    private int hp;
    private int attack;
    private int defense;
    private int spAttack;
    private int spDefense;
    private int speed;
    private List<String> abilities;
    private List<String> learnableMoves;
    private List<String> gamesIncludedIn;
    private String spriteUrl;
    private String shinySpriteUrl;

    public Pokemon(String name, int nationalDexNumber,
                   int hp, int attack, int defense, int spAttack, int spDefense, int speed,
                   List<String> abilities, List<String> learnableMoves,
                   String spriteUrl, String shinySpriteUrl,List<String> gamesIncludedIn) {
        this.name = name;
        this.nationalDexNumber = nationalDexNumber;
        this.hp = hp;
        this.attack = attack;
        this.defense = defense;
        this.spAttack = spAttack;
        this.spDefense = spDefense;
        this.speed = speed;
        this.abilities = abilities;
        this.learnableMoves = learnableMoves;
        this.spriteUrl = spriteUrl;
        this.shinySpriteUrl = shinySpriteUrl;
        this.gamesIncludedIn = gamesIncludedIn;
    }

    public String getName() { return name; }
    public int getNationalDexNumber() { return nationalDexNumber; }
    public int getHp() { return hp; }
    public int getAttack() { return attack; }
    public int getDefense() { return defense; }
    public int getSpAttack() { return spAttack; }
    public int getSpDefense() { return spDefense; }
    public int getSpeed() { return speed; }
    public List<String> getGamesIncludedIn() { return gamesIncludedIn; }
    public List<String> getAbilities() { return abilities; }
    public List<String> getLearnableMoves() { return learnableMoves; }
    public String getSpriteUrl() { return spriteUrl; }
    public String getShinySpriteUrl() { return shinySpriteUrl; }

}
