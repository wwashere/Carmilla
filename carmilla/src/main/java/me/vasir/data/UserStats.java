package me.vasir.data;

public class UserStats {

    private final long id;
    private int level;
    private int xp;

    public UserStats(long id, int level, int xp) {
        this.id = id;
        this.level = level;
        this.xp = xp;
    }

    public long getId() {return id;}
    public int getLevel() {return level;}
    public void setLevel(int level) {this.level = level;}
    public int getXp() {return xp;}
    public void setXp(int xp) {this.xp = xp;}
}
