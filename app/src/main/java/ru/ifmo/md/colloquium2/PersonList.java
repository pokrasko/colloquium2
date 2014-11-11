package ru.ifmo.md.colloquium2;

import java.util.ArrayList;

/**
 * Created by pokrasko on 11.11.14.
 */
public class PersonList {
    private ArrayList<Person> persons;

    public PersonList() {
        persons = new ArrayList<Person>();
    }

    public Person get(int i) {
        return persons.get(i);
    }

    public int size() {
        return persons.size();
    }

    public void add(Person person) {
        persons.add(person);
    }

    public void rename(int i, String name) {
        persons.get(i).rename(name);
    }

    public void vote(int i) {
        persons.get(i).vote();
    }

    public void remove(int i) {
        persons.remove(i);
    }
}
