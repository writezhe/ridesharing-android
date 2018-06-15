package org.futto.app.ui.user;

import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.TextView;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.AWSStartupHandler;
import com.amazonaws.mobile.client.AWSStartupResult;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedList;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.google.gson.Gson;
import com.muddzdev.styleabletoastlibrary.StyleableToast;

import org.futto.app.R;
import org.futto.app.RunningBackgroundServiceActivity;
import org.futto.app.fcm.FuttoFirebaseMessageService;
import org.futto.app.nosql.NotesDO;
import org.futto.app.nosql.NotificationDO;
import org.futto.app.session.SessionActivity;
import org.futto.app.storage.PersistentData;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * The main menu activity of the app. Currently displays 4 buttons - Audio Recording, Graph, Call Clinician, and Sign out.
 *
 * @author Dor Samet
 */
public class MainMenuActivity extends SessionActivity {
    //extends a SessionActivity
    TextView username;
    TextView notification;
    ViewGroup route;
    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private NavigationView nvDrawer;
    private ActionBarDrawerToggle drawerToggle;
    DynamoDBMapper dynamoDBMapper;
    String user = PersistentData.getPatientID();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        displayUsername();

        // Setup drawer view
        displayToobar();
//
//		// Find our drawer view
//
        drawerLayout = (DrawerLayout) findViewById(R.id.main_DrawerLayout);
        nvDrawer = (NavigationView) findViewById(R.id.nvView);
        drawerToggle = setupDrawerToggle();


        drawerLayout.addDrawerListener(drawerToggle);

        setupDrawerContent(nvDrawer);
        setUpNotification();
        setUpDB();
        //Button callClinicianButton = (Button) findViewById(R.id.main_menu_call_clinician);
        //callClinicianButton.setText(PersistentData.getCallClinicianButtonText());
    }

    private void setUpDB() {
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                "us-east-1:ee5d02f7-4e0f-4637-875e-8eeea9b80588", // Identity pool ID
                Regions.US_EAST_1 // Region
        );


        AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient(credentialsProvider);
        dynamoDBMapper = DynamoDBMapper.builder()
                .dynamoDBClient(dynamoDBClient)
                .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                .build();
        readNews();

    }
    public void readNews() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                NotificationDO news = new NotificationDO();
                news.setUserId(user);
                news.setCreationDate(new Double("1529093763866"));

                Condition rangeKeyCondition = new Condition()
                        .withComparisonOperator(ComparisonOperator.BEGINS_WITH)
                        .withAttributeValueList(new AttributeValue().withS("Trial"));

                DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression()
                        .withHashKeyValues(news)
                        .withConsistentRead(false);

                PaginatedList<NotificationDO> result = dynamoDBMapper.query(NotificationDO.class, queryExpression);

                Gson gson = new Gson();
                StringBuilder stringBuilder = new StringBuilder();

                // Loop through query results
                for (int i = 0; i < result.size(); i++) {
                    String jsonFormOfItem = gson.toJson(result.get(i));
                    stringBuilder.append(jsonFormOfItem + "\n\n");
                }

                // Add your code here to deal with the data result
                Log.d("Query result: ", stringBuilder.toString());

                if (result.isEmpty()) {
                    // There were no items matching your query.
                }
            }
        }).start();
    }


    private void setUpNotification() {
        notification = (TextView) findViewById(R.id.noti_content);
        if (FuttoFirebaseMessageService.checkNotificationExit()) {
//		    String title = getIntent().getStringExtra("title");
            String title = FuttoFirebaseMessageService.getNoteTitle();
            notification.setText(FuttoFirebaseMessageService.getNoteTitle());
        } else {
            notification.setText("You don't have new message now.");
        }
    }

    private ActionBarDrawerToggle setupDrawerToggle() {
        return new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        drawerToggle.onConfigurationChanged(newConfig);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });
    }

    public void selectDrawerItem(MenuItem menuItem) {
        // Create a new fragment and specify the fragment to show based on nav item clicked

        switch (menuItem.getItemId()) {
            case R.id.nav_first_fragment:
                StyleableToast.makeText(this, "Sorry, this feature is temporary unavailable", R.style.mytoast).show();
                break;
            case R.id.nav_second_fragment:
                StyleableToast.makeText(this, "Sorry, this feature is temporary unavailable", R.style.mytoast).show();
                break;
            case R.id.nav_third_fragment:
                StyleableToast.makeText(this, "Sorry, this feature is temporary unavailable", R.style.mytoast).show();
                break;
            default:
                StyleableToast.makeText(this, "Sorry, this feature is temporary unavailable", R.style.mytoast).show();
                break;

        }


        // Highlight the selected item has been done by NavigationView
        menuItem.setChecked(true);
        // Set action bar title
        setTitle(menuItem.getTitle());
        // Close the navigation drawer
        drawerLayout.closeDrawers();
    }


    private void displayToobar() {
        toolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        getSupportActionBar().setTitle("Futto Main Menu");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);

    }


    private void displayUsername() {
        username = findViewById(R.id.username_name);
//		usernameNav = findViewById(R.id.nav_header_username);
        username.setText(user);
//		usernameNav.setText(user);
    }


	/*#########################################################################
	############################## Buttons ####################################
	#########################################################################*/

//	public void graphResults (View v) { startActivity( new Intent(getApplicationContext(), GraphActivity.class) ); }
}
