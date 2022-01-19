package com.android.yoganetwork.adapters;

import android.animation.Animator;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.ablanco.zoomy.ZoomListener;
import com.ablanco.zoomy.Zoomy;
import com.android.yoganetwork.AddPostActivity;
import com.android.yoganetwork.PostDetailActivity;
import com.android.yoganetwork.PostLikedByActivity;
import com.android.yoganetwork.R;
import com.android.yoganetwork.ThereProfileActivity;
import com.android.yoganetwork.models.ModelPost;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static android.view.View.GONE;

public class AdapterPosts extends RecyclerView.Adapter<AdapterPosts.MyHolder> {


    private final DatabaseReference likesRef; //for likes database node
    private final DatabaseReference postsRef; //reference of posts
    Context context;
    List<ModelPost> postList;
    String myUid;
    boolean mProcessLike=false;


    public AdapterPosts(Context context, List<ModelPost> postList) {
        this.context = context;
        this.postList = postList;
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        //inflate layout row_post.xml
        View view = LayoutInflater.from(context).inflate(R.layout.row_posts, viewGroup, false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder myHolder, int i) {
        //get data
        final String uid = postList.get(i).getUid();
        String uPseudonym = postList.get(i).getuPseudonym();
        String uPractic = postList.get(i).getuPractic();
        String uDp = postList.get(i).getuDp();
        final String pId = postList.get(i).getpId();
        String pTitle = postList.get(i).getpTitle();
        String pDescription = postList.get(i).getpDescr();
        String pImage = postList.get(i).getpImage();
        String pTimeStamp = postList.get(i).getpTime();
        String pLikes = postList.get(i).getpLikes(); //contains total number of likes for a post
        String pComments = postList.get(i).getpComments(); //contains total number of comments for a post
        //convert timestamp to dd/mm/yyyy HH:mm
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
        String pTime = DateFormat.format("dd/MM/yyyy HH:mm", calendar).toString();

        MyHolder holder1 = myHolder;
       // holder1.imageFull(postList.get(i).getpImage());
        MyHolder holder2 = myHolder;
        holder2.fullImage(postList.get(i).getpImage());
        //set data
        myHolder.uPseudonymTv.setText(uPseudonym);
        myHolder.uPracticTv.setText(uPractic);
        myHolder.pTimeTv.setText(pTime);
        myHolder.pTitleTv.setText(pTitle);
        myHolder.pDescriptionTv.setText(pDescription);
        myHolder.pLikesTv.setText(pLikes +" Likes"); //i.e. 100 likes
        myHolder.pCommentsTv.setText(pComments +context.getString(R.string.comments)); //i.e. 100 comments
        //set likes for each post
        setLikes(myHolder, pId);


      //  myHolder.pImageIv.setOnClickListener(new View.OnClickListener() {
     //       @Override
          //  public void onClick(View v) {

         //       if (v != null) {
        //            Zoomy.Builder builder = new Zoomy.Builder((Activity) context).target(v);
        //            builder.register();
       //         } else {
        //        System.out.println("SUKA");
       //     }}
     //    });

        //set user dp
        try {
            Picasso.get().load(uDp).placeholder(R.drawable.ic_default_img).into(myHolder.uPictureIv);
        }
        catch (Exception e) {
        }

        //set post image
        //if there is no image i.e. pImage.equals("noImage") then hide ImageView


        if(pImage.equals("noImage")) {
            //hide imageview
            myHolder.pImageIv.setVisibility(View.GONE);
            myHolder.zoomInBtn.setVisibility(View.GONE);

        }

        else {

            //show imageview
            Picasso.get().load(pImage).fit().centerCrop().into(myHolder.pImageIv);



        }



        //handle button clicks
        myHolder.moreBtn.setOnClickListener(v -> showMoreOptions(myHolder.moreBtn, uid, myUid, pId, pImage));
        myHolder.commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //start PostDetailActivity
                Intent intent = new Intent(context, PostDetailActivity.class);
                intent.putExtra("postId", pId); //will get detail of post using this id, its id of the post clicked
                context.startActivity(intent);
            }
        });
        myHolder.likeBtn.setOnClickListener(new View.OnClickListener() {
                                                @Override
                 public void onClick(View v) {
                     /*Get total number of likes for the post, whose like button clicked
                      * if currently signed in user has not liked before
                      * increase value by 1, otherwise decrease value by 1*/
                     int pLikes = Integer.parseInt(postList.get(i).getpLikes());
                     mProcessLike = true;
                     //get id of the post liked
                     String postIde = postList.get(i).getpId();
                     likesRef.addValueEventListener(new ValueEventListener() {
                         @Override
                         public void onDataChange(@NonNull DataSnapshot snapshot) {
                               if (mProcessLike) {
                                if (snapshot.child(postIde).hasChild(myUid)) {
                              //already liked, so remove like
                              postsRef.child(postIde).child("pLikes").setValue(""+(pLikes-1));
                              likesRef.child(postIde).child(myUid).removeValue();
                              mProcessLike = false;
     }
                                   else {
                                 //not liked, like it
                                 postsRef.child(postIde).child("pLikes").setValue(""+(pLikes+1));
                                 likesRef.child(postIde).child(myUid).setValue("Liked"); //set any value
                                 mProcessLike = false;

                                 addToHisNotifications(""+uid, ""+pId, context.getString(R.string.liked));
                        } } }

                         @Override
                         public void onCancelled(@NonNull DatabaseError error) {
                         }
    });

                                                }});
        myHolder.shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//              some contain text and some text with images, we will handle both
                BitmapDrawable bitmapDrawable = (BitmapDrawable)myHolder.pImageIv.getDrawable();
                if (bitmapDrawable == null) {
                    //post without image
                    shareTextOnly(pTitle, pDescription);
                }
                else {
                    //post with image

                    //convert image to bitmap
                    Bitmap bitmap = bitmapDrawable.getBitmap();
                    shareImageAndText(pTitle, pDescription, bitmap);

                }
        }
        });

        myHolder.profileLayout.setOnClickListener(v -> {
                    //click to go to ThereProfileActivity with uid, this uid is of clicked user
                    //which will be used to show user specifi data/posts
                    Intent intent = new Intent(context, ThereProfileActivity.class);
                    intent.putExtra("uid", uid);
                    context.startActivity(intent);
                });

        //click like count to start PostLikedByActivity, and pass the post id
        myHolder.pLikesTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PostLikedByActivity.class);
                intent.putExtra("postId", pId);
                context.startActivity(intent);
            }
        });




    }




    private void addToHisNotifications(String hisUid, String pId, String notification) {
        String timestamp = ""+System.currentTimeMillis();

        HashMap<Object, String> hashMap = new HashMap<>();
        hashMap.put("pId", pId);
        hashMap.put("timestamp", timestamp);
        hashMap.put("pUid", hisUid);
        hashMap.put("notification", notification);
        hashMap.put("sUid", myUid);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(hisUid).child("Notifications").child(timestamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //added successfully
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed
                    }
                });
    }


    private void shareTextOnly(String pTitle, String pDescription) {
        //concatenate title and description to share
        String shareBody = pTitle +"\n"+ pDescription;

        //share intent
        Intent sIntent = new Intent(Intent.ACTION_SEND);
        sIntent.setType("text/plain");
        sIntent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.enviadopor)); //in case you share via email app
        sIntent.putExtra(Intent.EXTRA_TEXT, shareBody); //text to share
        context.startActivity(Intent.createChooser(sIntent, context.getString(R.string.compartirvia))); //message to show in share dialog
    }

    private void shareImageAndText(String pTitle, String pDescription, Bitmap bitmap) {
        //concatenate title and description to share
        String shareBody = pTitle +"\n"+ pDescription;

        //first we will save this image in cache, get the saved image uri
        Uri uri = saveImageToShare(bitmap);
        //share intent
        Intent sIntent = new Intent(Intent.ACTION_SEND);
        sIntent.setType("text/plain");
        sIntent.putExtra(Intent.EXTRA_STREAM, uri);
        sIntent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.sujeto)); //in case you share via email app
        sIntent.putExtra(Intent.EXTRA_TEXT, shareBody); //text to share
        sIntent.setType("image/png"); //text to share
        context.startActivity(Intent.createChooser(sIntent, context.getString(R.string.compartirvia))); //message to show in share dialog

    }

    private Uri saveImageToShare(Bitmap bitmap) {
        File imageFolder = new File(context.getCacheDir(), "images");
        Uri uri = null;
        try {
            imageFolder.mkdir(); //create if not exists
            File file = new File(imageFolder, "shared_image.png");

            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
            stream.flush();
            stream.close();
            uri = FileProvider.getUriForFile(context, "com.android.yoganetwork.fileprovider", file);
        }
        catch (Exception e) {
            Toast.makeText(context, "Error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return uri;
    }


    //add a key named "pLikes" to each post and set its value to "0" manually in firebase

    private void setLikes(MyHolder holder, String postKey) {
        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(postKey).hasChild(myUid)) {
                    //user has liked this post
                    /*To indicate that the post is liked by this (SignedIn) user
                    * Change drawable left icon of like button
                    * Change text of like button from "like" to "liked"*/
                    holder.likeBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_liked, 0, 0, 0);
                    holder.likeBtn.setText("Liked");
                }
                else {
                    //user has noy liked this post
                    /*To indicate that the post is not liked by this (SignedIn) user
                     * Change drawable left icon of like button
                     * Change text of like button from "liked" to "like"*/
                    holder.likeBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_like_black, 0, 0, 0);
                    holder.likeBtn.setText("LIKE");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void showMoreOptions(ImageButton moreBtn, String uid, String myUid, String pId, String pImage) {
        //creating popup menu currently having option Delete, we will add more options later
        PopupMenu popupMenu = new PopupMenu(context, moreBtn, Gravity.END);

        //add items in menu
        if (uid.equals(myUid)) {
            popupMenu.getMenu().add(Menu.NONE, 0, 0, context.getString(R.string.eliminar));
            popupMenu.getMenu().add(Menu.NONE, 1, 0, context.getString(R.string.editar));

        }
        popupMenu.getMenu().add(Menu.NONE, 2, 0, context.getString(R.string.details));


        //item click listener
        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id==0) {
                //delete is clicked
                beginDelete(pId, pImage);
            }
            else   if (id==1) {
                //edit is clicked
                //start AddPostActivity with key "editPost" and the if of the post clicked
                Intent intent = new Intent(context, AddPostActivity.class);
                        intent.putExtra("key", "editPost");
                         intent.putExtra("editPostId", pId);
                         context.startActivity(intent);
            }
            else if (id==2) {
                //start PostDetailActivity
                Intent intent = new Intent(context, PostDetailActivity.class);
                intent.putExtra("postId", pId); //will get detail of post using this id, its id of the post clicked
                context.startActivity(intent);
            }

            return false;
        });
        //show menu
        popupMenu.show();
    }

    private void beginDelete(String pId, String pImage) {
        //post can be with or without image
        
        if (pImage.equals("noImage")) {

            deleteWithoutImage(pId);
        }
        else {
            //post is with image
            deleteWithImage(pId, pImage);
        }
    }

    private void deleteWithImage(String pId, String pImage) {
        //progress bar
        ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage(context.getString(R.string.eliminando));
        /*Steps
        * 1)Delete image using url
        * 2)Delete from database using post id*/
        StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
        picRef.delete()
                .addOnSuccessListener(aVoid -> {
                    //image deleted, now delete database

                    Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
                    fquery.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot ds: snapshot.getChildren()) {
                                ds.getRef().removeValue(); //remove values from firebase where pId matches
                            }
                            //deleted
                            Toast.makeText(context, context.getString(R.string.eliminado), Toast.LENGTH_SHORT).show();
                            pd.dismiss();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                })
                        .addOnFailureListener(e -> {
                            //failed, can't go further
                            pd.dismiss();
                            Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
        }

    private void deleteWithoutImage(String pId) {
        final ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage(context.getString(R.string.eliminando));

        Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
        fquery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()) {
                    ds.getRef().removeValue(); //remove values from firebase where pId matches
                }
                //deleted
                Toast.makeText(context, context.getString(R.string.eliminado), Toast.LENGTH_SHORT).show();
                pd.dismiss();


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    //view holder class
    class MyHolder extends RecyclerView.ViewHolder {
        //views from row_post.xml

        ImageView uPictureIv, pImageIv, imageView2, ic_arrow_left, ic_arrow_right;
        TextView uPseudonymTv, uPracticTv, pTimeTv, pTitleTv, pDescriptionTv, pLikesTv, pCommentsTv;
        ImageButton moreBtn, zoomInBtn, zoomOutBtn;
        Button likeBtn, commentBtn, shareBtn;
        LinearLayout profileLayout;



        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //init views
            uPictureIv = itemView.findViewById(R.id.uPictureIv);
            pImageIv = itemView.findViewById(R.id.pImageIv);

            ic_arrow_left = itemView.findViewById(R.id.ic_arrow_left);
            ic_arrow_right = itemView.findViewById(R.id.ic_arrow_right);
            imageView2 = itemView.findViewById(R.id.imageView2);
            uPseudonymTv = itemView.findViewById(R.id.uPseudonymTv);
            uPracticTv = itemView.findViewById(R.id.uPracticTv);
            pTimeTv = itemView.findViewById(R.id.pTimeTv);
            pTitleTv = itemView.findViewById(R.id.pTitleTv);
            pDescriptionTv = itemView.findViewById(R.id.pDescriptionTv);
            pLikesTv = itemView.findViewById(R.id.pLikesTv);
            pCommentsTv = itemView.findViewById(R.id.pCommentsTv);
            moreBtn = itemView.findViewById(R.id.moreBtn);
            likeBtn = itemView.findViewById(R.id.likeBtn);
            commentBtn = itemView.findViewById(R.id.commentBtn);
            shareBtn = itemView.findViewById(R.id.shareBtn);
            profileLayout = itemView.findViewById(R.id.profileLayout);
            zoomInBtn = itemView.findViewById(R.id.zoomInBtn);
            zoomOutBtn = itemView.findViewById(R.id.zoomOutBtn);


    }



        public void fullImage(String getpImage) {
            zoomInBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String pImage = getpImage;
                    if (pImage != null) {


                        pImageIv.setVisibility(View.GONE);
                        imageView2.setVisibility(View.VISIBLE);
                        Picasso.get().load(pImage).into(imageView2, new Callback() {
                            @Override
                            public void onSuccess() {
                                ivsizetofs();
                            }

                            @Override
                            public void onError(Exception e) {

                            }
                        });
                        Zoomy.Builder builder = new Zoomy.Builder((Activity) context)
                                .target(imageView2);
                        builder.register();
                        zoomInBtn.setVisibility(View.GONE);
                        zoomOutBtn.setVisibility(View.VISIBLE);
                    }
                    else {
                        System.out.println("Nada");
                    }
                }
            });
            zoomOutBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String pImage = getpImage;
                    if (pImage != null) {


                        pImageIv.setVisibility(View.VISIBLE);
                        imageView2.setVisibility(View.GONE);
                        Picasso.get().load(pImage).into(imageView2);
                        zoomInBtn.setVisibility(View.VISIBLE);
                        zoomOutBtn.setVisibility(View.GONE);
                    }
                    else {
                        System.out.println("Nada");
                    }
                }
            });
        }


        private void ivsizetofs() {
            ViewTreeObserver vto = imageView2.getViewTreeObserver();
            vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                public boolean onPreDraw() {
                    imageView2.getViewTreeObserver().removeOnPreDrawListener(this);
                    int finalHeight = imageView2.getMeasuredHeight()/2;
                    int finalWidth = imageView2.getMeasuredWidth()/2;
                    ic_arrow_left.getLayoutParams().width = imageView2.getMeasuredWidth();
                    ic_arrow_left.getLayoutParams().height = imageView2.getMeasuredHeight();
                    ic_arrow_right.getLayoutParams().width = imageView2.getMeasuredWidth();
                    ic_arrow_right.getLayoutParams().height = imageView2.getMeasuredHeight();

                    fullScreenAnimation(finalWidth, finalHeight);
                    return true;
                }
            });
        }

        private void fullScreenAnimation(int imageWidth, int imageHeight) {
                            ic_arrow_right.setVisibility(View.VISIBLE);
                            ViewPropertyAnimator animator2 = ic_arrow_right.animate()
                                    .translationX(imageWidth)
                                    .translationY(imageHeight)
                                    .setInterpolator(new AccelerateInterpolator())
                                    .setInterpolator(new BounceInterpolator())
                                    .setDuration(1000);
                            animator2
                                    .setListener(new Animator.AnimatorListener() {
                                        @Override
                                        public void onAnimationStart(Animator animator) {

                                        }

                                        @Override
                                        public void onAnimationEnd(Animator animator) {
                                            ic_arrow_right.setVisibility(View.GONE);
                                            ic_arrow_right.animate()
                                                    .translationX(0)
                                                    .translationY(0)
                                                    .setDuration(0)
                                                    .start();
                                        }

                                        @Override
                                        public void onAnimationCancel(Animator animator) {

                                        }

                                        @Override
                                        public void onAnimationRepeat(Animator animator) {

                                        }
                                    })
                                    .start();
            ic_arrow_left.setVisibility(View.VISIBLE);
            ViewPropertyAnimator animator1 = ic_arrow_left.animate()
                    .translationX(-imageWidth)
                    .translationY(-imageHeight)
                    .setInterpolator(new AccelerateInterpolator())
                    .setInterpolator(new BounceInterpolator())
                    .setDuration(1000);
            animator1
                    .setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animator) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animator) {
                            ic_arrow_left.setVisibility(View.GONE);
                            ic_arrow_left.animate()
                                    .translationX(0)
                                    .translationY(0)
                                    .setDuration(0)
                                    .start();
                        }

                        @Override
                        public void onAnimationCancel(Animator animator) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animator) {

                        }
                    })
                    .start();
                        }
        }

    }
