package com.example.robert.together;

import android.content.Context;
import android.util.Log;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by robert on 11/1/15.
 */
public class PersonPicker {
    public static final String ATTR_SIGNATURE = "signature";
    public static final String ATTR_THUMBNAIL = "thumbnail";
    public static final String ATTR_HEIGHT = "height";
    public static final String ATTR_WEIGHT = "weight";
    public static final String ATTR_HOMETOWN = "hometown";
    public static final String ATTR_PROFESSION = "profession";
    public static final String ATTR_COMPANY = "company";
    public static final String ATTR_BIRTHDAY = "birthday";
    public static final String ATTR_PICTURE = "pictures";


    private static final String ACTION_PERSON = "action_type";
    private static final int ACTION_PERSON_UPDATE = 2;

    private static final int ACTION_IMAGE_CREATE = 1;
    private static final int ACTION_IMAGE_UPDATE = 2;
    private static final int ACTION_IMAGE_DELETE = 3;

    private static final int ACTION_THUMBNAIL_UPDATE = 2;
    private static final int ACTION_THUMBNAIL_DELETE = 3;


    private static final String TAG = "PersonPicker";

    public static PersonPicker sPicker;
    private static OkHttpClient sClient;
    private static final String SERVER = "http://hinsk.com/together";
    private static final String PATH_PERSON = String.format("%s/%s", SERVER, "persons.php");
    private static final String PATH_INVITE = String.format("%s/%s", SERVER, "invite.php");
    private static final String PATH_NEARBY = String.format("%s/%s", SERVER, "recent.php");
    private static final String PATH_LOGIN = String.format("%s/%s", SERVER, "login.php");
    private static final String PATH_IMAGE = String.format("%s/%s", SERVER, "pictures.php") ;
    private static final String PATH_THUMBNAIL = String.format("%s/%s", SERVER, "thumbnail.php");


    private static final MediaType TYPE_JSON = MediaType.parse("application/json; charset=utf-8");
    private static final MediaType TYPE_JPG = MediaType.parse("image/jpg");

    private static final int INVITE_ACTION_TYPE_INVITE = 0;
    private static final int INVITE_ACTION_TYPE_AGREE = 1;
    private static final int INVITE_ACTION_TYPE_DENY = 2;

    private static final int ACTION_CREATE = 1;
    private static final int ACTION_UPDATE = 2;

    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String HTTP_CODE = "code";
    public static final String HTTP_MSG = "msg";
    public static final String PERSON = "person";


    private Context mContext;
    public static PersonPicker getInstance(Context ctx) {
        if (sPicker == null) {
            synchronized (PersonPicker.class) {
                sPicker = new PersonPicker(ctx);
                sClient = OkHttpUtil.getClient(ctx);
            }
        }
        return sPicker;
    }

    private PersonPicker(Context ctx) {
        mContext = ctx;
    }


    public void invitePerson(int to_id, Person.Status curStatus, String msg) {

        invitePerson(PersonPool.getInstance(mContext).getSelf().getId(), to_id, msg, curStatus, new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.i(TAG, "inviteResponse, failed: " + request.body());
            }

            @Override
            public void onResponse(Response response) throws IOException {
                Log.i(TAG, "inviteResponse, ok: " + response.body().string());
            }
        });
    }

    public void invitePerson(int from_id, int to_id, String msg, Person.Status curStatus, Callback callback) {
        String jsonString = createInviteJson(from_id, to_id, msg, curStatus);
        Log.i(TAG, "jsonString: " + jsonString);
        RequestBody body = RequestBody.create(TYPE_JSON, jsonString);
        Request req = new Request.Builder()
                .url(PATH_INVITE)
                .post(body)
                .build();
        Log.i(TAG, "invitePerson, request: " + req);
        sClient.newCall(req).enqueue(callback);
    }

    private String createInviteJson(int from_id, int to_id, String msg, Person.Status curStatus) {
        int action_type = 0;
        if (curStatus == Person.Status.BEEN_INVITED) {
            action_type = 1;        // agree
        } else if (curStatus == Person.Status.HAVE_INVITED) {
            action_type = 0;
        } else {        // not invited, then invite
            action_type = 0;
        }

        return String.format("{\"from_id\":%d, \"to_id\":%d,\"msg\":\"%s\", \"action_type\":%d}", from_id, to_id, msg, action_type);
    }

    public void fetchSelf(final Runnable callback) {
        fetchPersonInfo(PersonPool.getInstance(mContext).getSelf().getId(), new OnPersonFetchFinished() {
            @Override
            public void personFetchFinished(Person[] personsFetched) {
                if (personsFetched.length == 1) {
                    Person selfPerson = PersonPool.getInstance(mContext).getSelf();
                    selfPerson.merge(personsFetched[0]);
                    if (callback != null) {
                        callback.run();
                    }
                }
            }
        });
    }

    public void fetchPersonInfo(int id, final OnPersonFetchFinished finishedCallback) {
        Request req = new Request.Builder()
                .url(String.format("%s?id=%d&src_id=%d", PATH_PERSON, id, PersonPool.getInstance(mContext).getSelf().getId()))
                .build();

        sClient.newCall(req).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Toaster.getInstance(mContext).showToast("获取信息失败。");
            }

            @Override
            public void onResponse(Response response) throws IOException {
                ResponseBody body = response.body();
                String jsonString = body.string();
                Log.i(TAG, "response: " + jsonString);
                try {
                    final JSONObject jsonObject = new JSONObject(jsonString);
                    int code = jsonObject.getInt(PersonPicker.HTTP_CODE);
                    Log.i(TAG, "onResponse, code " + code);
                    Person person = new Person();
                    if (code != 0) {
                        final String errMsg = jsonObject.getString(PersonPicker.HTTP_MSG);
                        Log.e(TAG, "code: " + code + ", issuePersonInfo: " + errMsg);
                        Toaster.getInstance(mContext).showToast(errMsg);

                    } else {
                        String personString = jsonObject.getString(PersonPicker.PERSON);
                        person.deserialize(personString);
                        finishedCallback.personFetchFinished(new Person[]{person});
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "issuePersonInfo: " + e);
                    Toaster.getInstance(mContext).showToast("获取信息失败。");
                }
            }
        });
    }

    public void fetchNearbyPersons(int id, int start, Callback callback) {
        fetchNearbyPersons(id, start, 10, callback);
    }
    public void fetchNearbyPersons(int id, int start, int num, Callback callback) {
        RequestBody form = new FormEncodingBuilder()
                .add("id", "" + "" + id)
                .add("num", "" + num)
                .add("num_start", "" + start)
                .build();

        Request request = new Request.Builder()
                .url(PATH_NEARBY)
                .post(form)
                .build();

        sClient.newCall(request).enqueue(callback);
    }

    public void register(String name, String encPassword,  boolean isMan,  long birthday,  Callback callback) {
        String jsonString = String.format("{\"name\":\"%s\", \"gender\":%d, \"birthday\":%d, \"action_type\":%d, \"password\":\"%s\"}", name, isMan?0:1, birthday, ACTION_CREATE, encPassword );
        Log.i(TAG, "jsonStrign: " + jsonString);
        RequestBody body = RequestBody.create(TYPE_JSON, jsonString);
        Request request = new Request.Builder()
                .url(PATH_PERSON)
                .post(body)
                .build();
        sClient.newCall(request).enqueue(callback);
    }

    public void login(String name, String encPassword, Callback callback) {
        RequestBody form = new FormEncodingBuilder()
                .add("name", name)
                .add("password", encPassword)
                .build();

        Request request = new Request.Builder()
                .url(PATH_LOGIN)
                .post(form)
                .build();

        sClient.newCall(request).enqueue(callback);
    }

    public void updateThumbnail(int id, String newPath, Callback callback) {
        updateThumbnail(id, newPath, ACTION_THUMBNAIL_UPDATE, callback);
    }

    public void updateThumbnail(int id, String newPath, int action_type, Callback callback) {
        File file = null;
        if (newPath != null) {
            file = new File(newPath);
        }
        MultipartBuilder builder = new MultipartBuilder()
                .type(MultipartBuilder.FORM)
                .addPart(Headers.of("Content-Disposition", "form-data; name=\"id\""), RequestBody.create(null, "" + id))
                .addPart(Headers.of("Content-Disposition", "form-data; name=\"action_type\""), RequestBody.create(null, "" + action_type));
        if (file != null) {
            RequestBody fileBody = RequestBody.create(TYPE_JPG, file);
            builder.addPart(Headers.of("Content-Disposition", "form-data; name=\"filename\"; filename=\"" + newPath + "\""), fileBody);
        }

        RequestBody body = builder.build();
        Request request = new Request.Builder()
                .url(PATH_THUMBNAIL)
                .post(body)
                .build();
        sClient.newCall(request).enqueue(callback);

    }

    public void deleteThumbnail(int id, Callback callback) {
        updateThumbnail(id, null, ACTION_THUMBNAIL_DELETE, callback);
    }

    public String toJson(String[] attrs, Object[] values) {
        if (attrs.length != values.length) {
            Log.i(TAG, "attrs.length != values.length");
            return null;
        }

        StringBuffer sb = new StringBuffer();
        sb.append('{');
        for (int i = 0; i < attrs.length; i++) {
            if (values[i] instanceof String) {
                String val = (String) values[i];
                sb.append(String.format("\"%s\":\"%s\"", attrs[i], val));
            }
            else if (values[i] instanceof Integer) {
                sb.append(String.format("\"%s\": %d", attrs[i], values[i]));
            } else if (values[i] instanceof Long) {
                sb.append(String.format("\"%s\": %d", attrs[i], values[i]));
            } else if ((values[i] instanceof Double) || values[i] instanceof Float) {
                sb.append(String.format("\"%s\":%f", attrs[i], values[i]));
            } else if ((values[i] instanceof String[])) {
                String[] list = (String[]) values[i];
                sb.append(String.format("\"%s\":{"));
                for (int j = 0; j < list.length; j++) {
                    sb.append(list[i]);
                    if (i < list.length - 1) {
                        sb.append(',');
                    }
                }
                sb.append('}');
            }
            if (i < attrs.length - 1) {
                sb.append(',');
            }
        }
        sb.append("}");
        return sb.toString();
    }

    public void uploadLocation(int id, double lat, double lon, Callback callback) {
        String[] attrs = {"id", "location_latitude", "location_longtitude", "action_type"};
        Object[] values = {Integer.valueOf(id), Double.valueOf(degree2Pi(lat)),
                Double.valueOf(degree2Pi(lon)), Integer.valueOf(ACTION_UPDATE)};

        String jsonString = toJson(attrs, values);

        Log.i(TAG, "uploadLocation, jsonStrng: " + jsonString);
        RequestBody body = RequestBody.create(TYPE_JSON, jsonString);
        Request request = new Request.Builder()
                .url(PATH_PERSON)
                .post(body)
                .build();

        sClient.newCall(request).enqueue(callback);
    }

    public void updateInfo(int id, String attr, Object value, final Runnable runnable) {
        String jsonString = toJson(new String[]{"id", attr, ACTION_PERSON}, new Object[]{Integer.valueOf(id), value, Integer.valueOf(ACTION_PERSON_UPDATE)});

        Log.i(TAG, "updateInfo, jsonString: " + jsonString);

        RequestBody body = RequestBody.create(TYPE_JSON, jsonString);
        Request request = new Request.Builder()
                .url(PATH_PERSON)
                .post(body)
                .build();
        sClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.e(TAG, "updateInfo, request failed, url: " + request.urlString());
            }

            @Override
            public void onResponse(Response response) throws IOException {
                String jsonString = response.body().string();
                Log.i(TAG, "updateInfo: response jsonString: " + jsonString);
                try {
                    JSONObject jsonObject = new JSONObject(jsonString);
                    int code = jsonObject.getInt("code");
                    if (code != 0) {
                        Log.e(TAG, "updateInfo failed: , code: " + code + ", msg: " + jsonObject.getString("msg"));
                    } else {
                        Log.i(TAG, "updateInfo succeeded");
                        if (runnable != null) {
                            runnable.run();
                        }
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "updateInfo json parse failed: " + e);
                }
            }
        });
    }

    public void updatePicture(int person_id, String url, String newFilePath, Callback callback) {
        updatePicture(person_id, url, newFilePath, ACTION_IMAGE_UPDATE, callback);
    }

    public void updatePicture(int person_id, String url, String newFilePath, int action_type, Callback callback) {
        File newFile = new File(newFilePath);
        RequestBody fileBody = RequestBody.create(TYPE_JPG, newFile);
        MultipartBuilder builder = new MultipartBuilder()
                .type(MultipartBuilder.FORM)
                .addPart(Headers.of("Content-Disposition", "form-data; name=\"person_id\""), RequestBody.create(null, "" + person_id))
                ;
        if (url != null) {
            builder.addPart(Headers.of("Content-Disposition", "form-data; name=\"url\""), RequestBody.create(null, url));
        }

        RequestBody body = builder.addPart(Headers.of("Content-Disposition", "form-data; name=\"action_type\""), RequestBody.create(null, "" + action_type))
                .addPart(Headers.of("Content-Disposition", "form-data; name=\"filename\"; filename=\"" + newFilePath + "\""), fileBody)
                .build();

        Request req = new Request.Builder()
                .url(PATH_IMAGE)
                .post(body)
                .build();
        sClient.newCall(req).enqueue(callback);
    }


        public void deletePicture(String url, Callback callback) {
        RequestBody reqBody = new FormEncodingBuilder()
                .add("url", url)
                .add("action_type", "" + ACTION_IMAGE_DELETE)
                .build();
        Request request = new Request.Builder()
                .url(PATH_IMAGE)
                .post(reqBody)
                .build();
        sClient.newCall(request).enqueue(callback);
    }
    public  void addPicture(int person_id, String newFilePath,  Callback callback) {
        updatePicture(person_id, null, newFilePath, ACTION_IMAGE_CREATE, callback);
    }

    public static double degree2Pi(double d) {
        return d * Math.PI / 180;
    }

    public interface OnPersonFetchFinished {
        void personFetchFinished(Person[] personsFetched);
    }
}
