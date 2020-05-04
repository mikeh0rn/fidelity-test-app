package com.stevehwang.basicapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.localytics.android.CallToActionListenerAdapterV2;
import com.localytics.android.Campaign;
import com.localytics.android.Localytics;

import org.json.JSONException;
import org.json.JSONObject;

import io.branch.indexing.BranchUniversalObject;
import io.branch.referral.Branch;
import io.branch.referral.BranchError;
import io.branch.referral.BranchUtil;
import io.branch.referral.util.BRANCH_STANDARD_EVENT;
import io.branch.referral.util.BranchContentSchema;
import io.branch.referral.util.BranchEvent;
import io.branch.referral.util.ContentMetadata;
import io.branch.referral.util.CurrencyType;
import io.branch.referral.util.LinkProperties;
import io.branch.referral.util.ProductCategory;
import io.branch.referral.validators.IntegrationValidator;


public class MainActivity extends AppCompatActivity implements View.OnClickListener  {
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Localytics.setCallToActionListener(new CallToActionListenerAdapterV2() {
            @Override
            public boolean localyticsShouldDeeplink(@NonNull final String url, @NonNull Campaign campaign) {
                handleURL(url);
                return false;
            }
        });
        button = findViewById(R.id.button);
        button.setOnClickListener(this);
    }

    @Override public void onStart() {
        super.onStart();
        Branch.sessionBuilder(this).withCallback(callback).withData(getIntent() != null ? getIntent().getData() : null).init();
//        IntegrationValidator.validate(MainActivity.this);
    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
//        Localytics.onNewIntent(this, intent);

        // if activity is in foreground (or in backstack but partially visible) launching the same
        // activity will skip onStart, handle this case with reInitSession
        Branch.sessionBuilder(this).withCallback(callback).reInit();
        Log.d("Branch - MainActivity", "called Branch.reInit() from onNewIntent");
    }

    private Branch.BranchReferralInitListener callback = new Branch.BranchReferralInitListener() {
        @Override
        public void onInitFinished(JSONObject linkProperties, BranchError error) {
            // do stuff with deep link data (nav to page, display content, etc)
            Log.d("Branch Callback", "onInitFinished");
            try {
                if (linkProperties.has("+clicked_branch_link") && linkProperties.getBoolean("+clicked_branch_link")) {
                    Log.d("Branch Callback", "+clicked_branch_link = true");
                } else {
                    Log.d("Branch Callback", "+clicked_branch_link = false");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private void handleURL(String url) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("branch",url);
        intent.putExtra("branch_force_new_session",true);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        Log.d("Branch - MyApplication", "url: "+url);
        Log.d("Branch - MyApplication", "calling startedActivity(intent)");
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        Localytics.tagEvent("Team Favorited");

    }

    private void logCustomEvent() {
        new BranchEvent("Button Clicked")
                //.addCustomDataProperty("top", "true")
                .addCustomDataProperty("custom data", "data")
                .logEvent(this);

        Branch.getInstance().setRequestMetadata("more metadata", "some more custom data");
    }

    private void logCommerceEvent() {
        BranchUniversalObject buo = new BranchUniversalObject()
                .setCanonicalIdentifier("myprod/1234")
                .setCanonicalUrl("https://test_canonical_url")
                .setTitle("test_title")
                .setContentMetadata(
                        new ContentMetadata()
                                .addCustomMetadata("custom_metadata_key1", "custom_metadata_val1")
                                .addCustomMetadata("custom_metadata_key1", "custom_metadata_val1")
                                .addImageCaptions("image_caption_1", "image_caption2", "image_caption3")
                                .setAddress("Street_Name", "test city", "test_state", "test_country", "test_postal_code")
                                .setRating(5.2, 6.0, 5)
                                .setLocation(-151.67, -124.0)
                                .setPrice(10.0, CurrencyType.USD)
                                .setProductBrand("test_prod_brand")
                                .setProductCategory(ProductCategory.APPAREL_AND_ACCESSORIES)
                                .setProductName("test_prod_name")
                                .setProductCondition(ContentMetadata.CONDITION.EXCELLENT)
                                .setProductVariant("test_prod_variant")
                                .setQuantity(1.5)
                                .setSku("test_sku")
                                .setContentSchema(BranchContentSchema.COMMERCE_PRODUCT))
                .addKeyWord("keyword1")
                .addKeyWord("keyword2");

        new BranchEvent(BRANCH_STANDARD_EVENT.ADD_TO_CART)
                .setAffiliation("test_affiliation")
                .setCustomerEventAlias("my_custom_alias")
                .setCoupon("Coupon Code")
                .setCurrency(CurrencyType.USD)
                .setDescription("Customer added item to cart")
                .setShipping(0.0)
                .setTax(9.75)
                .setRevenue(1.5)
                .setSearchQuery("Test Search query")
                .addCustomDataProperty("Custom_Event_Property_Key1", "Custom_Event_Property_val1")
                .addCustomDataProperty("Custom_Event_Property_Key2", "Custom_Event_Property_val2")
                .addContentItems(buo)
                .logEvent(this);

        return;
    }

}
