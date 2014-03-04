package com.gunit.spacecrack.fragment;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.gunit.spacecrack.R;
import com.gunit.spacecrack.activity.HomeActivity;
import com.gunit.spacecrack.application.SpaceCrackApplication;
import com.gunit.spacecrack.model.Profile;
import com.gunit.spacecrack.restservice.RestService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;

/**
 * Created by Dimitri on 20/02/14.
 */
public class EditProfileFragment extends Fragment {
    private ImageView profilePicture;
    private EditText firstName;
    private EditText lastName;
    private Button date;
    private Button save;
    private Bitmap newPicture;

    private int galleryResult;
    private String picturePath;

    private Context context;
    private final String TAG = "Edit profile";
    private SharedPreferences sharedPreferences;

    //Datepicker
    Calendar calendar;
    int startYear;
    int startMonth;
    int startDay;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        context = getActivity();

        Profile profile = SpaceCrackApplication.user.profile;

        calendar = Calendar.getInstance();
        calendar.setTimeInMillis(profile.dayOfBirth);
        startYear = calendar.get(Calendar.YEAR);
        startMonth = calendar.get(Calendar.MONTH);
        startDay = calendar.get(Calendar.DAY_OF_MONTH);

        profilePicture = (ImageView) rootView.findViewById(R.id.img_edit_profile_profilepicture);
        if (SpaceCrackApplication.profilePicture != null) {
            profilePicture.setImageBitmap(SpaceCrackApplication.profilePicture);
        }
        profilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, galleryResult);
            }
        });
        firstName = (EditText) rootView.findViewById(R.id.edt_profile_first_name);
        firstName.setText(profile.firstname);
        lastName = (EditText) rootView.findViewById(R.id.edt_profile_last_name);
        lastName.setText(profile.lastname);
        date = (Button) rootView.findViewById(R.id.btn_profile_date);
        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment datePicker = new StartDatePicker();
                datePicker.show(getActivity().getFragmentManager(), "start_date_picker");
            }
        });
        date.setText(startDay + "-" + (startMonth + 1) + "-" + startYear);
        save = (Button) rootView.findViewById(R.id.btn_profile_save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!firstName.getText().toString().equals("") && !lastName.getText().toString().equals("") && !date.getText().toString().equals(getResources().getText(R.string.birth_date))) {
                    new EditTask(firstName.getText().toString(), lastName.getText().toString(), SpaceCrackApplication.user.profile.email, date.getText().toString(), newPicture).execute(SpaceCrackApplication.URL_PROFILE);
                } else {
                    Toast.makeText(getActivity(), getResources().getString(R.string.fill_in_fields), Toast.LENGTH_SHORT).show();
                }
            }
        });

        sharedPreferences = getActivity().getSharedPreferences("Login", 0);

        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == galleryResult && data != null) {

            Uri selectedImage = data.getData();
            String[] filePathImage = {MediaStore.Images.Media.DATA};
            Cursor cursor = getActivity().getContentResolver().query(selectedImage, filePathImage, null, null, null);
            if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex(filePathImage[0]);
                picturePath = cursor.getString(columnIndex);
                try {
//                    profilePicture.setImageBitmap(decodeUri(selectedImage));
                    newPicture = decodeSampledBitmapFromUri(selectedImage, 200, 200);
                    profilePicture.setImageBitmap(newPicture);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
            cursor.close();
        }

//        if (requestCode == galleryResult && data != null) {
//            Uri selectedImage = data.getData();
//            String[] filePathImage = {MediaStore.Images.Media.DATA};
//            Cursor cursor = getActivity().getContentResolver().query(selectedImage, filePathImage, null, null, null);
//
//            if (cursor.moveToFirst()) {
//                int columnIndex = cursor.getColumnIndex(filePathImage[0]);
//                picturePath = cursor.getString(columnIndex);
//                profilePicture.setImageBitmap(BitmapFactory.decodeFile(picturePath));
//            }
//
//            cursor.close();
//        }
    }

    public Bitmap decodeSampledBitmapFromUri (Uri selectedImage, int reqWidth, int reqHeight) throws FileNotFoundException {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(
                getActivity().getContentResolver().openInputStream(selectedImage), null, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeStream(
                getActivity().getContentResolver().openInputStream(selectedImage), null, options);
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    private Bitmap decodeUri(Uri selectedImage) throws FileNotFoundException {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(
                getActivity().getContentResolver().openInputStream(selectedImage), null, o);

        final int REQUIRED_SIZE = 100;

        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while (true) {
            if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE) {
                break;
            }
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        return BitmapFactory.decodeStream(
                getActivity().getContentResolver().openInputStream(selectedImage), null, o2);
    }

    private class StartDatePicker extends DialogFragment implements DatePickerDialog.OnDateSetListener{
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            DatePickerDialog dialog = new DatePickerDialog(context, this, startYear, startMonth, startDay);
            return dialog;
        }
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            startYear = year;
            startMonth = monthOfYear;
            startDay = dayOfMonth;
            date.setText(startDay + "-" + (startMonth + 1) + "-" + startYear);
        }
    }

    //POST request to edit the profile
    public class EditTask extends AsyncTask<String, Void, Boolean> {

        private JSONObject profile;

        public EditTask (String firstname, String lastname, String email, String dateOfBirth, Bitmap image)
        {
            super();

            String byte64Img = "";
            if (image != null) {
                //Convert image to Base64 String
//                File file = new File(image);
//                Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                image.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();
                byte64Img = Base64.encodeToString(byteArray, Base64.DEFAULT);

            } else {
                if (SpaceCrackApplication.user.profile.image != null) {
                    byte64Img = SpaceCrackApplication.user.profile.image.substring(SpaceCrackApplication.user.profile.image.indexOf(",") + 1);
                }
            }

            //Create an profile to edit
            profile = new JSONObject();
            try {
                profile.put("firstname", firstname);
                profile.put("lastname", lastname);
                profile.put("email", email);
                profile.put("dayOfBirth", dateOfBirth);
                profile.put("image", "data:image/png;base64," + byte64Img);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onPreExecute() {
            save.setEnabled(false);
        }

        @Override
        protected Boolean doInBackground (String...url)
        {
            return RestService.editProfile(profile);
        }

        @Override
        protected void onPostExecute (Boolean result)
        {
            Toast.makeText(getActivity(), result ? getResources().getString(R.string.profile_edited) : getResources().getString(R.string.edited_failed), Toast.LENGTH_SHORT).show();

            if (result) {
                new GetProfile().execute(SpaceCrackApplication.URL_PROFILE);
            }

            save.setEnabled(true);
        }
    }

    //POST request to edit the profile
    private class GetProfile extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground (String...url)
        {
            return RestService.getRequest(url[0]);
        }

        @Override
        protected void onPostExecute (String result)
        {
            if (result != null) {
                try {
                    Gson gson = new Gson();
                    SpaceCrackApplication.user.profile = gson.fromJson(result, Profile.class);

                    if (SpaceCrackApplication.user.profile.image != null) {
                        //Get the image from the Data URI
                        String image = SpaceCrackApplication.user.profile.image.substring(SpaceCrackApplication.user.profile.image.indexOf(",") + 1);
                        byte[] decodedString = Base64.decode(image, 0);
                        SpaceCrackApplication.profilePicture = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    }

                    //Return to the Profile
                    getActivity().getFragmentManager().beginTransaction()
                            .replace(R.id.container, new ProfileFragment(), "Profile")
                            .commit();
                } catch (JsonParseException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(getActivity(), getResources().getText(R.string.profile_fail), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
