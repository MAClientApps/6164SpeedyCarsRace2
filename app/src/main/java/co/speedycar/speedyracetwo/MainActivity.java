package co.speedycar.speedyracetwo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import co.speedycar.speedyracetwo.R;

import com.applovin.sdk.AppLovinSdk;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    String JSONLink = "https://1hg26mzbpd.execute-api.eu-central-1.amazonaws.com/gbox_getgamescontent?language=en";
    public static JSONObject json;
    GridLayout gridLayout;
    StringBuilder response;
    //allCategories is fixed, categoriesList can change based on search
    static ArrayList<Category> categoriesList;
    static ArrayList<Category> allCategories;
    Intent intent;
    static int screenWidth, screenHeight;
    Button favoriteButton, allCategoriesButton;
    FrameLayout searchContainer;
    ProgressBar progressBar;
    JsonParsingTask asyncTask;
    SearchView searchView;
    AppOpenManager appOpenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //opening ad
        appOpenManager = new AppOpenManager(this);

        //initialize
        categoriesList = new ArrayList<>();
        allCategories = new ArrayList<>();

        // Get the screen width and height
        Display display = getWindowManager().getDefaultDisplay();
        screenWidth = display.getWidth();
        screenHeight = display.getHeight();

        //get ids
        favoriteButton = findViewById(R.id.favourites);
        allCategoriesButton = findViewById(R.id.allCategories);
        progressBar = findViewById(R.id.progressBar);
        searchContainer = findViewById(R.id.searchContainer);
        searchView = findViewById(R.id.searchView);
        TextView browse = findViewById(R.id.browse);
        TextView favoritesText = findViewById(R.id.favouritesText);
        LinearLayout bottomNavBar = findViewById(R.id.bottomNavBar);

        //set search bar hint
        searchView.setQueryHint("Search Category");
        // Set an OnClickListener on the SearchView to trigger focus and show keyboard
        searchContainer.setOnClickListener(v -> {
            // Request focus on the search input field
            searchView.setFocusable(true);
            searchView.setFocusableInTouchMode(true);
            searchView.requestFocus();

            // Show the keyboard
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(searchView, InputMethodManager.SHOW_IMPLICIT);
        });

        //onclicks
        //set functionality for category search bar
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                updateFilteredCategories(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                updateFilteredCategories(newText);
                return true;
            }
        });
        //set functionality for favourite button click
        favoriteButton.setOnClickListener(v -> {
            filterFavourites();
        });
        favoritesText.setOnClickListener(v -> {
            filterFavourites();
        });
        //set functionality for all categories click
        allCategoriesButton.setOnClickListener(v -> {
        });
        //set onclick listener to browse
        browse.setOnClickListener(v -> {
        });
        //disable clicking on navigation bar to avoid bugs
        bottomNavBar.setOnClickListener(v -> {
        });
        // Initialize Picasso
        Picasso.get().setIndicatorsEnabled(true);
        gridLayout = findViewById(R.id.gridLayout);
        categoriesList = new ArrayList<>();

        //parse json and place it in category list
        new JsonParsingTask(progressBar, MainActivity.this).execute(JSONLink);

        LinearLayout banner_layout = findViewById(R.id.banner_layout);
        //banner
        Banner banner = new Banner(this, banner_layout);
        banner.createBannerAd();
    }

    //gets json file and converts it to response string
    public class JsonParsingTask extends AsyncTask<String, Void, JSONObject> {

        private final ProgressBar progressBar; // Reference to your ProgressBar

        public JsonParsingTask(ProgressBar progressBar, MainActivity activity) {
            this.progressBar = progressBar;
            // Reference to your activity (if needed)
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Show loading indicator
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected JSONObject doInBackground(String... urls) {
            //convert json to string response
            if(urls[0] == null)
                return null;
            JSONObject json = null;
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                int responseCode = connection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    json = new JSONObject(response.toString());
                } else {
                    Log.e("JsonParsingTask", "HTTP request failed with response code: " + responseCode);
                }
                connection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
            MainActivity.json = json;
            return json;
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            super.onPostExecute(json);

            if (json != null) {
                // Parse and process the JSON data here
                try {
                    addJSONToCategoriesList(json);
                    //style and add buttons tho the grid layout
                    gridLayout.removeAllViews();
                    addCategoryButtonsToGrid();
                } catch (Exception ignored) {
                }
            } else {
                System.out.println("HTTP request failed with response code: ");
            }

            // Hide loading indicator
            progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Make sure to cancel the AsyncTask if the activity is destroyed
        if (asyncTask != null) {
            asyncTask.cancel(true);
        }
    }

    //add categories to grid
    public void addCategoryButtonsToGrid(){
        //empty the grid
        gridLayout.removeAllViews();
        for(Category category : categoriesList){
            Button categoryButton = new Button(MainActivity.this);

            //dynamically configure button
            //styling
            categoryButton.setHeight((int)(screenHeight/6.5));
            categoryButton.setWidth((screenWidth/3));
            //Picasso.get().invalidate(category.getImage());
            Util.changeButtonBackground(this, categoryButton, category.getImage());

            //add functionality
            categoryButton.setOnClickListener(v -> {
                GameList.games = category.gameList;
                intent = new Intent(MainActivity.this, GameList.class);
                intent.putExtra("category", category.getName());
                startActivity(intent);
            });

            LinearLayout categoryLayout = new LinearLayout(this);

            //add and style text to category
            TextView textBackground = new TextView(this);
            textBackground.setWidth((screenWidth/3));
            textBackground.setText(category.getName());
            textBackground.setTextColor(Color.rgb(244, 67, 54));
            textBackground.setBackgroundColor(Color.rgb(50, 50, 50)); // Set the background color to dark gray
            textBackground.setShadowLayer(10, 0, 0, Color.rgb(0, 0, 0)); // Set the shadow properties
            textBackground.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            textBackground.setGravity(Gravity.CENTER_HORIZONTAL);
            textBackground.setGravity(Gravity.BOTTOM);

            //style categoryLayout
            LinearLayout.LayoutParams layoutParamsText = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT , LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParamsText.setMargins(25,25,25,25);
            categoryLayout.setLayoutParams(layoutParamsText);
            categoryLayout.setOrientation(LinearLayout.VERTICAL);
            //style the categoryLayout (add roundness and shades)
            Drawable drawable = ContextCompat.getDrawable(this, R.drawable.text_background);
            categoryLayout.setBackground(drawable);
            categoryLayout.addView(categoryButton);
            categoryLayout.addView(textBackground);
            //add the categoryLayouts to the grid
            gridLayout.addView(categoryLayout);
        }
    }

    //used by search bar to filter out games
    private void updateFilteredCategories(String query) {
        ArrayList<Category> filteredCategories = new ArrayList<>();
        categoriesList = new ArrayList<>(allCategories);
        // Filter and add matching categories to the filtered list
        for (Category category : allCategories) {
            if (category.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredCategories.add(category);
            }
        }

        // Clear the current categoriesList and add the filtered categories
        categoriesList.clear();
        categoriesList.addAll(filteredCategories);

        // Update the UI with filtered categories
        addCategoryButtonsToGrid();
    }

    //fills the categoriesList and allCategories arrays
    public static void addJSONToCategoriesList(@NonNull JSONObject json) throws JSONException {
        // Parse and process the JSON data here
        // Process the "Service" object
        JSONObject service = json.getJSONObject("Service");
        String serviceName = service.getString("Name");
        String serviceIcon = service.getString("Icon");

        // Process the "Content" array
        JSONArray contentArray = json.getJSONArray("Content");
        for (int i = 0; i < contentArray.length(); i++) { //content only has HTML5
            JSONObject contentObject = contentArray.getJSONObject(i);
            JSONArray html5Array = contentObject.getJSONArray("HTML5");
            for (int j = 0; j < html5Array.length(); j++) {
                JSONObject html5Object = html5Array.getJSONObject(j);
                String catName = html5Object.getString("Name");
                String catIcon = html5Object.getString("Icon");
                // ... and so on for other fields

                //new category add it to category list
                Category category = new Category(catName, catIcon);
                categoriesList.add(category);

                //array of games fot the category
                JSONArray games = html5Object.getJSONArray("Content");
                for (int k = 0; k < games.length(); k++) {
                    JSONObject game = games.getJSONObject(k);
                    String title = game.getString("Title");
                    String thumbnail = game.getString("Thumbnail_Large");
                    String gameLink = game.getString("Content");

                    //create a new game
                    Game g = new Game(title, thumbnail, gameLink);

                    //add the games to the appropriate category
                    category.gameList.add(g);
                }
                //update category thumbnail to be the thumbnail of the first game in its games
                category.setImage(category.gameList.get(0).getImage());
            }
        }
        //save all categories in aux list
        allCategories = new ArrayList<>(categoriesList);
    }

    //filter favourites
    public void filterFavourites(){
        //initialize shared preferences
        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        //clear any games in the game list for the game_list activity
        GameList.games = new ArrayList<>();
        //loop through all games in all categories and only add favorite games
        for(Category category : allCategories){
            for(Game game : category.gameList){
                boolean isFavorite = preferences.getBoolean("favorite_game_" + game.getName(), false);
                //if its a favorite game
                if(isFavorite){
                    if(!GameList.containsGame(GameList.games, game.getName()))
                        GameList.games.add(game);
                }
            }
        }
        //start gameList activity
        //set title
        intent = new Intent(MainActivity.this, GameList.class);
        intent.putExtra("category", "Favorites");
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
    }
}
