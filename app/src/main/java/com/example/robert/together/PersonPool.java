package com.example.robert.together;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by robert on 11/3/15.
 */
public class PersonPool {
    List<Person> mPersons;
    private Person mSelfPerson;

    private static PersonPool sPool;

    public static PersonPool getInstance(Context context) {
        if (sPool == null) {
            synchronized (Person.class) {
                sPool = new PersonPool(context.getApplicationContext());
            }
        }
        return sPool;
    }

    public PersonPool(Context ctx) {
        mPersons = new ArrayList<>();
        mSelfPerson = new Person();

    }

    public List<Person> getPersons() {
        return mPersons;
    }

    public Person getPerson(int personId) {
        for (Person p: mPersons) {
            if (p.getId() == personId) {
                return p;
            }
        }
        return null;
    }

    public Person getSelf() {
        return mSelfPerson;
    }

    public void setSelf(Person self) {
        mSelfPerson = self;
    }

    public boolean addPerson(Person person) {
        if (person == null || person.getId() < 0) {
            return false;
        }

        for (Person p: mPersons) {
            if (p.getId() == person.getId()) {
                return false;
            }
        }
        mPersons.add(person);
        return true;
    }
}
