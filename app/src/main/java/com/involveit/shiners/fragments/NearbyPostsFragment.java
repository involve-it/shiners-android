package com.involveit.shiners.fragments;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.involveit.shiners.logic.cache.CacheEntity;
import com.involveit.shiners.logic.cache.CachingHandler;
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
import com.involveit.shiners.services.SimpleLocationService;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import im.delight.android.ddp.MeteorSingleton;
import im.delight.android.ddp.ResultListener;

public class NearbyPostsFragment extends Fragment {
    private static final String TAG = "NearbyPostsFragment";
    TabLayout tabLayout;
    ListView listView;
    View view;

    boolean postsPending = true;
    boolean loading = false;

    ProgressDialog progressDialog;
    SwipeRefreshLayout layout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_posts, container, false);
        setHasOptionsMenu(true);
        tabLayout= (TabLayout) view.findViewById(R.id.tabLayout);
        listView= (ListView) view.findViewById(R.id.listView);
        listView.setEmptyView(view.findViewById(R.id.nearby_posts_empty_view));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PostsArrayAdapter adapter = (PostsArrayAdapter) listView.getAdapter();
                startActivity(new Intent(getActivity(), PostDetailsActivity.class).putExtra(PostDetailsActivity.EXTRA_POST, (Parcelable) adapter.getItem(position)));
            }
        });

        layout = (SwipeRefreshLayout)view.findViewById(R.id.fragment_posts_layout);
        layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getNearbyPostsTest();
            }
        });

        CacheEntity<ArrayList<Post>> cacheEntity = CachingHandler.getCacheObject(getActivity(), CachingHandler.KEY_NEARBY_POSTS);

        if (cacheEntity == null) {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage(getResources().getText(R.string.message_loading_posts));
            progressDialog.show();
            progressDialog.setCancelable(false);
        } else {
            createListView(cacheEntity.getObject());

            if (!cacheEntity.isStale()){
                postsPending = false;
            }
        }

        if (postsPending && MeteorSingleton.getInstance().isConnected() && LocationHandler.getLatestReportedLocation() != null){
            getNearbyPostsTest();
            postsPending = false;
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        this.meteorBroadcastReceiver.register(getActivity());
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(this.locationBroadcastReceiver, new IntentFilter(SimpleLocationService.BROADCAST_LOCATION_REPORTED));
    }

    @Override
    public void onPause() {
        super.onPause();
        this.meteorBroadcastReceiver.unregister(getActivity());
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(this.locationBroadcastReceiver);
    }

    public void getNearbyPostsTest(){
        if (!loading && MeteorSingleton.getInstance().isConnected() && LocationHandler.getLatestReportedLocation() != null) {
            loading = true;
            Location currentLocation = LocationHandler.getLatestReportedLocation();
            Map<String, Object> map = new HashMap<>();
            map.put("lat", currentLocation.getLatitude());
            map.put("lng", currentLocation.getLongitude());
            map.put("radius", 10000);
            map.put("take", 10);

            MeteorSingleton.getInstance().call(Constants.MethodNames.GET_NEARBY_POSTS, new Object[]{map}, new ResultListener() {
                @Override
                public void onSuccess(String result) {
                    Log.d(TAG, result);
                    GetPostsResponse res = JsonProvider.defaultGson.fromJson(result, GetPostsResponse.class);
                    if (res.success) {
                        CachingHandler.setObject(getActivity(), CachingHandler.KEY_NEARBY_POSTS, res.result);

                        createListView(res.result);
                    } else {
                        Toast.makeText(NearbyPostsFragment.this.getActivity(), R.string.message_internal_error, Toast.LENGTH_SHORT).show();
                    }
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                        progressDialog = null;
                    }
                    loading = false;
                    layout.setRefreshing(false);
                }

                @Override
                public void onError(String error, String reason, String details) {
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                        progressDialog = null;
                    }
                    Toast.makeText(NearbyPostsFragment.this.getActivity(), R.string.message_internal_error, Toast.LENGTH_SHORT).show();
                    loading = false;
                    layout.setRefreshing(false);
                }
            });
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.m_fragment_posts,menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    public void createListView(ArrayList<Post> posts){
        PostsArrayAdapter adapter = (PostsArrayAdapter) listView.getAdapter();
        if (adapter == null){
            adapter = new PostsArrayAdapter(getActivity(), R.layout.fragment_posts_adap, posts);
            adapter.setNotifyOnChange(true);
            listView.setAdapter(adapter);
        } else {
            Helper.mergeDataToArrayAdapter(posts, adapter, true);
        }
    }

    private void recalculateDistances(){
        listView.invalidateViews();
    }

    private BroadcastReceiver locationBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (SimpleLocationService.BROADCAST_LOCATION_REPORTED.equals(action)){
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

    private static class PostsArrayAdapter extends ArrayAdapter<Post>{
        private PostsArrayAdapter(Context context, int resource, List<Post> objects) {
            super(context, resource, objects);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                LayoutInflater li = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = li.inflate(R.layout.fragment_posts_adap, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.titleView= (TextView) convertView.findViewById(R.id.textView2);
                viewHolder.descView= (TextView) convertView.findViewById(R.id.textView3);
                viewHolder.dateView= (TextView) convertView.findViewById(R.id.textView);
                viewHolder.distanceView= (TextView) convertView.findViewById(R.id.textView4);
                viewHolder.icon= (ImageView) convertView.findViewById(R.id.icon1);
                viewHolder.imageView= (ImageView) convertView.findViewById(R.id.imageView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            Post post = getItem(position);

            viewHolder.distanceView.setText(R.string.message_na);
            Location currentLocation = LocationHandler.getLatestReportedLocation();
            if (currentLocation != null) {
                com.involveit.shiners.logic.objects.Location location = post.getLocation();
                if (location != null) {
                    float distance = location.distanceFrom(currentLocation.getLatitude(), currentLocation.getLongitude());

                    viewHolder.distanceView.setText(LocationHandler.distanceFormatted(getContext(), distance));
                }
            }

            viewHolder.titleView.setText(post.details.title);
            if (post.details.description != null) {
                viewHolder.descView.setText(Html.fromHtml(post.details.description));
            } else {
                viewHolder.descView.setText("");
            }

            if (post.isLive()){
                if (com.involveit.shiners.logic.objects.Location.LOCATION_TYPE_DYNAMIC.equals(post.getPostType())){
                    viewHolder.icon.setImageResource(R.drawable.postcell_dynamic_live3x);
                } else {
                    viewHolder.icon.setImageResource(R.drawable.posttype_static_live3x);
                }
            } else {
                if (com.involveit.shiners.logic.objects.Location.LOCATION_TYPE_DYNAMIC.equals(post.getPostType())){
                    viewHolder.icon.setImageResource(R.drawable.postcell_dynamic3x);
                } else {
                    viewHolder.icon.setImageResource(R.drawable.posttype_static3x);
                }
            }

            viewHolder.dateView.setText(Helper.formatDate(post.timestamp));

            Photo photo = post.getMainPhoto();
            if (photo != null){
                Picasso.with(getContext()).load(photo.original).into(viewHolder.imageView);
            } else {
                viewHolder.imageView.setImageDrawable(null);
            }

            return convertView;
        }

        private static class ViewHolder{
            private TextView titleView;
            private TextView descView;
            private TextView dateView;
            private TextView distanceView;
            private ImageView icon;
            private ImageView imageView;
        }
    }
}
