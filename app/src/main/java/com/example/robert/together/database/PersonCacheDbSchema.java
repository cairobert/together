package com.example.robert.together.database;

/**
 * Created by robert on 10/30/15.
 */
public class PersonCacheDbSchema {
    public static final class PersonTable {
        public static final String NAME = "persons";

        public static final class Cols {
            public static final String ID = "_id";
            public static final String NAME = "name";
            public static final String BIRTHDAY = "birthday";
            public static final String AGE = "age";
            public static final String HEIGHT = "height";
            public static final String WEIGHT = "weight";
            public static final String GENDER = "gender";
            public static final String PROFESSION = "profession";
            public static final String HOMETOWN = "hometown";
            public static final String LOCATION_X = "locationX";
            public static final String LOCATION_Y = "locationY";
            public static final String COMPANY = "company";
            public static final String SIGNATURE = "signature";
            public static final String LAST_LOGIN_DATE = "last_login_date";
            public static final String PROFILE_URL = "profile_url";
        }
    }

    public static final class PictureTable {
        public static final String NAME = "pictures";

        public static final class Cols {
            public static final String ID = "_id";
            public static final String PERSON_ID = "person_id";
            public static final String URL = "url";
        }
    }
}
