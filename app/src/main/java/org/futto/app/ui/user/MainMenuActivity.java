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
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.google.firebase.iid.FirebaseInstanceId;
import com.muddzdev.styleabletoastlibrary.StyleableToast;

import org.futto.app.R;
import org.futto.app.fcm.FuttoFirebaseMessageService;
import org.futto.app.networking.NetworkUtility;
import org.futto.app.nosql.NotificationDO;
import org.futto.app.session.SessionActivity;
import org.futto.app.storage.PersistentData;

import java.util.List;

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
    List<NotificationDO> result;
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
        if(NetworkUtility.networkIsAvailable(this))setUpDB();


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
        try {
            readNews();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }
    public void readNews() throws InterruptedException {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                NotificationDO news = new NotificationDO();
                news.setUserId(user);
                news.setCreationDate(new Double("1529093763866"));

                DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression()
                        .withHashKeyValues(news)
                        .withConsistentRead(false);

                result = dynamoDBMapper.query(NotificationDO.class, queryExpression);

                if (result.isEmpty()) {
                    // There were no items matching your query.
                }else{
                    Log.d("query", result.get(result.size()-1).getTitle());
                }
            }
        });
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        setUpNotification();
    }


    private void setUpNotification() {
        notification = (TextView) findViewById(R.id.noti_content);
        NotificationDO  latestNote = null;
        if(result != null && result.size() > 0) latestNote = result.get(result.size()-1);
        else notification.setText("You don't have new message now.");
        if (latestNote != null && !latestNote.getIsReaded()) {
            notification.setText(latestNote.getTitle());
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
