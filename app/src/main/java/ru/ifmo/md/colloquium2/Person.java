package ru.ifmo.md.colloquium2;

/**
 * Created by pokrasko on 11.11.14.
 */
public class Person {
    private long id;
    private String name;
    private long votes;

    public Person(long id, String name, long votes) {
        this.id = id;
        this.name = name;
        this.votes = votes;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getVotes() {
        return votes;
    }

    public void rename(String name) {
        this.name = name;
    }

    public void vote() {
        this.votes++;
    }
}
