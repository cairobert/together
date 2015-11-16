package com.example.robert.together;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by robert on 10/11/15.
 */
public class NewMealFragment extends Fragment {
    private Button mNewMealButton;
    private boolean mStarted;
    private RecyclerView mRecyclerView;
    private ImageDownloader<NewMealHolder> mImageDownloader;
    private NewMealAdapter mAdapter;

    private static final String TAG = "NewMealFragment";

    public static NewMealFragment newInstance() {
        return new NewMealFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(TAG, "NewMealFragment.onCreate()");

        mStarted = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View v = inflater.inflate(R.layout.fragment_new_meal, container, false);

        mNewMealButton = (Button) v.findViewById(R.id.new_meal_button);
        mStarted = false;
        mNewMealButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStarted = !mStarted;
                if (mStarted) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        new FetchItemTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, 1);
                    } else {
                        new FetchItemTask().execute(1);
                    }
                    Log.i(TAG, "setOnClickListener");
                }
                updateButton();
            }
        });
        updateButton();

        mAdapter = new NewMealAdapter(new ArrayList<Person>(0));
        mRecyclerView = (RecyclerView) v.findViewById(R.id.persons_on_meal_recycler_view);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        updateRecyclerView();

        return v;
    }

    @Override
    public void onStop() {
        super.onStop();
        mStarted = false;
    }

    @Override
    public void onStart() {
        super.onStart();
        mStarted = false;
    }

    private void updateButton() {
        if (!mStarted) {
            mNewMealButton.setBackground(getResources().getDrawable(R.drawable.ic_add_white_24dp));
        } else {
            mNewMealButton.setBackground(getResources().getDrawable(R.drawable.ic_clear_white_24dp));
        }
    }

    private void updateRecyclerView() {
        if (!mStarted && mAdapter.getPersons().size() == 0) {
            mRecyclerView.setVisibility(View.GONE);
        } else if (mStarted && mAdapter.getPersons().size() == 0 ) {
            // TODO: 10/20/15 提示没有匹配的人
            mRecyclerView.setVisibility(View.GONE);
        } else {
            mAdapter.notifyDataSetChanged();
            mRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private class FetchItemTask extends AsyncTask<Integer,Void,List<Person>> {

        @Override
        protected List<Person> doInBackground(Integer... params) {
            Log.i(TAG, "paras.length: " + params.length);
            if (params.length == 0) {
                return null;
            }

            Log.i(TAG, "FetchItemTask.doInBackground() called");

            int personId = params[0];
            List<Person> persons = null;
            try {
                persons = new ImagePicker().fetchPerson(personId);
                Log.i(TAG, "response: \n" + persons);
                return persons;
            } catch (IOException e) {
                Log.e(TAG, "Failed to fetch url: " + e);
            }
            return persons;
        }

        @Override
        protected void onPostExecute(List<Person> result) {
            mAdapter.setPersons(result);
            updateRecyclerView();
        }
    }

    private class NewMealHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView mProfileImageView;
        private TextView mNameTextView;
        private ImageView mGenderImageView;
        private TextView mDistanceTextView;
        private int mPersonId;

        public NewMealHolder(View itemView) {
            super(itemView);

            itemView.setOnClickListener(this);

            mProfileImageView = (ImageView) itemView.findViewById(R.id.profile_image_view);
            mNameTextView = (TextView) itemView.findViewById(R.id.name_text_view);
            mGenderImageView = (ImageView) itemView.findViewById(R.id.gender_image_view);
            mDistanceTextView = (TextView) itemView.findViewById(R.id.distance_text_view);
        }

        public void bindPerson(Person person) {
            mPersonId = person.getId();
            mNameTextView.setText(person.getName());

            if (person.getGender() == Person.Gender.MAN) {
                mGenderImageView.setImageResource(R.drawable.man);
                mProfileImageView.setImageResource(R.drawable.default_man);
            } else if (person.getGender() == Person.Gender.WOMAN){
                mGenderImageView.setImageResource(R.drawable.woman);
                mProfileImageView.setImageResource(R.drawable.default_woman);
            }

            mDistanceTextView.setText(ProfileFragment.getFormatDistance(getContext(), person.getDistance()));
        }

        public void bindProfileDrawable(Bitmap bitmap) {
            mProfileImageView.setImageBitmap(bitmap);
        }

        @Override
        public void onClick(View v) {
            Intent intent = ProfileActivity.newIntent(getContext(), mPersonId);
            Log.i(TAG, "onClick, start ProfileActivity");
            startActivity(intent);
        }
    }

    private class NewMealAdapter extends RecyclerView.Adapter<NewMealHolder> {

        List<Person> mPersons;

        public NewMealAdapter(List<Person> persons) {
            mPersons = persons;
        }

        @Override
        public NewMealHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            View v = inflater.inflate(R.layout.list_person_simple, parent, false);
            return new NewMealHolder(v);
        }

        @Override
        public void onBindViewHolder(final NewMealHolder holder, int position) {
            Person person = mPersons.get(position);
            holder.bindPerson(person);
            final String url = person.getProfileUrl();
//            mImageDownloader.queueImageDownload(holder, url);
            ImageCache.getInstance(getContext()).cacheImage(url, new Runnable() {
                @Override
                public void run() {
                    Bitmap bitmap = ImageCache.getInstance(getContext()).getCache(url);
                    if (bitmap == null) {
                        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.default_man);
                    }
                    holder.bindProfileDrawable(bitmap);
                }
            }, true);
        }

        @Override
        public int getItemCount() {
            return mPersons.size();
        }

        public void setPersons(List<Person> persons) {
            mPersons = persons;
        }

        public List<Person> getPersons() {
            return mPersons;
        }
    }
}

