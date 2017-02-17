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
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.involveit.shiners.activities.newpost.NewPostActivity;
import com.involveit.shiners.R;
import com.involveit.shiners.logic.Constants;
import com.involveit.shiners.logic.Helper;
import com.involveit.shiners.logic.JsonProvider;
import com.involveit.shiners.logic.LocationHandler;
import com.involveit.shiners.logic.MeteorBroadcastReceiver;
import com.involveit.shiners.logic.cache.CacheEntity;
import com.involveit.shiners.logic.cache.CachingHandler;
import com.involveit.shiners.logic.objects.Photo;
import com.involveit.shiners.logic.objects.Post;
import com.involveit.shiners.logic.objects.response.GetPostsResponse;
import com.squareup.picasso.Cache;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.ObjectOutputStream;
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
                    getMyPosts();
                } else if (!MeteorSingleton.getInstance().isConnected()) {
                    mPendingPostsRequest = true;
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
            createListView(cache.getObject());

            if (!cache.isStale()){
                mPendingPostsRequest = false;
            }
        }

        if (mPendingPostsRequest && MeteorSingleton.getInstance().isConnected()) {
            getMyPosts();
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

    public void getMyPosts(){
        loading = true;
        HashMap<String, Object> map = new HashMap<>();
        map.put("skip", 0);
        map.put("take", 100);
        map.put("type","all");

        MeteorSingleton.getInstance().call(Constants.MethodNames.GET_MY_POSTS, new Object[]{map}, new ResultListener() {
            @Override
            public void onSuccess(String result) {
                GetPostsResponse response = JsonProvider.defaultGson.fromJson(result, GetPostsResponse.class);
                if (response.success){
                    CachingHandler.setObject(getActivity(), CachingHandler.KEY_MY_POSTS, response.result);
                    createListView(response.result);
                } else {
                    Toast.makeText(getActivity(), R.string.message_internal_error, Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getActivity(), R.string.message_internal_error, Toast.LENGTH_SHORT).show();
                loading = false;
                layout.setRefreshing(false);
            }
        });
    }

    public static void largeLog(String tag, String content) {
        if (content.length() > 4000) {
            Log.d(tag, content.substring(0, 4000));
            largeLog(tag, content.substring(4000));
        } else {
            Log.d(tag, content);
        }
    }

    public void createListView(ArrayList<Post> posts){
        MyPostsArrayAdapter adapter = (MyPostsArrayAdapter) listView.getAdapter();
        if (adapter == null){
            adapter = new MyPostsArrayAdapter(getActivity(), R.layout.fragment_me_adap, posts);
            adapter.setNotifyOnChange(true);
            listView.setAdapter(adapter);
        } else {
            Helper.mergeDataToArrayAdapter(posts, adapter, true);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.m_fragment_me,menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==R.id.add){
            startActivity(new Intent(getActivity(), NewPostActivity.class));
            //getActivity().overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        }
        return super.onOptionsItemSelected(item);
    }

    private MeteorBroadcastReceiver meteorBroadcastReceiver = new MeteorBroadcastReceiver() {
        @Override
        public void connected() {
            if (mPendingPostsRequest){
                getMyPosts();
                mPendingPostsRequest = false;
            }
        }

        @Override
        public void disconnected() {

        }
    };

    private static class MyPostsArrayAdapter extends ArrayAdapter<Post>{
        public MyPostsArrayAdapter(Context context, int resource, List<Post> objects) {
            super(context, resource, objects);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                LayoutInflater li = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = li.inflate(R.layout.fragment_me_adap, parent, false);

                viewHolder = new ViewHolder();
                viewHolder.imageView= (ImageView) convertView.findViewById(R.id.imageView);
                viewHolder.icon= (ImageView) convertView.findViewById(R.id.imageView5);
                viewHolder.textTitle = (TextView) convertView.findViewById(R.id.textView2);
                viewHolder.textDesc = (TextView) convertView.findViewById(R.id.textView3);
                viewHolder.textCount = (TextView) convertView.findViewById(R.id.textView4);
                viewHolder.textLeft = (TextView) convertView.findViewById(R.id.textView5);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            Post post = getItem(position);

            viewHolder.textTitle.setText(post.details.title);
            if (post.details.description != null){
                viewHolder.textDesc.setText(Html.fromHtml(post.details.description));
            } else {
                viewHolder.textDesc.setText("");
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

            Photo photo = post.getMainPhoto();
            if (photo != null){
                Picasso.with(getContext()).load(photo.original).into(viewHolder.imageView);
            } else {
                viewHolder.imageView.setImageDrawable(null);
            }

            return convertView;
        }

        private static class ViewHolder{
            private ImageView imageView;

            private ImageView icon;
            private TextView textTitle;
            private TextView textDesc;
            private TextView textCount;
            private TextView textLeft;
        }
    }
}
