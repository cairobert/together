package com.example.robert.together;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends FragmentWithRoundRectView implements SimpleImageAdapter.OnItemClickListener {
    private static final String TAG = "ProfileFragment";
    private static final String ARG_PERSON_ID = "arg_person_id";

    private Person mPerson;
    private ImageView mProfileImageView;
    private TextView mNameTextView;
    private TextView mAgeTextView;
    private ImageView mGenderImageView;
    private TextView mDistanceTextView;
    private RecyclerView mPicturesRecyclerView;
    private TextView mSignatureTextView;
    private TextView mItemNameKeyTextView;
    private TextView mItemNameValueTextView;
    private TextView mItemAgeKeyTextView;
    private TextView mItemAgeValueTextView;
    private TextView mItemHeightKeyTextView;
    private TextView mItemHeightValueTextView;
    private TextView mItemWeightKeyTextView;
    private TextView mItemWeightValueTextView;
    private TextView mItemHometownKeyTextView;
    private TextView mItemHometownValueTextView;
    private TextView mItemProfessionKeyTextView;
    private TextView mItemProfessionValueTextView;
    private TextView mItemCompanyKeyTextView;
    private TextView mItemCompanyValueTextView;
    private ImageCache mImageCache;
    private RelativeLayout mProfileLinearLayout;
    private SimpleImageAdapter mAdapter;
    private Button mInviteButton;
    private View mRootView;
    private Handler mUiHandler;

    public static Fragment newInstance(int personId) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(ARG_PERSON_ID, personId);

        ProfileFragment fragment = new ProfileFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        mUiHandler = new Handler(Looper.getMainLooper());

//        setHasOptionsMenu(true);

        // TODO: 10/24/15 setup mPerson here with personId

        int personId = (int) getArguments().getInt(ARG_PERSON_ID);
        issuePersonInfo(personId);

        mImageCache = ImageCache.getInstance(getContext().getApplicationContext());
        // TODO: 10/22/15 query from sql or server
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_profile, container, false);

        setupWidgets(mRootView);

        return mRootView;
    }

    private void issuePersonInfo(int personId) {
        mPerson = PersonPool.getInstance(getContext()).getPerson(personId); // default

        PersonPicker.getInstance(getContext()).fetchPersonInfo(personId, new PersonPicker.OnPersonFetchFinished() {
            @Override
            public void personFetchFinished(Person[] personsFetched) {
                if (personsFetched.length == 1) {
                    mPerson = personsFetched[0];
                    mUiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            setupWidgets(mRootView);
                        }
                    });
                }
            }
        });
    }


    private void showToast(final String msg) {
        Toaster.getInstance(getContext()).showToast(msg);
    }

    private void setupWidgets(View v) {
        if (!isAdded()) {
            return;
        }
        mProfileImageView = (ImageView) v.findViewById(R.id.fragment_profile_image_view);
        setupProfileImageView();

        mProfileLinearLayout = (RelativeLayout) v.findViewById(R.id.fragment_profile_profile_relative_layout);
        setupProfileBackground();

        mGenderImageView = (ImageView) v.findViewById(R.id.fragment_profile_gender_image_view);
        setupGenderImageView();

        mPicturesRecyclerView = (RecyclerView) v.findViewById(R.id.fragment_profile_pictures_recycler_view);
        setupRecyclerView();

        mNameTextView = (TextView) v.findViewById(R.id.fragment_profile_name_text_view);
        mNameTextView.setText(mPerson.getName());

        mAgeTextView = (TextView) v.findViewById(R.id.fragment_profile_age_text_view);
        if (mPerson.getAge() <= 0) {
            mAgeTextView.setVisibility(View.GONE);
        } else {
            mAgeTextView.setText("" + mPerson.getAge());
        }

        mDistanceTextView = (TextView) v.findViewById(R.id.fragment_profile_distance_text_view);
        mDistanceTextView.setText(getFormatDistance(getContext(), mPerson.getDistance()));

        // item name
        View layout = (View) v.findViewById(R.id.fragment_profile_item_layout_name);
        mItemNameKeyTextView = (TextView) layout.findViewById(R.id.item_profile_data_name);
        mItemNameKeyTextView.setText(R.string.fragment_profile_item_key_name);

        mItemNameValueTextView = (TextView) layout.findViewById(R.id.item_profile_data_value);
        mItemNameValueTextView.setText(mPerson.getName());

        // item age
        layout = (View) v.findViewById(R.id.fragment_profile_item_layout_age);
        mItemAgeKeyTextView = (TextView) layout.findViewById(R.id.item_profile_data_name);
        mItemAgeKeyTextView.setText(R.string.fragment_profile_item_key_age);

        mItemAgeValueTextView = (TextView) layout.findViewById(R.id.item_profile_data_value);
        if (mPerson.getAge() > 0) {
            Log.i(TAG, "age: " + mPerson.getAge());
            mItemAgeValueTextView.setText("" + mPerson.getAge());
        } else {
            mItemAgeValueTextView.setText(R.string.fragment_profile_default_empty_text);
        }

        // item height
        layout = (View) v.findViewById(R.id.fragment_profile_item_layout_height);
        mItemHeightKeyTextView = (TextView) layout.findViewById(R.id.item_profile_data_name);
        mItemHeightKeyTextView.setText(R.string.fragment_profile_item_key_height);

        mItemHeightValueTextView = (TextView) layout.findViewById(R.id.item_profile_data_value);
        if (mPerson.getHeight() > 0) {
            mItemHeightValueTextView.setText(getString(R.string.fragment_profile_height_format, mPerson.getHeight()));
        } else {
            mItemHeightValueTextView.setText(R.string.fragment_profile_default_empty_text);
        }

        // item weight
        layout = (View) v.findViewById(R.id.fragment_profile_item_layout_weight);
        mItemWeightKeyTextView = (TextView) layout.findViewById(R.id.item_profile_data_name);
        mItemWeightKeyTextView.setText(R.string.fragment_profile_item_key_weight);

        mItemWeightValueTextView = (TextView) layout.findViewById(R.id.item_profile_data_value);
        if (mPerson.getWeight() > 0) {
            mItemWeightValueTextView.setText(getString(R.string.fragment_profile_weight_format, mPerson.getWeight()));
        } else {
            mItemWeightValueTextView.setText(R.string.fragment_profile_default_empty_text);
        }

        // signature
        mSignatureTextView = (TextView) v.findViewById(R.id.fragment_profile_signature_text_view);

        // item hometown
        layout = (View) v.findViewById(R.id.fragment_profile_item_layout_hometown);
        mItemHometownKeyTextView = (TextView) layout.findViewById(R.id.item_profile_data_name);
        mItemHometownKeyTextView.setText(R.string.fragment_profile_item_key_hometown);

        mItemHometownValueTextView = (TextView) layout.findViewById(R.id.item_profile_data_value);

        // item profession
        layout = (View) v.findViewById(R.id.fragment_profile_item_layout_profession);
        mItemProfessionKeyTextView = (TextView) layout.findViewById(R.id.item_profile_data_name);
        mItemProfessionKeyTextView.setText(R.string.fragment_profile_item_key_profession);

        mItemProfessionValueTextView = (TextView) layout.findViewById(R.id.item_profile_data_value);

        // item company
        layout = (View) v.findViewById(R.id.fragment_profile_item_layout_company);
        mItemCompanyKeyTextView = (TextView) layout.findViewById(R.id.item_profile_data_name);
        mItemCompanyKeyTextView.setText(R.string.fragment_profile_item_key_company);
        mItemCompanyValueTextView = (TextView) layout.findViewById(R.id.item_profile_data_value);

        // set the textview with text. it doesn't have units
        TextView[] textViewArray = new TextView[] {
                mSignatureTextView, mItemHometownValueTextView, mItemProfessionValueTextView, mItemCompanyValueTextView
        };
        String[] textArray = new String[] {
                mPerson.getSignature(), mPerson.getHometown(), mPerson.getProfession(), mPerson.getCompany()
        };
        for (int i = 0; i < textViewArray.length; i++) {
            if (textArray[i] != null) {
                textViewArray[i].setText(textArray[i]);
            } else {
                textViewArray[i].setText(getString(R.string.fragment_profile_default_empty_text));
            }
        }

        mInviteButton = (Button) v.findViewById(R.id.fragment_profile_button_invite);
        setupInviteButton();
    }

    private void setupProfileImageView() {
        mProfileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageClicked(getContext(), mPerson.getProfileUrl());
            }
        });

        String url = mPerson.getProfileUrl();
        if (url == null) {
            return;
        }
        drawProfile(mProfileImageView, url);
    }



    private void imageClicked(Context ctx, String url) {
        multipleImagesClicked(ctx, new String[]{url}, 0);
    }

    private void showImagesInAdapter(Context ctx, int curIdx) {
        multipleImagesClicked(ctx, mAdapter.getPictures().toArray(new String[]{}), curIdx);
    }

    private void multipleImagesClicked(Context ctx, String[] urls, int idx) {
        Intent intent = ImageViewerActivity.newIntent(ctx, urls, idx);
        startActivity(intent);
    }

    private void setupProfileBackground() {
        if (!isAdded()) {
            return;
        }
        Bitmap bitmap = mImageCache.getCache(mPerson.getProfileUrl());
        if (bitmap == null) {
            bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.man);
            mProfileLinearLayout.setBackground(new BitmapDrawable(getResources(), bitmap));
            mImageCache.cacheImage(mPerson.getProfileUrl(), new Runnable() {
                @Override
                public void run() {
                    setupProfileBackground();
                }
            });
        }

        bitmap = ImageDrawer.blur(getContext(), bitmap,
                                            ImageDrawer.BlurPart.QUATER);
        mProfileLinearLayout.setBackground(new BitmapDrawable(getResources(), bitmap));
    }

    private void setupGenderImageView() {
        if (mPerson.getGender() == Person.Gender.MAN) {
            mGenderImageView.setImageResource(R.drawable.man);
        } else {
            mGenderImageView.setImageResource(R.drawable.woman);
        }
    }

    private void setupRecyclerView() {
        List<String> pictures = mPerson.getPictureUrls();
        Log.i(TAG, "id: " + mPerson.getId() + ",  pictures.size(): " + mPerson.getPictureUrls().size());

        mAdapter = new SimpleImageAdapter(getActivity(), mPerson.getPictureUrls(), this);
        mPicturesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        mPicturesRecyclerView.setAdapter(mAdapter);

        if (pictures.size() == 0) {
            mPicturesRecyclerView.setVisibility(View.GONE);
        }
    }

    private void setupInviteButton() {
        setupInviteButtonText();

        mInviteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PersonPicker.getInstance(
                        getContext()).invitePerson(mPerson.getId(), mPerson.getInviteStatus(), "邀请msg1");
                mPerson.setInviteStatus(Person.Status.HAVE_INVITED);
                showToast("已发出邀请");
                setupInviteButtonText();
            }
        });
    }

    private void setupInviteButtonText() {
        if (mPerson.getInviteStatus() == Person.Status.HAVE_INVITED) {  // 邀请了
            mInviteButton.setText("等待接受");
        } else if (mPerson.getInviteStatus() == Person.Status.NOT_INVITED) {
            mInviteButton.setText("邀请");
        } else if (mPerson.getInviteStatus() == Person.Status.BEEN_INVITED) {
            mInviteButton.setText("接受");
        }
    }

    public static String getFormatDistance(Context ctx, int meters) {
        String dist;
        if (meters < 1000) {    // within 1km
            dist = ctx.getString(R.string.distance_format_string_meter_distance, (int)meters);
        } else {
            double fKm = meters / 1000.0;
            if (meters <= 20000) {
                dist = ctx.getString(R.string.distance_format_string_km_distance, fKm);
            } else {
                dist = ctx.getString(R.string.distance_format_string_km_long_distance, fKm);
            }
        }
        return dist;
    }

    @Override
    public void itemClicked(SimpleImageAdapter.ImageHolder holder, int position) {
        showImagesInAdapter(getActivity(), position);
    }


//    private  class ImageHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
//        ImageView mImageView;
//        String mUrl;
//
//        public ImageHolder(View itemView) {
//            super(itemView);
//
//            mImageView = (ImageView) itemView;
//            mImageView.setOnClickListener(this);
//        }
//
//        @Override
//        public void onClick(View v) {
//            Log.i("ImageHolder", "onClick");
//            showImagesInAdapter(getContext(), getAdapterPosition());
//        }
//
//        public void bindUrl(String url) {
//            mUrl = url;
//            final ImageCache imageCache = ImageCache.getInstance(getActivity());
//            Bitmap bitmap = imageCache.getCache(mUrl);
//            if (bitmap != null) {
//                mImageView.setImageBitmap(bitmap);
//            } else {
//                imageCache.cacheImage(mUrl, new Runnable() {
//                    @Override
//                    public void run() {
//                        Bitmap bmp = imageCache.getCache(mUrl);
//                        if (bmp != null) {
//                            mImageView.setImageBitmap(bmp);
//                        }
//                    }
//                });
//            }
//        }
//    }
//
//
//    private class ImageAdapter extends RecyclerView.Adapter<ImageHolder> {
//        List<String> mPictures;
//
//        public ImageAdapter(List<String> pictures) {
//            mPictures = pictures;
//        }
//
//        @Override
//        public ImageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//            ImageHolder holder;
//            View v = LayoutInflater.from(getActivity()).inflate(R.layout.list_item_image, parent, false);
//
//            holder = new ImageHolder(v);
//            return holder;
//        }
//
//        @Override
//        public void onBindViewHolder(ImageHolder holder, int position) {
//            String p = mPictures.get(position);
//            holder.bindUrl(p);
//        }
//
//        @Override
//        public int getItemCount() {
//            return mPictures.size();
//        }
//
//        @Override
//        public int getItemViewType(int position) {
//            return 0;
//        }
//
//        public void setPictures(List<String> pictures) {
//            mPictures = pictures;
//        }
//
//        public List<String> getPictures() {
//            return mPictures;
//        }
//    }
}
