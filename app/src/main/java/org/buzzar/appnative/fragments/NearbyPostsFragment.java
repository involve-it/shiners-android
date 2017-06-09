package org.buzzar.appnative.fragments;

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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.buzzar.appnative.activities.newpost.NewPostActivity;
import org.buzzar.appnative.logic.cache.CacheEntity;
import org.buzzar.appnative.logic.cache.CachingHandler;
import org.buzzar.appnative.logic.Constants;
import org.buzzar.appnative.logic.Helper;
import org.buzzar.appnative.logic.JsonProvider;
import org.buzzar.appnative.logic.LocationHandler;
import org.buzzar.appnative.logic.MeteorBroadcastReceiver;
import org.buzzar.appnative.logic.objects.response.GetPostsResponse;
import org.buzzar.appnative.logic.objects.Photo;
import org.buzzar.appnative.logic.objects.Post;
import org.buzzar.appnative.activities.PostDetailsActivity;
import org.buzzar.appnative.R;
import org.buzzar.appnative.services.SimpleLocationService;
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

    ArrayList<Post> posts = new ArrayList<>();
    boolean moreAvailable = true;

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

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (moreAvailable && totalItemCount - (firstVisibleItem + visibleItemCount) < 10){
                    getNearbyPosts(true);
                }
            }
        });

        layout = (SwipeRefreshLayout)view.findViewById(R.id.fragment_posts_layout);
        layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!loading && MeteorSingleton.getInstance().isConnected()) {
                    getNearbyPosts(false);
                } else if (!MeteorSingleton.getInstance().isConnected()){
                    postsPending = true;
                }
            }
        });

        CacheEntity<ArrayList<Post>> cacheEntity = CachingHandler.getCacheObject(getActivity(), CachingHandler.KEY_NEARBY_POSTS);

        if (cacheEntity == null) {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage(getResources().getText(R.string.message_loading_posts));
            progressDialog.show();
            progressDialog.setCancelable(false);
        } else {
            populateListView(cacheEntity.getObject(), false);

            if (!cacheEntity.isStale()){
                postsPending = false;
            }
        }

        if (postsPending && MeteorSingleton.getInstance().isConnected() && LocationHandler.getLatestReportedLocation() != null){
            getNearbyPosts(false);
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

    public static void longLog(String tag, String str) {
        if (str.length() > 4000) {
            Log.d(tag, str.substring(0, 4000));
            longLog(tag, str.substring(4000));
        } else
            Log.d(tag, str);
    }

    public void getNearbyPosts(final boolean loadMore){
        if (!loading && MeteorSingleton.getInstance().isConnected() && LocationHandler.getLatestReportedLocation() != null) {
            loading = true;
            final PostsArrayAdapter adapter = (PostsArrayAdapter) listView.getAdapter();
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
            Location currentLocation = LocationHandler.getLatestReportedLocation();
            Map<String, Object> map = new HashMap<>();
            map.put("lat", currentLocation.getLatitude());
            map.put("lng", currentLocation.getLongitude());
            map.put("radius", 10000);
            map.put("take", Constants.Defaults.DEFAULT_POSTS_PAGE);
            if (loadMore){
                map.put("skip", posts.size());
            }

            MeteorSingleton.getInstance().call(Constants.MethodNames.GET_NEARBY_POSTS, new Object[]{map}, new ResultListener() {
                @Override
                public void onSuccess(String result) {
                    //Log.d(TAG, result);
                    //longLog(TAG, result);
                    loading = false;
                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }

                    GetPostsResponse res = JsonProvider.defaultGson.fromJson(result, GetPostsResponse.class);
                    if (res.success) {
                        CachingHandler.setObject(getActivity(), CachingHandler.KEY_NEARBY_POSTS, res.result);
                        if (loadMore) {
                            posts.addAll(res.result);
                        } else {
                            posts = res.result;
                        }
                        populateListView(res.result, loadMore);
                    } else {
                        Toast.makeText(NearbyPostsFragment.this.getActivity(), R.string.message_internal_error, Toast.LENGTH_SHORT).show();
                    }
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                        progressDialog = null;
                    }

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
                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }
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

    private void populateListView(ArrayList<Post> posts, boolean append){
        PostsArrayAdapter adapter = (PostsArrayAdapter) listView.getAdapter();
        moreAvailable = posts.size() == Constants.Defaults.DEFAULT_POSTS_PAGE;
        if (adapter == null){
            Log.d(TAG, "New adapter");
            adapter = new PostsArrayAdapter(getActivity(), R.layout.fragment_posts_adap, new ArrayList<>(posts));
            adapter.setNotifyOnChange(true);
            listView.setAdapter(adapter);
        } else {
            if (append){
                Log.d(TAG, "Appending posts");
                adapter.addAll(new ArrayList<>(posts));
            } else {
                Log.d(TAG, "Replacing posts");
                Helper.mergeDataToArrayAdapter(posts, adapter, true);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.fragment_me_add:
                startActivity(new Intent(getActivity(), NewPostActivity.class));
                break;
        }

        return super.onOptionsItemSelected(item);
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
                    getNearbyPosts(false);
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
                getNearbyPosts(false);
                postsPending = false;
            }
        }

        @Override
        public void disconnected() {

        }
    };

    private class PostsArrayAdapter extends ArrayAdapter<Post>{
        private static final int VIEW_TYPE_POST = 0;
        private static final int VIEW_TYPE_LOADING = 1;
        private static final int VIEW_TYPE_COUNT = 2;

        @Override
        public int getViewTypeCount() {
            return VIEW_TYPE_COUNT;
        }

        @Override
        public int getItemViewType(int position) {
            if (loading && position == getCount() - 1){
                return VIEW_TYPE_LOADING;
            } else {
                return VIEW_TYPE_POST;
            }
        }

        @Override
        public int getCount() {
            return super.getCount() + (loading ? 1 : 0);
        }

        private PostsArrayAdapter(Context context, int resource, List<Post> objects) {
            super(context, resource, objects);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            int viewType = getItemViewType(position);
            if (convertView == null) {
                viewHolder = new ViewHolder();

                LayoutInflater li = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                if (viewType == VIEW_TYPE_POST) {
                    convertView = li.inflate(R.layout.fragment_posts_adap, parent, false);
                    viewHolder.titleView = (TextView) convertView.findViewById(R.id.textView2);
                    viewHolder.descView = (TextView) convertView.findViewById(R.id.textView3);
                    viewHolder.dateView = (TextView) convertView.findViewById(R.id.textView);
                    viewHolder.distanceView = (TextView) convertView.findViewById(R.id.textView4);
                    viewHolder.icon = (ImageView) convertView.findViewById(R.id.icon1);
                    viewHolder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
                    convertView.setTag(viewHolder);
                } else {
                    convertView = li.inflate(R.layout.row_loading_more, parent, false);
                    ((TextView)convertView.findViewById(R.id.row_loading_more_txt)).setText(R.string.row_posts_loading_label);
                }
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            if (viewType == VIEW_TYPE_POST) {
                Post post = getItem(position);

                viewHolder.distanceView.setText(R.string.message_na);
                Location currentLocation = LocationHandler.getLatestReportedLocation();
                if (currentLocation != null) {
                    org.buzzar.appnative.logic.objects.Location location = post.getLocation();
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

                if (post.isLive()) {
                    if (org.buzzar.appnative.logic.objects.Location.LOCATION_TYPE_DYNAMIC.equals(post.getPostType())) {
                        viewHolder.icon.setImageResource(R.drawable.postcell_dynamic_live3x);
                    } else {
                        viewHolder.icon.setImageResource(R.drawable.posttype_static_live3x);
                    }
                } else {
                    if (org.buzzar.appnative.logic.objects.Location.LOCATION_TYPE_DYNAMIC.equals(post.getPostType())) {
                        viewHolder.icon.setImageResource(R.drawable.postcell_dynamic3x);
                    } else {
                        viewHolder.icon.setImageResource(R.drawable.posttype_static3x);
                    }
                }

                viewHolder.dateView.setText(Helper.formatDate(getContext(), post.timestamp));

                Photo photo = post.getMainPhoto();
                if (photo != null) {
                    Picasso.with(getContext()).load(photo.data).into(viewHolder.imageView);
                } else {
                    viewHolder.imageView.setImageDrawable(null);
                }
            }

            return convertView;
        }

        private class ViewHolder{
            private TextView titleView;
            private TextView descView;
            private TextView dateView;
            private TextView distanceView;
            private ImageView icon;
            private ImageView imageView;
        }
    }
}
