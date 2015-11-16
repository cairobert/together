package com.example.robert.together;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by robert on 11/9/15.
 */
public class EditProfileFragment extends FragmentWithRoundRectView
        implements SimpleImageAdapter.OnItemClickListener, View.OnClickListener {
    private static final String TAG = "EditProfileFragment";
    private static final String TAG_EDIT_PROFILE = "tag_edit_profile";
    private static final String TAG_EDIT_BIRTHDAY = "tag_edit_birthday";
    private static final String TAG_OPTION_FRAGMENT = "tag_option_fragment";
    private static final String TAG_CHANGE_IMAGE = "tag_change_image";
    private static final String TAG_ADD_IMAGE = "tag_add_image";


    private static final int REQ_EDIT_TEXT = 0;
    private static final int REQ_OPTION_IMAGE_CHOSEN = 1;
    private static final int REQ_OPTION_IMAGE_CHANGE_OPTION = 2;
    private static final int REQ_OPTION_IMAGE_CHANGE_OPTION_GALLERY = 3;
    private static final int REQ_OPTION_IMAGE_ADD_OPTION = 4;
    private static final int REQ_OPTION_IMAGE_CHANGE_OPTION_CAMERA = 5;
    private static final int REQ_OPTION_IMAGE_ADD_OPTION_GALLERY = 6;
    private static final int REQ_OPTION_IMAGE_ADD_OPTION_CAMERA = 7;
    private static final int REQ_OPTION_PROFILE = 8;
    private static final int REQ_OPTION_PROFILE_CHANGE = 9;

    private static final int REQ_PROFILE_GALLERY = 10;
    private static final int REQ_PROFILE_CAMERA = 11;


    private static final int IMAGE_EDIT_ACTION_LOOK = 0;
    private static final int IMAGE_EDIT_ACTION_CHANGE = 1;
    private static final int IMAGE_EDIT_ACTION_DELETE = 2;
    private static final int IMAGE_EDIT_ACTION_CANCEL = 3;

    private static final int IMAGE_CHANGE_ACTION_GALLERY = 0;
    private static final int IMAGE_CHANGE_ACTION_CAMERA = 1;

    private static final int IMAGE_ADD_ACTION_GALLERY = 0;
    private static final int IMAGE_ADD_ACTION_CAMERA = 1;

    private static final int IMAGE_PROFILE_GALLERY = 0;
    private static final int IMAGE_PROFILE_CAMERA = 1;


    private Person mPerson;
    private ImageCache mImageCache;
    private View mRootView;
    private SimpleImageAdapter mImageAdapter;
    private Handler mUiHandler;
    private PersonPicker mPersonPicker;

    private int mPictureChosenId = -1;   // images in recycler view that is edited
    private File mTmpFile = null;

    private Map<Integer, String> mResIdToContent;
    private Map<Integer, String> mResIdToTitle;
    private Map<Integer, ViewGroup> mResIdToViewGroup;


    private ViewGroup mEditProfileImageViewGroup;
    private TextView mEditProfileImageTextView;
    private ImageView mEditProfileImageView;
    private String mThumbnail;

    private ViewGroup mNameViewGroup;
    private TextView mNameKeyTextView;
    private TextView mNameValueTextView;

    private ViewGroup mSignatureViewGroup;
    private TextView mSignatureKeyTextView;
    private TextView mSignatureValueTextView;

    private RecyclerView mPicturesRecyclerView;

    private ViewGroup mBirthdayViewGroup;
    private TextView mBirthdayKeyTextView;
    private TextView mBirthdayValueTextView;

    private ViewGroup mWeightViewGroup;
    private TextView mWeightKeyTextView;
    private TextView mWeightValueTextView;

    private ViewGroup mHeightViewGroup;
    private TextView mHeightKeyTextView;
    private TextView mHeightValueTextView;

    private ViewGroup mHometownViewGroup;
    private TextView mHometownKeyTextView;
    private TextView mHometownValueTextView;

    private ViewGroup mProfessionViewGroup;
    private TextView mProfessionKeyTextView;
    private TextView mProfessionValueTextView;

    private ViewGroup mCompanyViewGroup;
    private TextView mCompanyKeyTextView;
    private TextView mCompanyValueTextView;


    public static Fragment newInstance() {
        return new EditProfileFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        mPerson = PersonPool.getInstance(getActivity()).getSelf();
        Log.i(TAG, "picturesSize: " + mPerson.getPictureUrls().size());
        mImageCache = ImageCache.getInstance(getActivity());
        mUiHandler = new Handler(Looper.getMainLooper());
        mPersonPicker = PersonPicker.getInstance(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_edit_settings, container, false);

        setupWidgets(mRootView);

//        mPersonPicker.fetchSelf(new Runnable() {
//            @Override
//            public void run() {
////                mUiHandler.post(new Runnable() {
////                    @Override
////                    public void run() {
////                        setupWidgets(mRootView);
////                    }
////                });
//            }
//        });

        return mRootView;
    }

    private void setupWidgets(View v) {
        mResIdToViewGroup = new HashMap<>();
        mResIdToTitle = new HashMap<>();
        mResIdToContent = new HashMap<>();

        mEditProfileImageViewGroup = (ViewGroup) v.findViewById(R.id.fragment_edit_settings_profile_image_layout);
        mEditProfileImageTextView = (TextView) mEditProfileImageViewGroup.findViewById(R.id.template_settings_list_item_key_text_view);
        mEditProfileImageTextView.setText("头像");
        mEditProfileImageView = (ImageView) mEditProfileImageViewGroup.findViewById(R.id.template_settings_list_item_value_image_view);
        setupProfileImageView();

        mNameViewGroup = (ViewGroup) v.findViewById(R.id.fragment_edit_settings_name_layout);
        mNameKeyTextView = (TextView) mNameViewGroup.findViewById(R.id.template_settings_list_item_key_text_view);
        mNameKeyTextView.setText(R.string.fragment_profile_item_key_name);
        mNameValueTextView = (TextView) mNameViewGroup.findViewById(R.id.template_settings_list_item_value_text_view);
        mNameValueTextView.setText(mPerson.getName());

        mSignatureViewGroup = (ViewGroup) v.findViewById(R.id.fragment_edit_settings_signature_layout);
        mSignatureKeyTextView = (TextView) mSignatureViewGroup.findViewById(R.id.template_settings_list_item_key_text_view);
        mSignatureKeyTextView.setText(R.string.fragment_profile_item_key_signature);
        mSignatureValueTextView = (TextView) mSignatureViewGroup.findViewById(R.id.template_settings_list_item_value_text_view);
        mSignatureValueTextView.setText(formatString(mPerson.getSignature()));


        mBirthdayViewGroup = (ViewGroup) v.findViewById(R.id.fragment_edit_settings_birthday_layout);
        mBirthdayKeyTextView = (TextView) mBirthdayViewGroup.findViewById(R.id.template_settings_list_item_key_text_view);
        mBirthdayKeyTextView.setText(R.string.fragment_profile_item_key_birthday);
        mBirthdayValueTextView = (TextView) mBirthdayViewGroup.findViewById(R.id.template_settings_list_item_value_text_view);
        mBirthdayValueTextView.setText(formatDate(mPerson.getBirthday()));

        mHeightViewGroup = (ViewGroup) v.findViewById(R.id.fragment_edit_settings_height_layout);
        mHeightKeyTextView = (TextView) mHeightViewGroup.findViewById(R.id.template_settings_list_item_key_text_view);
        mHeightKeyTextView.setText(R.string.fragment_profile_item_key_height);
        mHeightValueTextView = (TextView) mHeightViewGroup.findViewById(R.id.template_settings_list_item_value_text_view);
        mHeightValueTextView.setText(formatString(mPerson.getHeight()));

        mWeightViewGroup = (ViewGroup) v.findViewById(R.id.fragment_edit_settings_weight_layout);
        mWeightKeyTextView = (TextView) mWeightViewGroup.findViewById(R.id.template_settings_list_item_key_text_view);
        mWeightKeyTextView.setText(R.string.fragment_profile_item_key_weight);
        mWeightValueTextView = (TextView) mWeightViewGroup.findViewById(R.id.template_settings_list_item_value_text_view);
        mWeightValueTextView.setText(formatString(mPerson.getWeight()));


        mHometownViewGroup = (ViewGroup) v.findViewById(R.id.fragment_edit_settings_hometown_layout);
        mHometownKeyTextView = (TextView) mHometownViewGroup.findViewById(R.id.template_settings_list_item_key_text_view);
        mHometownKeyTextView.setText(R.string.fragment_profile_item_key_hometown);
        mHometownValueTextView = (TextView) mHometownViewGroup.findViewById(R.id.template_settings_list_item_value_text_view);
        mHometownValueTextView.setText(formatString(mPerson.getHometown()));

        mProfessionViewGroup = (ViewGroup) v.findViewById(R.id.fragment_edit_settings_profession_layout);
        mProfessionKeyTextView = (TextView) mProfessionViewGroup.findViewById(R.id.template_settings_list_item_key_text_view);
        mProfessionKeyTextView.setText(R.string.fragment_profile_item_key_profession);
        mProfessionValueTextView = (TextView) mProfessionViewGroup.findViewById(R.id.template_settings_list_item_value_text_view);
        mProfessionValueTextView.setText(formatString(mPerson.getProfession()));

        mCompanyViewGroup = (ViewGroup) v.findViewById(R.id.fragment_edit_settings_company_layout);
        mCompanyKeyTextView = (TextView) mCompanyViewGroup.findViewById(R.id.template_settings_list_item_key_text_view);
        mCompanyKeyTextView.setText(R.string.fragment_profile_item_key_company);
        mCompanyValueTextView = (TextView) mCompanyViewGroup.findViewById(R.id.template_settings_list_item_value_text_view);
        mCompanyValueTextView.setText(formatString(mPerson.getCompany()));

        ViewGroup[] groups = {
                mEditProfileImageViewGroup,
//                mNameViewGroup,
                mSignatureViewGroup,
                mBirthdayViewGroup,
                mHeightViewGroup,
                mWeightViewGroup,
                mHometownViewGroup,
                mProfessionViewGroup,
                mCompanyViewGroup,
        };

        for (ViewGroup group: groups) {
            group.setOnClickListener(this);
        }

        groups = new ViewGroup[] {
                mSignatureViewGroup,
                mHeightViewGroup,
                mWeightViewGroup,
                mHometownViewGroup,
                mProfessionViewGroup,
                mCompanyViewGroup,
        };

        TextView[] keyTextViews = {
                mSignatureKeyTextView,
                mHeightKeyTextView,
                mWeightKeyTextView,
                mHometownKeyTextView,
                mProfessionKeyTextView,
                mCompanyKeyTextView,
        };

        TextView[] valueTextViews = {
                mSignatureValueTextView,
                mHeightValueTextView,
                mWeightValueTextView,
                mHometownValueTextView,
                mProfessionValueTextView,
                mCompanyValueTextView,
        };

        for (int i = 0; i < groups.length; i++) {
            Integer id = groups[i].getId();
            mResIdToViewGroup.put(id, groups[i]);
            mResIdToTitle.put(id, keyTextViews[i].getText().toString());
            mResIdToContent.put(id, valueTextViews[i].getText().toString());
        }


        mPicturesRecyclerView = (RecyclerView) v.findViewById(R.id.fragment_edit_settings_images_recycler_view);
        mPicturesRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));

        setupImageAdapter();
    }


    private void setupImageAdapter() {
        mImageAdapter = new EditImageAdapter(mPerson.getPictureUrls());
        Log.i(TAG, "setupImageAdapter: picturesSize: " + mPerson.getPictureUrls().size());
        mPicturesRecyclerView.setAdapter(mImageAdapter);
    }

    private String formatDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        if (date == null) {
            date = new Date();
        }
        calendar.setTime(date);
        return String.format("%s/%s/%s", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
    }


    private void setupProfileImageView() {
        String url = mPerson.getProfileUrl();
        if (url == null) {
            return;
        }
        drawProfile(mEditProfileImageView, mPerson.getProfileUrl());
    }

    private String formatString(int value) {
        if (value < 0) {
            return getString(R.string.fragment_profile_default_empty_text);
        }
        return "" + value;
    }

    private String formatString(Object value) {
        if (value == null ) {
            return getString(R.string.fragment_profile_default_empty_text);
        } else if (value instanceof String) {
            String s = (String) value;
            if (s.trim().isEmpty()) {
                return getString(R.string.fragment_profile_default_empty_text);
            } else {
                return s;
            }
        }
        return value.toString();
    }

    @Override
    public void itemClicked(SimpleImageAdapter.ImageHolder holder, int position) {
        if (position < mImageAdapter.getItemCount() - 1) {     // edit image
            mPictureChosenId = position;
            showEditImageOptionFragment(REQ_OPTION_IMAGE_CHOSEN);
        } else if (position == mImageAdapter.getItemCount() - 1) {   // add image
            mPictureChosenId = -1;
            addImage();
//            showEditImageOptionFragment(REQ_OPTION_IMAGE_ADD_OPTION);
        }
    }

    private void addImage() {
        ArrayList<String> options = new ArrayList<>(2);
        options.add(getString(R.string.image_option_from_galary));
        options.add(getString(R.string.image_option_from_camera));

        DialogFragment fragment = OptionDialogFragment.newInstance(options);
        fragment.setTargetFragment(this, REQ_OPTION_IMAGE_ADD_OPTION);
        fragment.show(getFragmentManager(), TAG_ADD_IMAGE);
    }

    private void showEditImageOptionFragment(int reqCode) {
        String[] items = getResources().getStringArray(R.array.image_edit_options);
        ArrayList<String> list = new ArrayList<>(Arrays.asList(items));
        DialogFragment fragment = OptionDialogFragment.newInstance(list);
        fragment.setTargetFragment(this, reqCode);
        fragment.show(getFragmentManager(), TAG_OPTION_FRAGMENT);
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        Log.i(TAG, "view.getId(): " + id);
        ViewGroup group = mResIdToViewGroup.get(v.getId());
        if (group != null) {
            String title = mResIdToTitle.get(id);
            String content = mResIdToContent.get(id);

            DialogFragment dialogFragment = EditTextDialogFragment.newInstance(title, content);
            dialogFragment.setTargetFragment(this, id); // send the view's id as requestcode
            dialogFragment.show(getFragmentManager(), TAG_EDIT_PROFILE);
        } else if (id == mEditProfileImageViewGroup.getId()) {
            showEditProfileImage();
        } else if (id == mBirthdayViewGroup.getId()) {
            DialogFragment dialogFragment = DatePickerFragment.newInstance(mPerson.getBirthday());
            dialogFragment.setTargetFragment(this, id);
            dialogFragment.show(getFragmentManager(), TAG_EDIT_BIRTHDAY);
        }
    }

    private void showEditProfileImage() {
        showEditImageOptionFragment(REQ_OPTION_PROFILE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "onActivityResult");
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        String attr = null;
        Object value = null;

        if (requestCode == mSignatureViewGroup.getId()) {
            String content = data.getStringExtra(EditTextDialogFragment.EXTRA_CONTENT);
            mPerson.setSignature(content);
            mSignatureValueTextView.setText(formatString(content));
            attr = PersonPicker.ATTR_SIGNATURE;
            value = content;
        } else if (requestCode == mHeightViewGroup.getId()) {
            String content = data.getStringExtra(EditTextDialogFragment.EXTRA_CONTENT);
            int height = parseInteger(content);
            mPerson.setHeight(height);
            mHeightValueTextView.setText(formatString(height));
            attr = PersonPicker.ATTR_HEIGHT;
            value = Integer.valueOf(height);
        } else if (requestCode == mWeightViewGroup.getId()) {
            String content = data.getStringExtra(EditTextDialogFragment.EXTRA_CONTENT);
            int weight = parseInteger(content);
            mPerson.setHeight(weight);
            mWeightKeyTextView.setText(formatString(weight));
            attr = PersonPicker.ATTR_WEIGHT;
            value = Integer.valueOf(weight);
        } else  if (requestCode == mHometownViewGroup.getId()) {
            String content = data.getStringExtra(EditTextDialogFragment.EXTRA_CONTENT);
            mPerson.setHometown(content);
            mHometownValueTextView.setText(formatString(content));
            attr = PersonPicker.ATTR_HOMETOWN;
            value = Integer.valueOf(content);
        } else if (requestCode == mProfessionViewGroup.getId()) {
            String content = data.getStringExtra(EditTextDialogFragment.EXTRA_CONTENT);
            mPerson.setProfession(content);
            mProfessionValueTextView.setText(formatString(content));
            attr = PersonPicker.ATTR_PROFESSION;
            value = Integer.valueOf(content);
        } else if (requestCode == mCompanyViewGroup.getId()) {
            String content = data.getStringExtra(EditTextDialogFragment.EXTRA_CONTENT);
            mPerson.setCompany(content);
            mCompanyValueTextView.setText(formatString(content));
            attr = PersonPicker.ATTR_COMPANY;
            value = Integer.valueOf(content);
        } else if (requestCode == mBirthdayViewGroup.getId()) {
            Date date = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mPerson.setBirthday(date);
            mBirthdayValueTextView.setText(formatDate(date));
            attr = "birthday";
            value = Long.valueOf(date.getTime());
        } else if (requestCode == REQ_OPTION_IMAGE_CHOSEN) {
            int which = data.getIntExtra(OptionDialogFragment.EXTRA_CHOSEN_ID, -1);
            if (isValidImageChange(which) == false) {
                return;
            }
            switch (which) {
                case IMAGE_EDIT_ACTION_LOOK:
                    showPicture(mImageAdapter.getPictures().get(mPictureChosenId));
                    break;
                case IMAGE_EDIT_ACTION_CHANGE:
                    showChangeImageOptionFragment(REQ_OPTION_IMAGE_CHANGE_OPTION);
                    break;
                case IMAGE_EDIT_ACTION_DELETE:
                    deletePicture(mPictureChosenId);
                    break;
                case IMAGE_EDIT_ACTION_CANCEL:
                    break;
                default:
                    Log.e(TAG, "onActivityResult, edit picture, unrecognized action id: " + which);
            }
            value = null;       // updating pictures will be different
        } else if (requestCode == REQ_OPTION_IMAGE_CHANGE_OPTION) {
            int which = data.getIntExtra(OptionDialogFragment.EXTRA_CHOSEN_ID, -1);
            if (isValidImageChange(which) == false) {
                return;
            }
            switch (which) {
                case IMAGE_CHANGE_ACTION_GALLERY:
                    selectPictureFromGallery(REQ_OPTION_IMAGE_CHANGE_OPTION_GALLERY);
                    break;
                case IMAGE_CHANGE_ACTION_CAMERA:
                    selectPictureFromCamera(REQ_OPTION_IMAGE_CHANGE_OPTION_CAMERA);
                    break;
                default:
                    Log.e(TAG, "onActivityResult, change picture, unrecognized action id: " + which);
            }
            value = null;
        } else if (requestCode == REQ_OPTION_IMAGE_CHANGE_OPTION_GALLERY) {
            Uri uri = data.getData();
            if (uri == null) {
                return;
            }
            Cursor cursor = getActivity().getContentResolver().query(uri, null, null, null, null);
            if (cursor == null) {
                return;
            }
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
            String imagePath = cursor.getString(columnIndex);
            String original = mPerson.getPictureUrls().get(mPictureChosenId);
            String newPath = changePicture(imagePath);
            updatePicture(original, newPath, mPictureChosenId);
        } else if (requestCode == REQ_OPTION_IMAGE_CHANGE_OPTION_CAMERA) {
            String orginalPath = mPerson.getPictureUrls().get(mPictureChosenId);
            String newPath = changePictureFromLocalPath(mTmpFile.getPath());
            updatePicture(orginalPath, newPath, mPictureChosenId);
        } else if (requestCode == REQ_OPTION_IMAGE_ADD_OPTION) {
            int which = data.getIntExtra(OptionDialogFragment.EXTRA_CHOSEN_ID, -1);
//            if (which < 0 || which > 1) {
                switch (which) {
                    case IMAGE_ADD_ACTION_GALLERY:
                        selectPictureFromGallery(REQ_OPTION_IMAGE_ADD_OPTION_GALLERY);
                        break;
                    case IMAGE_ADD_ACTION_CAMERA:
                        selectPictureFromCamera(REQ_OPTION_IMAGE_ADD_OPTION_CAMERA);
                        break;
                    default:
                        Log.e(TAG, "add image, unrecognized option: " + which);
                }
                return;
//            }
        } else if (requestCode == REQ_OPTION_IMAGE_ADD_OPTION_GALLERY) {
            Uri uri = data.getData();
            if (uri == null) {
                return;
            }
            Cursor cursor = getActivity().getContentResolver().query(uri, null, null, null, null);
            if (cursor == null) {
                return;
            }
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
            String imagePath = cursor.getString(columnIndex);
            addPicture(imagePath);
        } else if (requestCode == REQ_OPTION_IMAGE_ADD_OPTION_CAMERA) {
            addPicture(mTmpFile.getPath());
        } else if (requestCode == REQ_OPTION_PROFILE) {
            int which = data.getIntExtra(OptionDialogFragment.EXTRA_CHOSEN_ID, -1);
            switch (which) {
                case IMAGE_EDIT_ACTION_LOOK:
                    showPicture(mPerson.getProfileUrl());
                    break;
                case IMAGE_EDIT_ACTION_CHANGE:
                    showChangeImageOptionFragment(REQ_OPTION_PROFILE_CHANGE);
                    break;
                case IMAGE_EDIT_ACTION_DELETE:
                    deleteThumbnail();
                    break;
                case IMAGE_EDIT_ACTION_CANCEL:
                    break;
                default:
            }
        } else if (requestCode ==  REQ_OPTION_PROFILE_CHANGE) {
            int which = data.getIntExtra(OptionDialogFragment.EXTRA_CHOSEN_ID, -1);
            switch (which) {
                case IMAGE_PROFILE_GALLERY:
                    selectPictureFromGallery(REQ_PROFILE_GALLERY);
                    break;
                case IMAGE_PROFILE_CAMERA:
                    selectPictureFromCamera(REQ_PROFILE_CAMERA);
                    break;
                default:
                    Log.e(TAG, "onActivityResult, edit picture, unrecognized action id: " + which);
            }
        } else if (requestCode == REQ_PROFILE_GALLERY) {
            Uri uri = data.getData();
            if (uri == null) {
                return;
            }
            Cursor cursor = getActivity().getContentResolver().query(uri, null, null, null, null);
            if (cursor == null) {
                return;
            }
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
            String imagePath = cursor.getString(columnIndex);
            changeProfileFromLocalPath(imagePath);
//            String original = mPerson.getPictureUrls().get(mPictureChosenId);
//            String newPath = changePicture(imagePath);
//            updatePicture(original, newPath, mPictureChosenId);
        } else if (requestCode == REQ_PROFILE_CAMERA) {
            changeProfileFromLocalPath(mTmpFile.getPath());
        } else {
            Log.e(TAG, "unrecognized request code: " + requestCode);
        }

        if (attr != null && value != null) {
            mPersonPicker.updateInfo(mPerson.getId(), attr, value, null);
        }
    }

    private void changeProfileFromLocalPath(String path) {
        File f = new File(path);
        mImageCache.scaleDownImage(f);
        mPerson.setProfileUrl(f.getPath());
        mPersonPicker.updateThumbnail(mPerson.getId(), f.getPath(), new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.e(TAG, "changeProfileFromLocalPath() failed: " + e);
                Toaster.getInstance(getActivity()).showToast("更新头像失败");
            }

            @Override
            public void onResponse(Response response) throws IOException {
                String bodyString = response.body().string();
                try {
                    JSONObject jsonObject = new JSONObject(bodyString);
                    int code = jsonObject.getInt("code");
                    if (code != 0) {
                        Toaster.getInstance(getActivity()).showToast("更新头像失败");
                        Log.e(TAG, "changeProfileFromLocalPath: failed:, bodyString: " + bodyString);
                        Log.e(TAG, "changeProfileFromLocalPath: msg: " + jsonObject.getString("msg"));
                    } else {
                        final String url = jsonObject.getString("url");
                        mUiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mPerson.setProfileUrl(url);
                                setupProfileImageView();
                            }
                        });
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "changeProfileFromLocalPath: failed:, bodyString: " + bodyString);
                    Log.e(TAG, "changeProfileFromLocalPath, json exception: " + e);
                    e.printStackTrace();
                }
            }
        });
    }
    private void deleteThumbnail() {
        mPerson.setProfileUrl("");
        setupProfileImageView();
        mPersonPicker.deleteThumbnail(mPerson.getId(), new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.e(TAG, "deleteThumbnail failed: " + e);
            }

            @Override
            public void onResponse(Response response) throws IOException {
                String bodyString = response.body().string();
                try {
                    JSONObject jsonObject = new JSONObject(bodyString);
                    int code = jsonObject.getInt("code");
                    if (code != 0) {
                        Toaster.getInstance(getActivity()).showToast("删除头像失败");
                        Log.e(TAG, "deleteThumbnail failed, bodyString: " + bodyString);
                        Log.e(TAG, "deleteThumbnail failed, msg: " + jsonObject.getString("msg"));
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "deleteThumbnail, json exception: " + e);
                }
            }
        });
    }

    private void deletePicture(int pictureChosenId) {
        if (pictureChosenId != mPictureChosenId || pictureChosenId < 0 || pictureChosenId >= mImageAdapter.getItemCount() - 1) {
            return;
        }

        String url = mPerson.getPictureUrls().get(pictureChosenId);
        mPerson.getPictureUrls().remove(pictureChosenId);
        mImageAdapter.notifyDataSetChanged();
        mPersonPicker.deletePicture(url, new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.e(TAG, "deletePicture, onFailure: " + e);
            }

            @Override
            public void onResponse(Response response) throws IOException {
                String bodyString = response.body().string();
                Log.i(TAG, "deletePicture, onResponse: " + bodyString);
            }
        });
    }

    private boolean isValidImageChange(int which) {
        if (which != -1 && (mPictureChosenId != -1 && mPictureChosenId < mImageAdapter.getItemCount())) {
            return true;
        }
        return false;
    }

    private void selectPictureFromGallery(int reqCode) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, reqCode);
    }



    private void selectPictureFromCamera(int reqCode) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            mTmpFile = File.createTempFile("IMAGE", ".jpg", new File(mImageCache.getCacheImageDir()));
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mTmpFile));
            startActivityForResult(intent, reqCode );
        } catch (IOException e) {
            Log.e(TAG, "selectPictureFromCamera: " + e);
        }

    }

    private void showChangeImageOptionFragment(int reqCode) {
        ArrayList<String> options = new ArrayList<>(2);
        options.add(getString(R.string.image_option_from_galary));
        options.add(getString(R.string.image_option_from_camera));

        DialogFragment fragment = OptionDialogFragment.newInstance(options);
        fragment.setTargetFragment(this, reqCode);
        fragment.show(getFragmentManager(), TAG_CHANGE_IMAGE);
    }

    private String changePictureFromLocalPath(String path) {
        Log.i(TAG, "path returned from camera: " + path);
        Log.i(TAG, "file size: " + new File(path).length());
        return changePicture(path);
    }

    private String addPictureFromLocalPath(String path) {
        String newPath = mImageCache.cacheLocalFile(path);

        if (newPath != null) {
            File f = new File(newPath);
            mImageCache.scaleDownImage(f);
        } else {
            Toaster.getInstance(getActivity()).showToast("未能改变图片");
        }
        return newPath;
    }

    private void addPicture(String orgPath) {
        if (orgPath == null) {
            return;
        }

        final String path = mImageCache.cacheLocalFile(orgPath);
        if (path != null) {
            File f = new File(path);
            mImageCache.scaleDownImage(f);

            mPerson.getPictureUrls().add(path);
            setupImageAdapter();
            mImageAdapter.notifyDataSetChanged();

            PersonPicker.getInstance(getActivity()).addPicture(mPerson.getId(), path, new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    Toaster.getInstance(getActivity()).showToast("添加图片出错");
                    Log.e(TAG, "changePictureFromLocalPath, onFailure: " + e);
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    String bodyString = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(bodyString);
                        int code = jsonObject.getInt("code");
                        if (code != 0) {
                            Log.e(TAG, "changePictureFromLocalPath, onResponse, code: " + code + ", msg: " + jsonObject.getString("msg"));
                        } else {
                            int id = jsonObject.getInt("person_id");
                            if (id != mPerson.getId()) {
                                Log.e(TAG, "no same perons, mPeron.id: " + mPerson.getId() + ", returned id: " + id);
                            } else {
                                final String url = jsonObject.getString("url");
                                mUiHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mPerson.getPictureUrls().set(mImageAdapter.getPictures().size() - 1, url);
                                        mImageAdapter.notifyItemChanged(mImageAdapter.getPictures().size() - 1);
                                    }
                                });
                            }
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "changePictureFromLocalPath, onResponse, json error: " + e);
                    }
                }
            });
        }
    }

    // path is the path of selected file
    private String changePicture(String path) {
        String newPath = mImageCache.cacheLocalFile(path);

        if (newPath != null) {
            File f = new File(newPath);
            mImageCache.scaleDownImage(f);
            mPerson.getPictureUrls().set(mPictureChosenId, newPath);
            mImageAdapter.notifyItemChanged(mPictureChosenId);
        } else {
            Toaster.getInstance(getActivity()).showToast("未能改变图片");
        }
        return newPath;
    }



    private void updatePicture(String originalUrl, String newPath, final int chosenId) {
        PersonPicker.getInstance(getActivity()).updatePicture(mPerson.getId(), originalUrl, mImageCache.getCachePath(newPath), new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Toaster.getInstance(getActivity()).showToast("不能更新图片");
                Log.e(TAG, "change picture failed: " + e);
            }

            @Override
            public void onResponse(Response response) throws IOException {
                String bodyString = response.body().string();
                Log.i(TAG, "changePicture, bodyString: " + bodyString);
                try {
                    JSONObject jsonObject = new JSONObject(bodyString);
                    int code = jsonObject.getInt("code");
                    if (code != 0) {
                        Log.e(TAG, "change picture failed, code: " + code + ", msg: " + jsonObject.getString("msg"));
                    } else {
                        mPerson.getPictureUrls().set(chosenId, jsonObject.getString("url"));
                        mUiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mImageAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "changePicture: " + e);
                }
            }
        });
    }



    private void showPicture(String url) {
        Intent intent = ImageViewerActivity.newIntent(getActivity(), url);
        startActivity(intent);
    }


    private int parseInteger(String intStr) {
        int ret = -1;
        try {
            ret = Integer.parseInt(intStr);
        } catch (NumberFormatException e) {
            Toaster.getInstance(getActivity()).showToast("设置错误");
        }
        return ret;
    }


    private class EditImageAdapter extends SimpleImageAdapter {

        public EditImageAdapter(List<String> pictures) {
            super(getActivity(), pictures, EditProfileFragment.this);
        }

        @Override
        public int getItemCount() {
            return super.getItemCount() + 1;
        }

        @Override
        public void onBindViewHolder(ImageHolder holder, int position) {
            if (position < super.getItemCount()) {
                super.onBindViewHolder(holder, position);
            } else {

            }
        }

        @Override
        public ImageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == 0) {
                return super.onCreateViewHolder(parent, viewType);
            } else {
                View v = LayoutInflater.from(getActivity()).inflate(R.layout.image_view_add, parent, false);
                ImageAddHolder holder = new ImageAddHolder(v);
                return holder;
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position < super.getItemCount()) {
                return 0;     // super return zero
            } else {
                return 1;
            }
        }
    }

    private class ImageAddHolder extends SimpleImageAdapter.ImageHolder {

        public ImageAddHolder(View itemView) {
            super(itemView, EditProfileFragment.this);
        }
    }
}
