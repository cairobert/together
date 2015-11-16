package com.example.robert.together;

import android.content.Context;
import android.location.Location;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created by robert on 10/19/15.
 */
public class Person {
    private static final String TAG = "Person";

    private int mId;
    private String mName;
    private Date mBirthday;
    private int mAge = -1;
    private int mHeight = -1;
    private int mWeight = -1;
    private Gender mGender = Gender.INVALID;
    private String mProfession;
    private String mHometown;
    private int mDistance;  // does not have location, only distance
    private String mCompany;
    private String mSignature;
    private String mProfileUrl;     // in remote url
    private List<String> mPictureUrls;
    private String mJPushId;
    private Status mInviteStatus;
    private Location mLocation;

    public static List<Person> sPersons = new ArrayList<>();
    static {
        int ids[] = {0, 1};
        String[] names = {"robert", "james"};
        Date[] dates = {new GregorianCalendar(1992, 2, 3).getTime(), new GregorianCalendar(1990, 1, 1).getTime()};
        int[] heights = {168, 173};
        int[] weights = {60, 52};
        Gender[] genders = {Gender.MAN, Gender.WOMAN};
        String[] professions = {"程序员", "销售"};
        String[] hometowns = {"重庆", "浙江"};
        int[] distances = {10, 1003};
        String[] company = {"腾讯", "百度"};
        String[] signature = {"我喜欢吃麻辣烫", ""};
        Date[] lastLoginDates = {new Date(), new Date()};

        String[] urls = {"http://img0.bdstatic.com/img/image/shouye/mingxing1019.jpg", "http://img0.bdstatic.com/img/image/shouye/touxiang1019.jpg"};

        for (int i = 0; i < ids.length; i++) {
            Person p = new Person();
            p.setId(ids[i]);
            p.setName(names[i]);
            p.setBirthday(dates[i]);
            p.setHeight(heights[i]);
            p.setWeight(weights[i]);
            p.setGender(genders[i]);
            p.setProfession(professions[i]);
            p.setHometown(hometowns[i]);
            p.setDistance(distances[i]);
            p.setCompany(company[i]);
            p.setSignature(signature[i]);
            p.setProfileUrl(urls[i]);
            List<String> pics = new ArrayList<>();
            pics.add("http://t12.baidu.com/it/u=3194313170,3989673488&fm=32&s=C70267A74ED335EB05107D2603007040&w=532&h=799&img.JPEG");
            pics.add("http://e.hiphotos.baidu.com/image/pic/item/6609c93d70cf3bc7fbdba1edd200baa1cc112ad3.jpg");
            p.setPictureUrls(pics);
            sPersons.add(p);
        }
    }

    public static Person getPerson(int personId) {
        for (Person p: sPersons) {
            if (p.getId() == personId) {
                return p;
            }
        }
        return null;
    }

    public static List<Person> getPersons() {
        return sPersons;
    }

    public Person() {
        mPictureUrls = new ArrayList<>();
        mInviteStatus = Status.NOT_INVITED;
        mDistance = -1;
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public Date getBirthday() {
        return mBirthday;
    }

    public void setBirthday(Date birthday) {
        mBirthday = birthday;
        // TODO: 10/23/15 set date
        Calendar now = Calendar.getInstance();
        Calendar birth = Calendar.getInstance();
        birth.setTime(birthday);
        int age = now.get(Calendar.YEAR) - birth.get(Calendar.YEAR);
        Log.i(TAG, "now.age: " + now.get(Calendar.YEAR));
        Log.i(TAG, "birth.age: " + birth.get(Calendar.YEAR));
        if ((now.get(Calendar.MONTH) < birth.get(Calendar.MONTH))
                || ((now.get(Calendar.MONTH) == birth.get(Calendar.MONTH)
                        && now.get(Calendar.DAY_OF_MONTH) < birth.get(Calendar.DAY_OF_MONTH)))) {
            age--;
        }
        mAge = age;
    }

    public int getAge() {
        return mAge;
    }

    public Gender getGender() {
        return mGender;
    }

    public void setGender(Gender gender) {
        mGender = gender;
    }


    public String getProfileUrl() {
        return mProfileUrl;
    }

    public void setProfileUrl(String profileUrl) {
        mProfileUrl = profileUrl;
    }

    public List<String> getPictureUrls() {
        return mPictureUrls;
    }

    public void setPictureUrls(List<String> pictureUrls) {
        mPictureUrls = pictureUrls;
    }

    public String getProfession() {
        return mProfession;
    }

    public void setProfession(String profession) {
        mProfession = profession;
    }

    public int getDistance() {
        return mDistance;
    }

    public void setDistance(int distance) {
        mDistance = distance;
    }

    public String getCompany() {
        return mCompany;
    }

    public void setCompany(String company) {
        mCompany = company;
    }

    public String getSignature() {
        return mSignature;
    }

    public void setSignature(String signature) {
        mSignature = signature;
    }

    public int getHeight() {
        return mHeight;
    }

    public void setHeight(int height) {
        mHeight = height;
    }

    public int getWeight() {
        return mWeight;
    }

    public void setWeight(int weight) {
        mWeight = weight;
    }


    public String getHometown() {
        return mHometown;
    }

    public void setHometown(String hometown) {
        mHometown = hometown;
    }


    public String getJPushId() {
        return mJPushId;
    }

    public void setJPushId(String JPushId) {
        mJPushId = JPushId;
    }

    public Status getInviteStatus() {
        return mInviteStatus;
    }

    public void setInviteStatus(Status inviteStatus) {
        mInviteStatus = inviteStatus;
    }

    public enum Gender {
        MAN(0), WOMAN(1), INVALID(3);
        private int mValue;
        private Gender(int value) {
            mValue = value;
        }
    }

    public enum Status {
        NOT_INVITED(0), HAVE_INVITED(1), BEEN_INVITED(2);
        private Status(int value) {
        }
    }



    private static final String PREF_PERSON = "pref_person";
    private static int sSelfId = -1;

    public void deserialize(String personString) throws JSONException {
        Log.i(TAG, "person_info: " + personString);
        JSONObject jsonObject = new JSONObject(personString);
        if (jsonObject.has("id") && !jsonObject.isNull("id")) {
            mId = jsonObject.getInt("id");
        }

        if (jsonObject.has("name") && !jsonObject.isNull("name")) {
            mName = jsonObject.getString("name");
        }

        if (jsonObject.has("birthday") && !jsonObject.isNull("birthday")) {
            setBirthday(new Date(jsonObject.getLong("birthday")));
        }

        if (jsonObject.has("height") && !jsonObject.isNull("height")) {
            mHeight = jsonObject.getInt("height");
        }
        if (jsonObject.has("weight") && !jsonObject.isNull("weight")) {
            mWeight = jsonObject.getInt("weight");
        }
        if (jsonObject.has("gender") && !jsonObject.isNull("gender")) {
            int g = jsonObject.getInt("gender");
            if (g == Gender.MAN.mValue) {
                mGender = Gender.MAN;
            } else {
                mGender = Gender.WOMAN;
            }
        }
        if (jsonObject.has("profession") && !jsonObject.isNull("profession")) {
            mProfession = jsonObject.getString("profession");
        }
        if (jsonObject.has("hometown") && !jsonObject.isNull("hometown")) {
            mHometown = jsonObject.getString("hometown");
        }
        if (jsonObject.has("distance") && !jsonObject.isNull("distance")) {
            mDistance = jsonObject.getInt("distance");
        }
        if (jsonObject.has("company") && !jsonObject.isNull("company")) {
            mCompany = jsonObject.getString("company");
        }
        if (jsonObject.has("signature") && !jsonObject.isNull("signature")) {
            mSignature = jsonObject.getString("signature");
        }
        if (jsonObject.has("thumbnail")) {
            mProfileUrl = jsonObject.getString("thumbnail");
        }
        if (jsonObject.has("jpush_id") && !jsonObject.isNull("jpush_id")) {
            mJPushId = jsonObject.getString("jpush_id");
        }
        if (jsonObject.has("invite_status") && !jsonObject.isNull("invite_status")) {
            int status = jsonObject.getInt("invite_status");
            if (status == 1) {
                mInviteStatus = Status.HAVE_INVITED;
            } else if (status == 2) {
                mInviteStatus = Status.BEEN_INVITED;
            } else {
                mInviteStatus = Status.NOT_INVITED;
            }
            Log.i(TAG, "id: " + mId + ", person_status: " + mInviteStatus);
        }
        if (jsonObject.has("pictures") && !jsonObject.isNull("pictures")) {
            JSONArray pictures = jsonObject.getJSONArray("pictures");
            List<String> arrayList = new ArrayList<>();
            for (int i = 0; i < pictures.length(); i++) {
                JSONObject obj = pictures.getJSONObject(i);
                arrayList.add(obj.getString("url"));
            }
            mPictureUrls = arrayList;
        }
    }

    public void merge(Person p) {
        if (p == null || mId != p.getId()) {
            return;
        }

        mName = p.mName;
        setBirthday(p.mBirthday);
        mHeight = p.mHeight;
        mWeight = p.mWeight;
        mGender = p.mGender;
        mProfession = p.mProfession;
        mHometown = p.mHometown;
        mDistance = p.mDistance;
        mCompany = p.mCompany;
        mSignature = p.mSignature;
        mProfileUrl = p.mProfileUrl;
        if (p.mPictureUrls.size() != 0) {
            mPictureUrls.clear();
            mPictureUrls.addAll(p.mPictureUrls);
        }
        mJPushId = p.mJPushId;
        mInviteStatus = p.mInviteStatus;
    }
}


