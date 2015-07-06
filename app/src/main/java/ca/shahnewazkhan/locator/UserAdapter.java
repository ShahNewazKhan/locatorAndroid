package ca.shahnewazkhan.locator;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.login.widget.ProfilePictureView;

import java.util.List;

/**
 * Created by sparqy on 05/07/15.
 */
public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<UserCardInfo> userList;
    private Context context;

    public UserAdapter(List<UserCardInfo> userList){ this.userList = userList; }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    @Override
    public void onBindViewHolder(UserViewHolder userViewHolder, int i) {
        UserCardInfo uci = userList.get(i);
        userViewHolder.vName.setText(uci.name);
        userViewHolder.vDistance.setText(uci.distance);
        //userViewHolder.vProfilePic.setProfileId(uci.fb_id);
    }

    @Override
    public UserViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.users_cardview, viewGroup, false);
        context = viewGroup.getContext();

        return new UserViewHolder(itemView);
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {

        protected TextView vName;
        protected TextView vDistance;
        protected ProfilePictureView vProfilePic;

        public UserViewHolder(View v){
            super(v);

            vName = (TextView) v.findViewById(R.id.tv_userName);
            vDistance = (TextView) v.findViewById(R.id.tv_distance);
            //vProfilePic = (ProfilePictureView) v.findViewById(R.id.ppv_profilePic);
        }
    }
}
