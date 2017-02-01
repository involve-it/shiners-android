package com.involveit.shiners.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.involveit.shiners.activities.newpost.NewPostActivity;
import com.involveit.shiners.R;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import im.delight.android.ddp.MeteorSingleton;
import im.delight.android.ddp.ResultListener;

public class MeFragment extends Fragment {
    View view;
    ListView listView;
    JSONArray jsonArray;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_me, container, false);
        listView= (ListView) view.findViewById(R.id.listView);
        setHasOptionsMenu(true);
        getMyPosts();
        return view;
    }

    public void getMyPosts(){
        HashMap map = new HashMap();
        map.put("skip", 0);
        map.put("take", 30);
        map.put("type","all");

        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(getResources().getString(R.string.message_loading_posts));
        progressDialog.show();
        progressDialog.setCancelable(false);

        MeteorSingleton.getInstance().call("getMyPosts", new Object[]{map}, new ResultListener() {

            @Override
            public void onSuccess(String result) {
                try {
                    Log.d("MeFragment=onSuccess", result);
                    jsonArray=new JSONObject(result).getJSONArray("result");
                    createListView();
                } catch (JSONException e) { e.printStackTrace(); }
                progressDialog.dismiss();
            }

            @Override
            public void onError(String error, String reason, String details) {

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

    public void createListView(){
        listView.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return jsonArray.length();
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
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = getActivity().getLayoutInflater().inflate(R.layout.fragment_me_adap, parent, false);
                }
                ImageView imageView= (ImageView) convertView.findViewById(R.id.imageView);
                ImageView imageIcon1= (ImageView) convertView.findViewById(R.id.imageView4);
                ImageView imageIcon2= (ImageView) convertView.findViewById(R.id.imageView5);
                TextView textTitle = (TextView) convertView.findViewById(R.id.textView2);
                TextView textDesc = (TextView) convertView.findViewById(R.id.textView3);
                TextView textCount = (TextView) convertView.findViewById(R.id.textView4);
                TextView textLeft = (TextView) convertView.findViewById(R.id.textView5);
                try{
                    textTitle.setText(jsonArray.getJSONObject(position).getJSONObject("details").getString("title"));
                    textDesc.setText(jsonArray.getJSONObject(position).getJSONObject("details").getString("description"));

                    //Проверка null ключа для картинок и загрузки
                    if (!jsonArray.getJSONObject(position).getJSONObject("details").isNull("photos")){
                        if (!jsonArray.getJSONObject(position).getJSONObject("details").getJSONArray("photos").isNull(0)){
                            if (!jsonArray.getJSONObject(position).getJSONObject("details").getJSONArray("photos").getJSONObject(0).isNull("thumbnail")){
                                Picasso.with(getActivity())
                                        .load(jsonArray.getJSONObject(position).getJSONObject("details").getJSONArray("photos").getJSONObject(0).getString("thumbnail"))
                                        .into(imageView);
                            }
                        }
                    }
                } catch (JSONException e) { e.printStackTrace(); }
                return convertView;
            }
        });
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
            getActivity().overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        }
        return super.onOptionsItemSelected(item);
    }
}
