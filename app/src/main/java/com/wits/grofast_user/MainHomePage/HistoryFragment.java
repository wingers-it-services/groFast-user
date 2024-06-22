package com.wits.grofast_user.MainHomePage;

import static com.wits.grofast_user.CommonUtilities.handleApiError;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.SearchView;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.wits.grofast_user.Adapter.AllHistoryAdapter;
import com.wits.grofast_user.Api.RetrofitService;
import com.wits.grofast_user.Api.interfaces.OrderInterface;
import com.wits.grofast_user.Api.responseClasses.OrderHistoryResponse;
import com.wits.grofast_user.Api.responseModels.OrderItemModel;
import com.wits.grofast_user.Api.responseModels.OrderModel;
import com.wits.grofast_user.Api.responseModels.ProductModel;
import com.wits.grofast_user.R;
import com.wits.grofast_user.session.UserActivitySession;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistoryFragment extends Fragment {
    RecyclerView recyclerView;
    AllHistoryAdapter allHistoryAdapter;
    private List<OrderModel> orderList = new ArrayList<>();
    private List<OrderModel> searchOrderList = new ArrayList<>();
    private UserActivitySession userActivitySession;
    private final String TAG = "HistoryFragment";
    LinearLayout empty_layout;
    TextView empty_text1, empty_text2;
    AppCompatButton gotoproduct;
    NestedScrollView showdata;
    ShimmerFrameLayout shimmerFrameLayout;

    private ImageView searchIcon;
    private String searchQuery;
    private SearchView searchView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_history, container, false);
        if (getActivity() instanceof HomePage) {
            ((HomePage) getActivity()).updateActionBar(getString(R.string.bottom_menu_history), 0, 0);
        }

        showdata = root.findViewById(R.id.show_history_data);
        shimmerFrameLayout = root.findViewById(R.id.shimmer_layout_history);
        empty_layout = root.findViewById(R.id.history_empty_layout);
        empty_text1 = root.findViewById(R.id.history_empty_text1);
        empty_text2 = root.findViewById(R.id.history_empty_text2);
        gotoproduct = root.findViewById(R.id.history_empty_start_shopping);
        //History Item
        recyclerView = root.findViewById(R.id.history_fragment_recycleview);
        userActivitySession = new UserActivitySession(getContext());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);

        searchView = root.findViewById(R.id.order_search_view);
        searchIcon = root.findViewById(R.id.order_search_icon);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchOrders(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    showAllorderHistory();
                }
                searchQuery = newText;
                return false;
            }
        });

        searchIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchOrders(searchQuery);
            }
        });

        ShowPageLoader();
        loadOrderHistory();

        gotoproduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragmentnav, new ProductFragment());
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        return root;
    }

    private void loadOrderHistory() {
        Call<OrderHistoryResponse> call = RetrofitService.getClient(userActivitySession.getToken()).create(OrderInterface.class).fetchOrderHistory();

        call.enqueue(new Callback<OrderHistoryResponse>() {
            @Override
            public void onResponse(Call<OrderHistoryResponse> call, Response<OrderHistoryResponse> response) {
                HidePageLoader();
                if (response.isSuccessful()) {
                    OrderHistoryResponse orderHistoryResponse = response.body();
                    orderList = orderHistoryResponse.getOrderList();

//                    logOrderHistory(orderList);

                    allHistoryAdapter = new AllHistoryAdapter(getContext(), orderList);
                    recyclerView.setAdapter(allHistoryAdapter);
                } else if (response.code() == 422) {
                    try {
                        String errorBodyString = response.errorBody().string();
                        Gson gson = new Gson();
                        JsonObject errorBodyJson = gson.fromJson(errorBodyString, JsonObject.class);

                        String errorMessage = errorBodyJson.has("errorMessage") ? errorBodyJson.get("errorMessage").getAsString() : "No errorMessage";
                        String message = errorBodyJson.has("message") ? errorBodyJson.get("message").getAsString() : "No message";

                        showNoHistoryMessage(message, errorMessage);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else handleApiError(TAG, response, getContext());
            }

            @Override
            public void onFailure(Call<OrderHistoryResponse> call, Throwable t) {
                HidePageLoader();
                t.printStackTrace();
            }
        });
    }

    private void ShowPageLoader() {
        shimmerFrameLayout.startShimmer();
        shimmerFrameLayout.setVisibility(View.VISIBLE);
        showdata.setVisibility(View.GONE);
    }

    private void HidePageLoader() {
        shimmerFrameLayout.setVisibility(View.GONE);
        shimmerFrameLayout.stopShimmer();
        showdata.setVisibility(View.VISIBLE);
    }

    private void logOrderHistory(List<OrderModel> orderHistory) {
        for (OrderModel order : orderHistory) {
            Log.e(TAG, " \n\nonResponse order: order id " + order.getId());

            for (OrderItemModel orderItem : order.getOrderItems()) {
                Log.e(TAG, "onResponse orderItem: product_id " + orderItem.getProduct_id());
                Log.e(TAG, "onResponse orderItem: customer_order_id " + orderItem.getCustomer_order_id());
                Log.e(TAG, "onResponse orderItem: subtotal " + orderItem.getSubtotal());

                ProductModel productModel = orderItem.getProduct();

                if (productModel != null) {
                    Log.e(TAG, "onResponse productModel : product name  " + productModel.getName());
                } else Log.e(TAG, "onResponse productModel : product model is null ");
            }
        }
    }

    private void showNoHistoryMessage(String message, String messaage2) {
        showdata.setVisibility(View.GONE);
        empty_layout.setVisibility(View.VISIBLE);
        empty_text1.setText(message);
        empty_text2.setText(messaage2);
    }

    private void searchOrders(String searchValue) {
        ShowPageLoader();
        Call<OrderHistoryResponse> call = RetrofitService.getClient(userActivitySession.getToken()).create(OrderInterface.class).searchOrders(searchValue);

        call.enqueue(new Callback<OrderHistoryResponse>() {
            @Override
            public void onResponse(Call<OrderHistoryResponse> call, Response<OrderHistoryResponse> response) {
                HidePageLoader();
                if (response.isSuccessful()) {
                    OrderHistoryResponse orderHistoryResponse = response.body();
                    searchOrderList = orderHistoryResponse.getOrderList();


                    allHistoryAdapter = new AllHistoryAdapter(getContext(), searchOrderList);
                    recyclerView.setAdapter(allHistoryAdapter);
                } else if (response.code() == 422) {
                    try {
                        String errorBodyString = response.errorBody().string();
                        Gson gson = new Gson();
                        JsonObject errorBodyJson = gson.fromJson(errorBodyString, JsonObject.class);

                        String errorMessage = errorBodyJson.has("errorMessage") ? errorBodyJson.get("errorMessage").getAsString() : "No errorMessage";
                        String message = errorBodyJson.has("message") ? errorBodyJson.get("message").getAsString() : "No message";

                        showNoHistoryMessage(message, errorMessage);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else handleApiError(TAG, response, getContext());
            }

            @Override
            public void onFailure(Call<OrderHistoryResponse> call, Throwable t) {
                HidePageLoader();
                t.printStackTrace();
            }
        });
    }

    private void showAllorderHistory() {
        if (!orderList.isEmpty())
            allHistoryAdapter = new AllHistoryAdapter(getContext(), orderList);
        recyclerView.setAdapter(allHistoryAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadOrderHistory();
    }

    @Override
    public void onStop() {
        super.onStop();
        searchView.setQuery("", false);
    }
}