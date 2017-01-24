package com.eranewgames.shiners.Fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Location;
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

import com.eranewgames.shiners.App;
import com.eranewgames.shiners.PostsItem;
import com.eranewgames.shiners.R;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import im.delight.android.ddp.ResultListener;

public class FragmentPosts extends Fragment {
    TabLayout tabLayout;
    ListView listView;
    View view;
    double testLat=37.890568,testLong=-122.205730;
    JSONArray jsonArray;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
                try {
                    jsonArray = new JSONObject(result).getJSONArray("result");
                    createListView();
                    progressDialog.dismiss();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String error, String reason, String details) {
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
            public View getView(final int position, View convertView, ViewGroup parent) {
                convertView=getActivity().getLayoutInflater(). inflate(R.layout.fragment_posts_adap,parent,false);
                TextView titleView= (TextView) convertView.findViewById(R.id.textView2);
                TextView descView= (TextView) convertView.findViewById(R.id.textView3);
                TextView dateView= (TextView) convertView.findViewById(R.id.textView);
                TextView distanceView= (TextView) convertView.findViewById(R.id.textView4);
                ImageView icon1= (ImageView) convertView.findViewById(R.id.icon1);
                ImageView icon2= (ImageView) convertView.findViewById(R.id.icon2);
                final ImageView imageView= (ImageView) convertView.findViewById(R.id.imageView);

                try{
                    //Рассчет дистанции
                    double lat,lng;
                    lat=jsonArray.getJSONObject(position).getJSONObject("details").getJSONArray("locations").getJSONObject(0).getJSONObject("coords").getDouble("lat");
                    lng=jsonArray.getJSONObject(position).getJSONObject("details").getJSONArray("locations").getJSONObject(0).getJSONObject("coords").getDouble("lng");
                    Location locationA=new Location("A");
                    Location locationB=new Location("B");
                    locationA.setLatitude(App.locationLat);
                    locationA.setLongitude(App.locationLng);
                    locationB.setLatitude(lat);
                    locationB.setLongitude(lng);
                    long distance=(int)locationA.distanceTo(locationB);
                    if (distance<5280){
                        distanceView.setText(distance+" ft");
                    }else {
                        distanceView.setText(distance/5280+" mi");
                    }

                    titleView.setText(jsonArray.getJSONObject(position).getJSONObject("details").getString("title"));
                    descView.setText(Html.fromHtml(jsonArray.getJSONObject(position).getJSONObject("details").getString("description")));
                    if(jsonArray.getJSONObject(position).getJSONObject("presences").optString("static").equals("close")){
                        icon2.setImageResource(R.drawable.posttype_static_live3x);
                    }
                    if(jsonArray.getJSONObject(position).getJSONObject("presences").optString("dynamic").equals("close")){
                        icon1.setImageResource(R.drawable.postcell_dynamic_live3x);
                    }

                    //конвертирование даты в нормальный вид
                    long date=jsonArray.getJSONObject(position).getJSONObject("endDatePost").getLong("$date");
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(date);
                    dateView.setText(calendar.get(Calendar.DAY_OF_MONTH)+"/"+calendar.get(Calendar.MONTH)+"/"+calendar.get(Calendar.YEAR));

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

                } catch (JSONException e) { e.printStackTrace();}
                return convertView;
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try{
                    startActivity(new Intent(getActivity(), PostsItem.class)
                            .putExtra("position",jsonArray.getJSONObject(position).getString("_id")));
                    getActivity().overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
