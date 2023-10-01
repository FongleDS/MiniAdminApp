package com.example.miniadminapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ProgressFragment extends Fragment {
    private final OrderActivity.OrderManager orderManager;
    private OrderActivity.OrderCategory category; // "주문접수", "처리중", "완료" 등 카테고리
    private RecyclerView recyclerView;

    private OrderAdapter adapter;

    private Handler handler;

    public ProgressFragment(OrderActivity.OrderCategory category, OrderActivity.OrderManager orderManager, Handler handler) {
        this.category = category;
        this.handler = handler;
        this.orderManager = orderManager;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_progress, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);

        // 어댑터 초기화
        switch (category) {
            case READY:
                adapter = new OrderAdapter(orderManager, OrderActivity.OrderCategory.READY, R.layout.list_item, handler);
                break;
            case PROCESS:
                adapter = new OrderAdapter(orderManager, OrderActivity.OrderCategory.PROCESS, R.layout.list_item2, handler);
                break;
            case COMPLETE:
                adapter = new OrderAdapter(orderManager, OrderActivity.OrderCategory.COMPLETE, R.layout.list_item3, handler);
                break;
        }

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


        return view;
    }

    public void updateView() {
        adapter.notifyDataSetChanged();
    }

    public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {
        private final Handler handler;
        private final OrderActivity.OrderCategory category;
        private List<OrderActivity.Order> orderList; // 주문 목록 데이터
        private int layoutResource;

        public OrderAdapter(OrderActivity.OrderManager orderManager, OrderActivity.OrderCategory category, int layoutResource, Handler handler) {
            this.category = category;
            this.layoutResource = layoutResource;
            this.handler = handler;
        }


        public void updatList() {
            this.orderList = orderManager.getOrderList(this.category);

            Collections.sort(this.orderList);
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(layoutResource, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            // 주문 목록 데이터에서 해당 위치(position)의 주문 객체를 가져옴
            OrderActivity.Order order = orderList.get(position);
            // 뷰 홀더에 주문 정보 설정
            holder.setOrder(order);

        }

        @Override
        public int getItemCount() {
            updatList();
            return this.orderList.size();
        }



        // 뷰 홀더 클래스 정의
        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView orderNameTextView, menuTextView;
            Button checkbtn, callbtn;
            private OrderActivity.Order order;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                orderNameTextView = itemView.findViewById(R.id.orderNameTextView);
                menuTextView = itemView.findViewById(R.id.menuTextView);
                checkbtn = itemView.findViewById(R.id.checkbtn);
                callbtn = itemView.findViewById(R.id.callbtn);

                // checkbtn 버튼 클릭 이벤트 처리
                if (checkbtn != null) {
                    checkbtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            int position = getAdapterPosition();
                            if (position != RecyclerView.NO_POSITION) {
                                OrderActivity.Order order = orderList.get(position);
                                order.setCategory(OrderActivity.OrderCategory.PROCESS);
                                adapter.notifyDataSetChanged();
                            }
                        }
                    });
                }
                // 호출 버튼 클릭 이벤트 처리
                if (callbtn != null) {
                    callbtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            updatestat(orderID, 1);
                            int position = getAdapterPosition();
                            if (position != RecyclerView.NO_POSITION) {
                                OrderActivity.Order order = orderList.get(position);
                                order.setCategory(OrderActivity.OrderCategory.COMPLETE);
                                adapter.notifyDataSetChanged();
                            }
                        }
                    });
                }


            }
            public void setOrder(OrderActivity.Order order) {
                this.order = order;
                orderNameTextView.setText(order.getOrderName());
                // ArrayList의 내용을 TextView에 설정
                StringBuilder stringBuilder = new StringBuilder();
                for (OrderActivity.OrderItem item : order.getOrderItems()) {

                    stringBuilder.append(
                            String.format("%s %s \n",
                                    item.getMenu(), item.getQuantity())); // 각 항목을 줄 바꿈과 함께 추가

                    //Log.d("Order Item : ", item.getMenu());
                }
                menuTextView.setText(stringBuilder);
            }
        }
    }


    OkHttpClient client = new OkHttpClient();
    public void updatestat(String orderID, String stat) {
        RequestBody formBody = new FormBody.Builder()
                .add("orderID", orderID)
                .add("stat", stat)
                .build();
        Request request = new Request.Builder()
                .url("http://10.0.2.2:5000/updateOrderStat")
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
                        if (jsonObject.has("Result")) {
                            String result = jsonObject.getString("Result");
                            System.out.println("===========");
                            System.out.println(result);
                        } else if (jsonObject.has("error")) {
                            String error = jsonObject.getString("error");
                            System.out.println("error");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }


}
