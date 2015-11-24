package com.example.robert.together;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by robert on 11/3/15.
 */
public class NearbyPeopleFragment extends Fragment
    implements SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = "NearbyPeopleFramgent";
    private RecyclerView mRecyclerView;
    private NearbyAdapter mAdapter;
    private List<Person> mRecentPersons;
    private Handler mMainThreadHandler;
    private PersonPool mPersonPool;
    private LinearLayoutManager mLinearLayoutManager;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private int mLastVisiblePosition;

    public static Fragment newFragment() {
        return new NearbyPeopleFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mMainThreadHandler = new Handler(Looper.getMainLooper());
        mPersonPool = PersonPool.getInstance(getContext());
        mRecentPersons = mPersonPool.getPersons();
        mLastVisiblePosition = 0;
        uploadLocation();

        PersonPicker.getInstance(getActivity()).fetchSelf(null);
        fetchNearbyPersons(0, true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_recent, container, false);

        mSwipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.fragment_recent_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        mRecyclerView = (RecyclerView) v.findViewById(R.id.fragment_recent_recycler_recent_people);
        mLinearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        setupAdapter();
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                mLastVisiblePosition = mLinearLayoutManager.findLastVisibleItemPosition();
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_SETTLING) {
                    Log.i(TAG, "mLastVisiblePosition: " + mLastVisiblePosition + ", size: " + mPersonPool.getPersons().size());
                    if (mLastVisiblePosition >= mPersonPool.getPersons().size() - 1) {
                        fetchNearbyPersons();
                    }
                }
                Log.i(TAG, "onScrollChanged, state: + " + newState);
            }
        });
        return v;
    }


    @Override
    public void onRefresh() {
        refreshNearbyPersons();
    }

    private void launchMainActivity() {
        getActivity().finish();

        Intent intent = MainActivity.newIntent(getContext());
        startActivity(intent);
    }

    private void uploadLocation() {
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        double lat;
        double lon;
        if (locationManager == null) {
            lat = Double.longBitsToDouble(preferences.getLong(MainActivity.PREF_LOCATION_LAT, 0));
            lon = Double.longBitsToDouble(preferences.getLong(MainActivity.PREF_LOCATION_LON, 0));
            if (lat == 0 && lon == 0) {
                Toaster.getInstance(getContext()).showToast(Toaster.ERR_LOCATION_UNKOWN);
                return;
            }
        } else {
            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (location == null) {
                Toaster.getInstance(getContext()).showToast("没有网哦", Toast.LENGTH_SHORT);
                getActivity().finish();
            }
            lat = location.getLatitude();
            lon = location.getLongitude();
            preferences.edit()
                    .putLong(MainActivity.PREF_LOCATION_LAT, Double.doubleToRawLongBits(lat))
                    .putLong(MainActivity.PREF_LOCATION_LON, Double.doubleToRawLongBits(lon))
                    .commit();

            PersonPicker.getInstance(getContext()).uploadLocation(PersonPool.getInstance(getActivity()).getSelf().getId(), lat, lon, new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    Log.e(TAG, "uploadLocation failed, req: " + request);
                    Toaster.getInstance(getContext()).showToast("获取信息失败");
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    String bodyString = response.body().string();
                    Log.i(TAG, "uploadLocation, bodyString: " + bodyString);
                    try {
                        JSONObject jsonObject = new JSONObject(bodyString);
                        int code = jsonObject.getInt("code");
                        if (code != 0) {
                            Log.e(TAG, "response failed: " + jsonObject.getString("msg"));
                            Toaster.getInstance(getContext()).showToast(code);
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "uploadLocation failed: " + e);
                    }
                }
            });
        }
    }

    private void refreshNearbyPersons() {
        mSwipeRefreshLayout.setRefreshing(true);
        fetchNearbyPersons(0, true);
    }

    private void fetchNearbyPersons(int start, final boolean isRefresh) {
        Log.i(TAG, "fetNearbyPersons, start: " + start + ", isRefresh: " + isRefresh);
        if (isAdded()) {
            PersonPicker picker = PersonPicker.getInstance(getContext());
            int id = PreferenceManager.getDefaultSharedPreferences(getContext()).getInt(MainActivity.PREF_LOGIN_ID, -1);
            if (id == -1) {
                Toaster.getInstance(getContext()).showToast("没有登陆");
                launchMainActivity();
            }

            picker.fetchNearbyPersons(id, start, new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    Log.i(TAG, "failed to fetch nearby persons: " + request);
                    mMainThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mSwipeRefreshLayout.setRefreshing(false);
                            Toaster.getInstance(getContext()).showToast("获取信息失败");
                        }
                    });
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    String bodyString = response.body().string();
                    Log.i(TAG, "fetch nearby persons, response: " + bodyString);
                    try {
                        JSONObject jsonObject = new JSONObject(bodyString);
                        int code = jsonObject.getInt("code");
                        if (code != 0) {
                            Log.e(TAG, "err code " + code);
                            Toaster.getInstance(getContext()).showToast(code);
                            return;
                        }
                        JSONArray persons = jsonObject.getJSONArray("persons");
                        if (persons.length() == 0) {
                            mMainThreadHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toaster.getInstance(getContext()).showToast("这就是全部饭友了");
                                }
                            });
                            return;
                        }
                        boolean isDataChanged = false;
                        if (persons.length() != 0 && isRefresh) {
//                            mPersonPool.getPersons().clear();
                            isDataChanged = true;
                        }
                        for (int i = 0; i < persons.length(); i++) {
                            String personString = persons.getString(i);
                            Person p = new Person();
                            p.deserialize(personString);
                            mPersonPool.addPerson(p);
                            isDataChanged = true;
                        }
                        if (isDataChanged) {
                            mMainThreadHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    mAdapter.notifyDataSetChanged();
//                                    setupAdapter();
                                }
                            });
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "failed to create json object: " + e);
                    }
                    mMainThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mSwipeRefreshLayout.setRefreshing(false);
                        }
                    });
                }
            });
        }
    }
    private void fetchNearbyPersons() {
        fetchNearbyPersons(mPersonPool.getPersons().size(), false);
    }

    private void setupAdapter() {
        mAdapter = new NearbyAdapter(mPersonPool.getPersons());
        mRecyclerView.setAdapter(mAdapter);
    }



    private class NearbyViewHolder extends RecyclerView.ViewHolder
                implements View.OnClickListener {
        private int mPersonId;
        private ImageView mProfileImageView;
        private TextView mNameTextView;
        private ImageView mGenderImageView;
        private TextView mDistanceTextView;
        private TextView mSignatureTextView;
        private TextView mAgeTextView;

        public NearbyViewHolder(View itemView) {
            super(itemView);

            itemView.setOnClickListener(this);

            mProfileImageView = (ImageView) itemView.findViewById(R.id.profile_image_view);
            mNameTextView = (TextView) itemView.findViewById(R.id.name_text_view);
            mGenderImageView = (ImageView) itemView.findViewById(R.id.gender_image_view);
            mSignatureTextView = (TextView) itemView.findViewById(R.id.list_person_simple_signature_text_view);
            mDistanceTextView = (TextView) itemView.findViewById(R.id.distance_text_view);
            mAgeTextView = (TextView) itemView.findViewById(R.id.list_person_simple_age_text_view);
        }

        public void bindPerson(Person p) {
            mPersonId = p.getId();
            setupProfileImageView(p);
            mNameTextView.setText(p.getName());
            setGenderOnImageView(mGenderImageView, p.getGender());
            mDistanceTextView.setText(ProfileFragment.getFormatDistance(getContext(), p.getDistance()));
            mSignatureTextView.setText(p.getSignature());
            mAgeTextView.setText("" + p.getAge());

        }

        private void setupProfileImageView(final Person p) {
            Log.i(TAG, "setupProfileImageView, imageView: " + mProfileImageView + ", url: " + p.getProfileUrl());
            final ImageCache imageCache = ImageCache.getInstance(getContext());
            Bitmap bitmap = imageCache.getCache(p.getProfileUrl());
            if (bitmap == null) {
                imageCache.cacheImage(p.getProfileUrl(), new Runnable() {
                    @Override
                    public void run() {
                        Bitmap bmp = imageCache.getCache(p.getProfileUrl());
                        Log.i(TAG, "url: " + p.getProfileUrl());
                        if (bmp != null) {
                            Log.i(TAG, "fetupProfileImageView, cache succeeded");
                            mProfileImageView.setImageBitmap(bmp);
                        } else {
                            Log.i(TAG, "fetupProfileImageView, cache failed");
                            setGenderOnImageView(mProfileImageView, p.getGender());
                        }
                    }
                });
            } else {
                mProfileImageView.setImageBitmap(bitmap);
            }
        }

        private void setGenderOnImageView(ImageView imageView, Person.Gender gender) {
            Bitmap bmp;
            if (gender == Person.Gender.MAN) {
                bmp = BitmapFactory.decodeResource(getResources(), R.drawable.default_man);
            } else {
                bmp = BitmapFactory.decodeResource(getResources(), R.drawable.default_woman);
            }
            imageView.setImageBitmap(bmp);
        }

        @Override
        public void onClick(View v) {
            Intent intent = ProfileActivity.newIntent(getContext(), mPersonId);
            Log.i(TAG, "onClick, start ProfileActivity");
            startActivity(intent);
        }
    }

    private class NearbyAdapter extends RecyclerView.Adapter<NearbyViewHolder> {
        private List<Person> mPersons;

        public NearbyAdapter(List<Person> persons) {
            mPersons = persons;
        }

        @Override
        public NearbyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Log.i("NearbyAdapter", "onCreateViewHolder");
            LayoutInflater inflater = LayoutInflater.from(getContext());
            View v = inflater.inflate(R.layout.list_person_simple, parent, false);
            return new NearbyViewHolder(v);
        }

        @Override
        public void onBindViewHolder(NearbyViewHolder holder, int position) {
            Log.i("NearbyAdapter", "onBindViewHolder, postion: " + position);
            Log.i("NearbyAdapter", "profileImageView address: " + holder.mProfileImageView);
            holder.bindPerson(mPersons.get(position));
        }

        @Override
        public int getItemCount() {
            return mPersons.size();
        }
    }
}
