package com.involveit.shiners.fragments;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
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

import com.involveit.shiners.logic.Constants;
import com.involveit.shiners.logic.Helper;
import com.involveit.shiners.logic.JsonProvider;
import com.involveit.shiners.logic.LocationHandler;
import com.involveit.shiners.logic.MeteorBroadcastReceiver;
import com.involveit.shiners.logic.objects.response.GetPostsResponse;
import com.involveit.shiners.logic.objects.Photo;
import com.involveit.shiners.logic.objects.Post;
import com.involveit.shiners.activities.PostDetailsActivity;
import com.involveit.shiners.R;
import com.involveit.shiners.services.LocationService;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import im.delight.android.ddp.MeteorSingleton;
import im.delight.android.ddp.ResultListener;

public class NearbyPostsFragment extends Fragment {
    TabLayout tabLayout;
    ListView listView;
    View view;
    ArrayList<Post> posts;

    Boolean postsPending = true;
    ProgressDialog progressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        posts = new ArrayList<>();
        view = inflater.inflate(R.layout.fragment_posts, container, false);
        setHasOptionsMenu(true);
        tabLayout= (TabLayout) view.findViewById(R.id.tabLayout);
        listView= (ListView) view.findViewById(R.id.listView);

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(getResources().getText(R.string.message_loading_posts));
        progressDialog.show();
        progressDialog.setCancelable(false);

        if (MeteorSingleton.getInstance().isConnected() && LocationHandler.getLatestReportedLocation() != null){
            getNearbyPostsTest();
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        this.meteorBroadcastReceiver.register(getActivity());
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(this.locationBroadcastReceiver, new IntentFilter(LocationService.BROADCAST_LOCATION_REPORTED));
    }

    @Override
    public void onPause() {
        super.onPause();
        this.meteorBroadcastReceiver.unregister(getActivity());
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(this.locationBroadcastReceiver);
    }

    public void getNearbyPostsTest(){
        Location currentLocation = LocationHandler.getLatestReportedLocation();
        Map<String,Object> map = new HashMap<>();
        map.put("lat", currentLocation.getLatitude());
        map.put("lng", currentLocation.getLongitude());
        map.put("radius", 10000);
        map.put("take", 10);

        MeteorSingleton.getInstance().call(Constants.MethodNames.GET_NEARBY_POSTS, new Object[]{map}, new ResultListener() {
            @Override
            public void onSuccess(String result) {

                //Type typeToken = new TypeToken<ResponseBase<ArrayList<Post>>>(){}.getType();
                //ResponseBase<ArrayList<Post>> res = JsonProvider.defaultGson.fromJson(result, typeToken);
                GetPostsResponse res = JsonProvider.defaultGson.fromJson(result, GetPostsResponse.class);
                if (res.success) {
                    posts = res.result;

                    createListView();
                } else {
                    Toast.makeText(NearbyPostsFragment.this.getActivity(), R.string.message_internal_error, Toast.LENGTH_SHORT).show();
                }
                if (progressDialog != null) {
                    progressDialog.dismiss();
                    progressDialog = null;
                }
            }

            @Override
            public void onError(String error, String reason, String details) {
                if (progressDialog != null) {
                    progressDialog.dismiss();
                    progressDialog = null;
                }
                Toast.makeText(NearbyPostsFragment.this.getActivity(), R.string.message_internal_error, Toast.LENGTH_SHORT).show();
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
                if (convertView == null) {
                    convertView = getActivity().getLayoutInflater().inflate(R.layout.fragment_posts_adap, parent, false);
                }
                TextView titleView= (TextView) convertView.findViewById(R.id.textView2);
                TextView descView= (TextView) convertView.findViewById(R.id.textView3);
                TextView dateView= (TextView) convertView.findViewById(R.id.textView);
                TextView distanceView= (TextView) convertView.findViewById(R.id.textView4);
                ImageView icon1= (ImageView) convertView.findViewById(R.id.icon1);
                final ImageView imageView= (ImageView) convertView.findViewById(R.id.imageView);
                Post post = posts.get(position);

                distanceView.setText(R.string.message_na);
                Location currentLocation = LocationHandler.getLatestReportedLocation();
                if (currentLocation != null) {
                    com.involveit.shiners.logic.objects.Location location = post.getLocation();
                    if (location != null) {
                        float distance = location.distanceFrom(currentLocation.getLatitude(), currentLocation.getLongitude());

                        distanceView.setText(LocationHandler.distanceFormatted(getActivity(), distance));
                    }
                }

                titleView.setText(post.details.title);
                if (post.details.description != null) {
                    descView.setText(Html.fromHtml(post.details.description));
                } else {
                    descView.setText("");
                }

                if (post.isLive()){
                    if (com.involveit.shiners.logic.objects.Location.LOCATION_TYPE_DYNAMIC.equals(post.getPostType())){
                        icon1.setImageResource(R.drawable.postcell_dynamic_live3x);
                    } else {
                        icon1.setImageResource(R.drawable.posttype_static_live3x);
                    }
                } else {
                    if (com.involveit.shiners.logic.objects.Location.LOCATION_TYPE_DYNAMIC.equals(post.getPostType())){
                        icon1.setImageResource(R.drawable.postcell_dynamic3x);
                    } else {
                        icon1.setImageResource(R.drawable.posttype_static3x);
                    }
                }

                dateView.setText(Helper.formatDate(post.timestamp));

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
                startActivity(new Intent(getActivity(), PostDetailsActivity.class).putExtra(PostDetailsActivity.EXTRA_POST, posts.get(position)));
            }
        });
    }

    private void recalculateDistances(){
        listView.invalidateViews();
    }

    private BroadcastReceiver locationBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (LocationService.BROADCAST_LOCATION_REPORTED.equals(action)){
                if (postsPending && LocationHandler.getLatestReportedLocation() != null){
                    getNearbyPostsTest();
                    postsPending = false;
                } else {
                    recalculateDistances();
                }
            }
        }
    };

    private MeteorBroadcastReceiver meteorBroadcastReceiver = new MeteorBroadcastReceiver() {
        @Override
        public void connected() {
            if (postsPending && LocationHandler.getLatestReportedLocation() != null){
                getNearbyPostsTest();
                postsPending = false;
            }
        }

        @Override
        public void disconnected() {

        }
    };
}
