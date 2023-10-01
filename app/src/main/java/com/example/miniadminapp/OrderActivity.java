package com.example.miniadminapp;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class OrderActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private OrdersPagerAdapter pagerAdapter;
    private OrderManager orderManager;

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

        public void addOrderItem(String menu, int quantity) {
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
        private int quantity;    // 주문 수량

        public OrderItem(String menu, int quantity) {
            this.menu = menu;
            this.quantity = quantity;
        }

        public String getMenu() {
            return menu;
        }

        public int getQuantity() {
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

        OrderManager() {
            // 주문 목록 초기화 예시
            List<OrderItem> orderItems;

            orderItems = new ArrayList<>();
            orderItems.add(new OrderItem("로제떡볶이", 1));
            orderList.add(new Order("주문1", OrderCategory.READY, orderItems));
            orderItems = new ArrayList<>();
            orderItems.add(new OrderItem("로제떡볶이",2));
            orderItems.add(new OrderItem("순대",1));
            orderList.add(new Order("주문2", OrderCategory.READY, orderItems));
            orderItems = new ArrayList<>();
            orderItems.add(new OrderItem("옛날떡볶이",1));
            orderItems.add(new OrderItem("튀김",1));
            orderList.add(new Order("주문3", OrderCategory.READY, orderItems));

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
}
