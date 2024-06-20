package com.wits.grofast_user.Details;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;

import com.google.android.material.textfield.TextInputEditText;
import com.wits.grofast_user.Adapter.CustomSpinnerAdapter;
import com.wits.grofast_user.Api.RetrofitService;
import com.wits.grofast_user.Api.interfaces.AddressInterface;
import com.wits.grofast_user.Api.responseModels.AddressModel;
import com.wits.grofast_user.Api.responseModels.CustomSpinnerModel;
import com.wits.grofast_user.Api.responseModels.SpinnerItemModel;
import com.wits.grofast_user.R;
import com.wits.grofast_user.session.UserActivitySession;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditAddress extends AppCompatActivity {
    private AddressModel addressModel;
    private final String TAG = "EditAddress";
    private UserActivitySession userActivitySession;
    private TextInputEditText address ;
    AppCompatSpinner stateSpinner, countrySpinner, citySpinner, pincodeSpinner;

    private CustomSpinnerAdapter countryAdapter, stateAdapter, cityAdapter, pincodeAdapter;
    private final List<CustomSpinnerModel> countrySpinnerList = new ArrayList<>();
    private final List<CustomSpinnerModel> stateSpinnerList = new ArrayList<>();
    private final List<CustomSpinnerModel> citySpinnerList = new ArrayList<>();
    private final List<CustomSpinnerModel> pincodeSpinnerList = new ArrayList<>();

    private List<SpinnerItemModel> countryList, stateList, cityList, pincodeList;
    private boolean isPincodesLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().setTitle(getString(R.string.update_address_button));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.outline_arrow_back_24);
        setContentView(R.layout.activity_edit_address);

        userActivitySession = new UserActivitySession(getApplicationContext());
        Intent intent = getIntent();
        address = findViewById(R.id.edit_address_address);
        countrySpinner = findViewById(R.id.edit_address_country);
        stateSpinner = findViewById(R.id.edit_address_state);

        citySpinner = findViewById(R.id.edit_address_city);
        pincodeSpinner = findViewById(R.id.edit_address_pincode);

        if (intent.hasExtra("address")) {
            addressModel = intent.getParcelableExtra("address");
            {
                Log.e(TAG, "onCreate: Address : " + addressModel.getAddress());
                Log.e(TAG, "onCreate: Country : " + addressModel.getCountry());
                Log.e(TAG, "onCreate: State   : " + addressModel.getState());
                Log.e(TAG, "onCreate: City    : " + addressModel.getCity());
                Log.e(TAG, "onCreate: Pincode : " + addressModel.getPin_code());
            }
            address.setText(addressModel.getAddress());

            setSpinnerAdapters();

            fetchListsOnSpinnerSelection();

            fetchCountries();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void fetchListsOnSpinnerSelection() {
        countrySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!countrySpinnerList.isEmpty()) {
                    CustomSpinnerModel model = countrySpinnerList.get(position - 1);
                    fetchStates(model.getId());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        stateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!stateSpinnerList.isEmpty()) {
                    CustomSpinnerModel model = stateSpinnerList.get(position - 1);
                    fetchCities(model.getId());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        citySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!citySpinnerList.isEmpty()) {
                    CustomSpinnerModel model = citySpinnerList.get(position - 1);
                    fetchPincodes(model.getId());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private void setSpinnerAdapters() {
        countryAdapter = new CustomSpinnerAdapter(getApplicationContext(), countrySpinnerList, R.string.spinner_select_country);
        stateAdapter = new CustomSpinnerAdapter(getApplicationContext(), stateSpinnerList, R.string.spinner_select_state);
        cityAdapter = new CustomSpinnerAdapter(getApplicationContext(), citySpinnerList, R.string.spinner_select_city);
        pincodeAdapter = new CustomSpinnerAdapter(getApplicationContext(), pincodeSpinnerList, R.string.spinner_select_pincode);

        countrySpinner.setAdapter(countryAdapter);
        stateSpinner.setAdapter(stateAdapter);
        citySpinner.setAdapter(cityAdapter);
        pincodeSpinner.setAdapter(pincodeAdapter);
    }

    private void fetchCountries() {

        Call<List<SpinnerItemModel>> call = RetrofitService.getClient(userActivitySession.getToken()).create(AddressInterface.class).getCountries();

        call.enqueue(new Callback<List<SpinnerItemModel>>() {
            @Override
            public void onResponse(Call<List<SpinnerItemModel>> call, Response<List<SpinnerItemModel>> response) {
                if (response.isSuccessful()) {
                    countryList = response.body();
                    countrySpinnerList.clear();
                    for (SpinnerItemModel model : countryList) {
                        countrySpinnerList.add(new CustomSpinnerModel(model.getName(), model.getId()));
                    }
                    countryAdapter.notifyDataSetChanged();

//                    SET SELECTED SPINNER ITEM IN CUSTOM SPINNER
                    {
                        int position = 1;
                        for (CustomSpinnerModel model : countrySpinnerList) {
                            if (model.getName().equals(addressModel.getCountry())) {
                                countrySpinner.setSelection(position);
                                break;
                            }
                            position++;
                        }
                    }

//                    CLEAR LOWER SPINNER LIST IF UPPER LIST IS EMPTY
                    if (countryList.isEmpty()) {
                        stateSpinnerList.clear();
                        citySpinnerList.clear();
                        pincodeSpinnerList.clear();

                        stateAdapter.notifyDataSetChanged();
                        cityAdapter.notifyDataSetChanged();
                        pincodeAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<SpinnerItemModel>> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void fetchStates(int countryId) {

        Call<List<SpinnerItemModel>> call = RetrofitService.getClient(userActivitySession.getToken()).create(AddressInterface.class).getStates(countryId);

        call.enqueue(new Callback<List<SpinnerItemModel>>() {
            @Override
            public void onResponse(Call<List<SpinnerItemModel>> call, Response<List<SpinnerItemModel>> response) {
                if (response.isSuccessful()) {
                    stateList = response.body();
                    stateSpinnerList.clear();
                    for (SpinnerItemModel model : stateList) {
                        stateSpinnerList.add(new CustomSpinnerModel(model.getName(), model.getId()));
                    }
                    stateAdapter.notifyDataSetChanged();

//                    SET SELECTED SPINNER ITEM IN CUSTOM SPINNER
                    {
                        int position = 1;
                        for (CustomSpinnerModel model : stateSpinnerList) {
                            if (model.getName().equals(addressModel.getState())) {
                                stateSpinner.setSelection(position);
                                break;
                            }
                            position++;
                        }
                    }

//                    CLEAR LOWER SPINNER LIST IF UPPER LIST IS EMPTY
                    if (stateList.isEmpty()) {
                        citySpinnerList.clear();
                        pincodeSpinnerList.clear();

                        cityAdapter.notifyDataSetChanged();
                        pincodeAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<SpinnerItemModel>> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void fetchCities(int stateId) {

        Call<List<SpinnerItemModel>> call = RetrofitService.getClient(userActivitySession.getToken()).create(AddressInterface.class).getCities(stateId);

        call.enqueue(new Callback<List<SpinnerItemModel>>() {
            @Override
            public void onResponse(Call<List<SpinnerItemModel>> call, Response<List<SpinnerItemModel>> response) {
                if (response.isSuccessful()) {
                    cityList = response.body();
                    citySpinnerList.clear();
                    for (SpinnerItemModel model : cityList) {
                        citySpinnerList.add(new CustomSpinnerModel(model.getName(), model.getId()));
                    }
                    cityAdapter.notifyDataSetChanged();

//                    SET SELECTED SPINNER ITEM IN CUSTOM SPINNER
                    {
                        int position = 1;
                        for (CustomSpinnerModel model : citySpinnerList) {
                            if (model.getName().equals(addressModel.getCity())) {
                                citySpinner.setSelection(position);
                                break;
                            }
                            position++;
                        }
                    }

//                    CLEAR LOWER SPINNER LIST IF UPPER LIST IS EMPTY
                    if (cityList.isEmpty()) {
                        pincodeSpinnerList.clear();
                        pincodeAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<SpinnerItemModel>> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void fetchPincodes(int cityId) {

        Call<List<SpinnerItemModel>> call = RetrofitService.getClient(userActivitySession.getToken()).create(AddressInterface.class).getPincodes(cityId);

        call.enqueue(new Callback<List<SpinnerItemModel>>() {
            @Override
            public void onResponse(Call<List<SpinnerItemModel>> call, Response<List<SpinnerItemModel>> response) {
                if (response.isSuccessful()) {
                    pincodeList = response.body();
                    pincodeSpinnerList.clear();
                    for (SpinnerItemModel model : pincodeList) {
                        pincodeSpinnerList.add(new CustomSpinnerModel(model.getCode(), model.getId()));
                    }
                    pincodeAdapter.notifyDataSetChanged();

//                    SET SELECTED SPINNER ITEM IN CUSTOM SPINNER
                    {
                        int position = 1;
                        for (CustomSpinnerModel model : pincodeSpinnerList) {
                            if (model.getName().equals(addressModel.getPin_code())) {
                                pincodeSpinner.setSelection(position);
                                break;
                            }
                            position++;
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<SpinnerItemModel>> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }
}