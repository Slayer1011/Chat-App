package com.application.chat;

import android.content.Context;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.DialogCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.application.chat.CacheDb.DatabaseChatListHelper;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserHolder> {
    List<User> userInfoList;
    List<User> newFilterList;
    private boolean isChat;
    Context context;
    public UserAdapter(List<User> userList, Context context,boolean isChat) {
        this.userInfoList = userList;
        this.newFilterList=new ArrayList<>(userList);
        this.context=context;
        this.isChat=isChat;
    }
    @NonNull
    @Override
    public UserHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.row_list,parent,false);
        return new UserHolder(view);
    }
    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onBindViewHolder(@NonNull UserHolder holder, int position) {
        User user = userInfoList.get(position);
        if(user.getName()!=null && user.getImage()!=null && user.getToken()!=null && user.getId()!=null) {
            holder.name.setText(user.getName());
            //int visibility=user.isOnline()?View.VISIBLE:View.GONE;
            //holder.onlinestatus.setVisibility(visibility);
            if(user.getImage()!=null) {
                RequestOptions options=new RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.ALL);
                Glide.with(context).load(user.getImage())
                        .apply(options)
                        .placeholder(R.drawable.default_pic)
                        .centerCrop()
                        .error(R.drawable.default_pic)
                        .into(holder.profile);
            }
            holder.itemView.setOnClickListener(c -> {
                Intent i = new Intent(context, ConversationActivity.class);
                i.putExtra("name", user.getName());
                i.putExtra("image", user.getImage());
                i.putExtra("token", user.getToken());
                i.putExtra("uid", user.getId());
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
            });
        }
        holder.profile.setOnClickListener(v -> viewImage(user.getImage()));

        holder.itemView.setOnLongClickListener(v -> {
            buildPopup(v, user, position);
            return true;
        });
    }
    public int getPositionById(String target){
        for(int i=0;i<userInfoList.size();i++){
            if(userInfoList.get(i).getId().equals(target)){
                return i;
            }
        }
        return -1;
    }
    public void viewImage(String uri){
        View dialogView=LayoutInflater.from(context).inflate(R.layout.image_preview,null);
        ImageView imageView=dialogView.findViewById(R.id.profile_image);
        Glide.with(context).load(Uri.parse(uri))
                .placeholder(R.drawable.default_pic)
                .error(R.drawable.default_pic)
                .into(imageView);
        MaterialAlertDialogBuilder builder=new MaterialAlertDialogBuilder(context,R.style.MaterialAlertDialog);
        builder.setView(dialogView);
        AlertDialog dialog=builder.create();
        /*Window window=dialog.getWindow();
        if(window!=null){
            WindowManager.LayoutParams layoutParams=new WindowManager.LayoutParams();
            layoutParams.copyFrom(window.getAttributes());
            layoutParams.width=WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.height=WindowManager.LayoutParams.MATCH_PARENT;
            window.setAttributes(layoutParams);
        }*/
        dialog.show();
        dialogView.findViewById(R.id.backBtn).setOnClickListener(v ->dialog.dismiss());
    }
    public void buildPopup(View v, User info, int pos){
        PopupMenu popupMenu= null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            popupMenu = new PopupMenu(context,v);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            popupMenu.getMenuInflater().inflate(R.menu.longpress_menu,popupMenu.getMenu());
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            popupMenu.setOnMenuItemClickListener(menuItem ->{
                int menuId=menuItem.getItemId();
                if(menuId==R.id.fav) {
                    Toast.makeText(context, "Marked", Toast.LENGTH_SHORT).show();
                } else if (menuId==R.id.delete) {
                    DatabaseChatListHelper.getInstance(context)
                            .delete(info.getId());
                    userInfoList.remove(pos);
                    notifyItemRemoved(pos);
                    notifyItemRangeChanged(pos,userInfoList.size());
                }
                else {
                    Log.e("PopupItemFailer","Item not found");
                }
                return true;
            });
        }
        popupMenu.show();
    }
    @Override
    public int getItemCount() {
        return userInfoList.size();
    }
    public void updateList(List<User> newList){
        userInfoList=newList;
        notifyDataSetChanged();
    }

    public static class UserHolder extends RecyclerView.ViewHolder{
        TextView onlinestatus;
        TextView name;
        ImageView profile;
        public UserHolder(@NonNull View itemView) {
            super(itemView);
            onlinestatus=itemView.findViewById(R.id.status);
            name=itemView.findViewById(R.id.nameText);
            profile=itemView.findViewById(R.id.userProfile);
        }
    }
}
