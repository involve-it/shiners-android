package com.involveit.shiners.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
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
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.involveit.shiners.activities.newpost.NewPostActivity;
import com.involveit.shiners.R;
import com.involveit.shiners.logic.Constants;
import com.involveit.shiners.logic.Helper;
import com.involveit.shiners.logic.JsonProvider;
import com.involveit.shiners.logic.MeteorBroadcastReceiver;
import com.involveit.shiners.logic.cache.CacheEntity;
import com.involveit.shiners.logic.cache.CachingHandler;
import com.involveit.shiners.logic.objects.Photo;
import com.involveit.shiners.logic.objects.Post;
import com.involveit.shiners.logic.objects.response.GetPostsResponse;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import im.delight.android.ddp.MeteorSingleton;
import im.delight.android.ddp.ResultListener;

public class MeFragment extends Fragment {
    View view;
    ListView listView;
    ProgressDialog progressDialog;
    boolean mPendingPostsRequest = true;
    SwipeRefreshLayout layout;
    boolean loading = false;
    boolean moreAvailable = true;
    ArrayList<Post> posts = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_me, container, false);
        listView= (ListView) view.findViewById(R.id.listView);
        listView.setEmptyView(view.findViewById(R.id.my_posts_list_empty_view));
        layout = (SwipeRefreshLayout) view.findViewById(R.id.fragment_me_layout);
        layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!loading && MeteorSingleton.getInstance().isConnected()) {
                    getMyPosts(false);
                } else if (!MeteorSingleton.getInstance().isConnected()) {
                    mPendingPostsRequest = true;
                }
            }
        });
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (moreAvailable && totalItemCount - (firstVisibleItem + visibleItemCount) < 10){
                    getMyPosts(true);
                }
            }
        });
        setHasOptionsMenu(true);

        CacheEntity<ArrayList<Post>> cache = CachingHandler.getCacheObject(getActivity(), CachingHandler.KEY_MY_POSTS);
        if (cache == null){
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage(getResources().getString(R.string.message_loading_posts));
            progressDialog.show();
            progressDialog.setCancelable(false);
        } else {
            populateListView(cache.getObject(), false);

            if (!cache.isStale()){
                mPendingPostsRequest = false;
            }
        }

        if (mPendingPostsRequest && MeteorSingleton.getInstance().isConnected()) {
            getMyPosts(false);
            mPendingPostsRequest = false;
        }

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        meteorBroadcastReceiver.unregister(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        meteorBroadcastReceiver.register(getActivity());
    }

    public void getMyPosts(final boolean loadMore){
        if (!loading && MeteorSingleton.getInstance().isConnected()) {
            loading = true;
            final MyPostsArrayAdapter adapter = (MyPostsArrayAdapter) listView.getAdapter();
            if (adapter != null){
                adapter.notifyDataSetChanged();
            }

            HashMap<String, Object> map = new HashMap<>();
            map.put("skip", (loadMore ? posts.size() : 0));
            map.put("take", Constants.Defaults.DEFAULT_MY_POSTS_PAGE);
            map.put("type", "all");

            MeteorSingleton.getInstance().call(Constants.MethodNames.GET_MY_POSTS, new Object[]{map}, new ResultListener() {
                @Override
                public void onSuccess(String result) {
                    loading = false;
                    if (adapter != null){
                        adapter.notifyDataSetChanged();
                    }
                    GetPostsResponse response = JsonProvider.defaultGson.fromJson(result, GetPostsResponse.class);
                    if (response.success) {
                        CachingHandler.setObject(getActivity(), CachingHandler.KEY_MY_POSTS, response.result);
                        populateListView(response.result, loadMore);
                    } else {
                        Toast.makeText(getActivity(), R.string.message_internal_error, Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(getActivity(), R.string.message_internal_error, Toast.LENGTH_SHORT).show();
                    loading = false;
                    if (adapter != null){
                        adapter.notifyDataSetChanged();
                    }
                    layout.setRefreshing(false);
                }
            });
        }
    }

    public static void largeLog(String tag, String content) {
        if (content.length() > 4000) {
            Log.d(tag, content.substring(0, 4000));
            largeLog(tag, content.substring(4000));
        } else {
            Log.d(tag, content);
        }
    }

    private void populateListView(ArrayList<Post> posts, boolean append){
        MyPostsArrayAdapter adapter = (MyPostsArrayAdapter) listView.getAdapter();
        moreAvailable = posts.size() == Constants.Defaults.DEFAULT_MY_POSTS_PAGE;
        if (adapter == null){
            adapter = new MyPostsArrayAdapter(getActivity(), R.layout.fragment_me_adap, posts);
            adapter.setNotifyOnChange(true);
            listView.setAdapter(adapter);
        } else {
            if (append){
                adapter.addAll(posts);
            } else {
                Helper.mergeDataToArrayAdapter(posts, adapter, true);
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.m_fragment_me,menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.fragment_me_add:
                startActivity(new Intent(getActivity(), NewPostActivity.class));
                break;
            case R.id.fragment_me_edit:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private MeteorBroadcastReceiver meteorBroadcastReceiver = new MeteorBroadcastReceiver() {
        @Override
        public void connected() {
            if (mPendingPostsRequest){
                getMyPosts(false);
                mPendingPostsRequest = false;
            }
        }

        @Override
        public void disconnected() {

        }
    };

    private class MyPostsArrayAdapter extends ArrayAdapter<Post>{
        private static final int VIEW_TYPE_POST = 0;
        private static final int VIEW_TYPE_LOADING = 1;
        private static final int VIEW_TYPE_COUNT = 2;

        public MyPostsArrayAdapter(Context context, int resource, List<Post> objects) {
            super(context, resource, objects);
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
        public int getViewTypeCount() {
            return VIEW_TYPE_COUNT;
        }

        @Override
        public int getCount() {
            return super.getCount() + (loading ? 1 : 0);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            int viewType = getItemViewType(position);
            if (convertView == null) {
                LayoutInflater li = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                viewHolder = new ViewHolder();
                if (viewType == VIEW_TYPE_POST) {
                    convertView = li.inflate(R.layout.fragment_me_adap, parent, false);

                    viewHolder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
                    viewHolder.icon = (ImageView) convertView.findViewById(R.id.imageView5);
                    viewHolder.textTitle = (TextView) convertView.findViewById(R.id.textView2);
                    viewHolder.textDesc = (TextView) convertView.findViewById(R.id.textView3);
                    viewHolder.textCount = (TextView) convertView.findViewById(R.id.textView4);
                    viewHolder.textLeft = (TextView) convertView.findViewById(R.id.textView5);
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

                viewHolder.textTitle.setText(post.details.title);
                if (post.details.description != null) {
                    viewHolder.textDesc.setText(Html.fromHtml(post.details.description));
                } else {
                    viewHolder.textDesc.setText("");
                }

                if (post.isLive()) {
                    if (com.involveit.shiners.logic.objects.Location.LOCATION_TYPE_DYNAMIC.equals(post.getPostType())) {
                        viewHolder.icon.setImageResource(R.drawable.postcell_dynamic_live3x);
                    } else {
                        viewHolder.icon.setImageResource(R.drawable.posttype_static_live3x);
                    }
                } else {
                    if (com.involveit.shiners.logic.objects.Location.LOCATION_TYPE_DYNAMIC.equals(post.getPostType())) {
                        viewHolder.icon.setImageResource(R.drawable.postcell_dynamic3x);
                    } else {
                        viewHolder.icon.setImageResource(R.drawable.posttype_static3x);
                    }
                }

                Photo photo = post.getMainPhoto();
                if (photo != null) {
                    Picasso.with(getContext()).load(photo.original).into(viewHolder.imageView);
                } else {
                    viewHolder.imageView.setImageDrawable(null);
                }
            }

            return convertView;
        }

        private class ViewHolder{
            private ImageView imageView;

            private ImageView icon;
            private TextView textTitle;
            private TextView textDesc;
            private TextView textCount;
            private TextView textLeft;
        }
    }
}
