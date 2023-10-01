package com.example.miniadminapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OrderActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private OrdersPagerAdapter pagerAdapter;
    private OrderManager orderManager;
    private TextView RestName;
    ArrayList infoList = new ArrayList();
    String orderID;
    String menuID;
    String stdID;
    String menuName;

    Socket mSocket;


    public class Order implements Comparable<Order>{
        private String orderName;          // 주문명
        private Date updateTime;
        private List<OrderItem> orderItems; // 메뉴 목록과 주문 수량
        private OrderCategory category;           // 주문 카테고리

        public Order(String orderName, OrderCategory category,List<OrderItem> orderItems) {
            this.orderName = orderName;
            this.category = category;
            this.orderItems = orderItems;
            updateTime = new Date();
        }

        public String getOrderName() {
            return orderName;
        }

        public List<OrderItem> getOrderItems() {
            return orderItems;
        }

        public void addOrderItem(String menu, String quantity) {
            orderItems.add(new OrderItem(menu, quantity));
        }

        public OrderCategory getCategory() {
            return category;
        }

        public void setCategory(OrderCategory category) {
            this.category = category;
            updateTime = new Date();
        }

        @Override
        public int compareTo(Order order) {
            return this.updateTime.compareTo(order.updateTime);
        }
    }

    public class OrderItem {
        private String menu;       // 주문된 메뉴
        private String quantity;    // 주문 수량

        public OrderItem(String menu, String quantity) {
            this.menu = menu;
            this.quantity = quantity;
        }

        public String getMenu() {
            return menu;
        }

        public String getQuantity() {
            return quantity;
        }
    }

    public enum OrderCategory {
        READY,
        PROCESS,
        COMPLETE
    }

    public class OrderManager {
        private List<Order> orderList = new ArrayList<>();

        // OrderManager(String menuName, String orderID, String quantity) {
        OrderManager() {
            // 주문 목록 초기화 예시
            List<OrderItem> orderItems;

            // orderItems = new ArrayList<>();
            // orderItems.add(new OrderItem(menuName, quantity));
            // orderList.add(new Order(orderID, OrderCategory.READY, orderItems));

            orderItems = new ArrayList<>();
            orderItems.add(new OrderItem("로제떡볶이","2"));
            orderItems.add(new OrderItem("순대","1"));
            orderList.add(new Order("주문2", OrderCategory.READY, orderItems));

            // orderItems = new ArrayList<>();
            // orderItems.add(new OrderItem("옛날떡볶이",1));
            // orderItems.add(new OrderItem("튀김",1));
            // orderList.add(new Order("주문3", OrderCategory.READY, orderItems));
        }

        public List<Order> getOrderList(OrderCategory category) {
            List<Order> resultList = new ArrayList<>();
            for (Order order : orderList) {
                if (order.category == category) {
                    resultList.add(order);
                }
            }
            return resultList;
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        Intent getintent = getIntent();
        Bundle bundle = getintent.getExtras();
        String RestID = bundle.getString("RestID");
        System.out.println(RestID);
        RestName = findViewById(R.id.RestName);

        switch (RestID) {
            case "1" :
                RestName.setText("파스타");
                break;
            case "2" :
                RestName.setText("군산카츠");
                break;
            case "3" :
                RestName.setText("마성떡볶이");
                break;
            case "4" :
                RestName.setText("토스트");
                break;
            case "5" :
                RestName.setText("한우사골마라탕");
                break;
        }


        try {
            mSocket = IO.socket("http://10.0.2.2:5000");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        mSocket.on("order_updated", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject) args[0];
                System.out.println(data);
                try {
                    orderID = data.getString("orderID");
                    menuID = data.getString("MenuID");
                    stdID = data.getString("StdID");
                    getmenuName(menuID);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        //this.orderManager = new OrderManager(menuName, orderID, "1");
        this.orderManager = new OrderManager();

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        pagerAdapter = new OrdersPagerAdapter(getSupportFragmentManager());

        // 프래그먼트 추가: "주문접수", "처리중", "완료" 등
        pagerAdapter.addFragment(new ProgressFragment(OrderCategory.READY, orderManager, notiHandle), "주문접수");
        pagerAdapter.addFragment(new ProgressFragment(OrderCategory.PROCESS, orderManager, notiHandle), "처리중");
        pagerAdapter.addFragment(new ProgressFragment(OrderCategory.COMPLETE, orderManager, notiHandle), "완료");

        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(0);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                ProgressFragment progressFragment  = (ProgressFragment) pagerAdapter.getItem(position);
                progressFragment.updateView();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        tabLayout.setupWithViewPager(viewPager);



    }


    public void toProcess() {

    }

    private Handler notiHandle = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            switch (message.what) {
                case 0 :
                    break;
                case 1 :
                    toComplete((Order)message.obj);
                    break;
            }
            return false;
        }
    });

    private void toComplete(Order order) {

    }

    private class OrdersPagerAdapter extends FragmentPagerAdapter {

        private final List<Fragment> fragments = new ArrayList<>();
        private final List<String> fragmentTitles = new ArrayList<>();

        public OrdersPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        public void addFragment(Fragment fragment, String title) {
            fragments.add(fragment);
            fragmentTitles.add(title);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = fragments.get(position);
            return fragment;
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitles.get(position);
        }
    }

    OkHttpClient client = new OkHttpClient();
    public void getmenuName(String menuID) {
        RequestBody formBody = new FormBody.Builder()
                .add("menuID", menuID)
                .build();
        Request request = new Request.Builder()
                .url("http://10.0.2.2:5000/getMenuName")
                .post(formBody)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(responseBody);
                        if (jsonObject.has("Menu")) {
                            String menu = jsonObject.getString("Menu");
                            runOnUiThread(() -> {
                                menuName = menu;
                            });
                        } else if (jsonObject.has("error")) {
                            String error = jsonObject.getString("error");
                            runOnUiThread(() -> {
                                System.out.println("error");
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
