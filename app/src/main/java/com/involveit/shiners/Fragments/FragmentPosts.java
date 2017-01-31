package com.involveit.shiners.Fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.icu.text.DateFormat;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.involveit.shiners.App;
import com.involveit.shiners.Logic.JsonProvider;
import com.involveit.shiners.Objects.GetPostsResponse;
import com.involveit.shiners.Objects.Photo;
import com.involveit.shiners.Objects.Post;
import com.involveit.shiners.PostDetails;
import com.involveit.shiners.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import im.delight.android.ddp.ResultListener;

public class FragmentPosts extends Fragment {
    TabLayout tabLayout;
    ListView listView;
    View view;
    double testLat=37.890568,testLong=-122.205730;
    ArrayList<Post> posts;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        posts = new ArrayList<>();
        view = inflater.inflate(R.layout.fragment_posts, container, false);
        setHasOptionsMenu(true);
        tabLayout= (TabLayout) view.findViewById(R.id.tabLayout);
        listView= (ListView) view.findViewById(R.id.listView);
        while (true){
            if (App.meteor.isConnected()){
                getNearbyPostsTest();
                break;
            }
        }
        return view;
    }

    public void getNearbyPostsTest(){
        Map<String,Object> map = new HashMap<>();
        map.put("lat", testLat);
        map.put("lng", testLong);
        map.put("radius", 10000);
        map.put("take", 10);

        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Загрузка данных");
        progressDialog.show();
        progressDialog.setCancelable(false);

        App.meteor.call("getNearbyPostsTest", new Object[]{map}, new ResultListener() {
            @Override
            public void onSuccess(String result) {

                //Type typeToken = new TypeToken<ResultBase<ArrayList<Post>>>(){}.getType();
                //ResultBase<ArrayList<Post>> res = JsonProvider.defaultGson.fromJson(result, typeToken);
                GetPostsResponse res = JsonProvider.defaultGson.fromJson(result, GetPostsResponse.class);
                posts = res.result;

                createListView();
                progressDialog.dismiss();
            }

            @Override
            public void onError(String error, String reason, String details) {
                progressDialog.dismiss();
                Toast.makeText(FragmentPosts.this.getActivity(), "An error occurred", Toast.LENGTH_SHORT).show();
            }

        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.m_fragment_posts,menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    public void createListView(){
        listView.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return posts.size();
            }

            @Override
            public Object getItem(int position) {
                return null;
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                convertView=getActivity().getLayoutInflater(). inflate(R.layout.fragment_posts_adap,parent,false);
                TextView titleView= (TextView) convertView.findViewById(R.id.textView2);
                TextView descView= (TextView) convertView.findViewById(R.id.textView3);
                TextView dateView= (TextView) convertView.findViewById(R.id.textView);
                TextView distanceView= (TextView) convertView.findViewById(R.id.textView4);
                ImageView icon1= (ImageView) convertView.findViewById(R.id.icon1);
                ImageView icon2= (ImageView) convertView.findViewById(R.id.icon2);
                final ImageView imageView= (ImageView) convertView.findViewById(R.id.imageView);
                Post post = posts.get(position);

                com.involveit.shiners.Objects.Location location = post.getLocation();
                if (location != null){
                    float distance = location.distanceFrom(App.locationLat, App.locationLng);

                    if (distance<5280){
                        distanceView.setText(distance+" ft");
                    }else {
                        distanceView.setText(distance/5280+" mi");
                    }
                } else {
                    distanceView.setText("N/A");
                }

                titleView.setText(post.details.title);
                descView.setText(Html.fromHtml(post.details.description));

                if (post.isLive()){
                    if (com.involveit.shiners.Objects.Location.LOCATION_TYPE_DYNAMIC.equals(post.getPostType())){
                        icon1.setImageResource(R.drawable.postcell_dynamic_live3x);
                    } else {
                        icon1.setImageResource(R.drawable.posttype_static_live3x);
                    }
                } else {
                    if (com.involveit.shiners.Objects.Location.LOCATION_TYPE_DYNAMIC.equals(post.getPostType())){
                        icon2.setImageResource(R.drawable.postcell_dynamic3x);
                    } else {
                        icon2.setImageResource(R.drawable.posttype_static3x);
                    }
                }

                DateFormat dateFormat = DateFormat.getDateInstance();
                dateView.setText(dateFormat.format(post.timestamp));

                Photo photo = post.getMainPhoto();
                if (photo != null){
                    Picasso.with(getActivity()).load(photo.original).into(imageView);
                }

                return convertView;
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startActivity(new Intent(getActivity(), PostDetails.class).putExtra(PostDetails.EXTRA_POST, posts.get(position)));
            }
        });
    }
}
