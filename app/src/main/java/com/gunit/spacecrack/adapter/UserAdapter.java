package com.gunit.spacecrack.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.gunit.spacecrack.R;
import com.gunit.spacecrack.model.User;

import java.util.List;

/**
 * Created by Dimitri on 3/03/14.
 */

/**
 * Adapter for ListViews to display all the users
 */
public class UserAdapter extends BaseAdapter {

    private Context context;
    private List<User> userList;

    public UserAdapter (Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
    }

    @Override
    public int getCount() {
        return userList.size();
    }

    @Override
    public Object getItem(int position) {
        return userList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return userList.indexOf(getItem(position));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        User user = (User) getItem(position);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_user_item, null);
            viewHolder = new ViewHolder();
            viewHolder.imgProfilePicture = (ImageView) convertView.findViewById(R.id.img_listusers_profile);
            viewHolder.txtUsername = (TextView) convertView.findViewById(R.id.txt_listusers_username);
            viewHolder.txtEmail = (TextView) convertView.findViewById(R.id.txt_listusers_email);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (user.profile.image != null) {
            String image = user.profile.image.substring(user.profile.image.indexOf(",") + 1);
            byte[] decodedString = Base64.decode(image, 0);
            viewHolder.imgProfilePicture.setImageBitmap(BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length));
        }
        viewHolder.txtUsername.setText(user.username);
        viewHolder.txtEmail.setText(user.email);

        return convertView;
    }

    private class ViewHolder {
        ImageView imgProfilePicture;
        TextView txtUsername;
        TextView txtEmail;
    }
}
