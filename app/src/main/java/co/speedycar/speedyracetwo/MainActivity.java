package co.speedycar.speedyracetwo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;


import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.ads.MaxInterstitialAd;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    static int screenHeight, screenWidth;
    public static ArrayList<Game> games;
    public static ArrayList<Game> allGames;
    GridLayout gridLayout;
    Intent intent;
    Button allCategoriesButton, favoriteButton;
    SearchView searchView;
    SharedPreferences preferences;
    private MaxInterstitialAd interstitialAd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_list);

        // Get the screen width and height
        Display display = getWindowManager().getDefaultDisplay();
        screenWidth = display.getWidth();
        screenHeight = display.getHeight();

        //initialize appOpen
        AppOpenManager appOpenManager = new AppOpenManager(this);

        //initialize banner
        LinearLayout bannerLayout = findViewById(R.id.banner);
        Banner banner = new Banner(this, bannerLayout);
        banner.createBannerAd();

        //load ads
        loadInterstitialAds();

        //initialize shared preferences
        preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);

        if(games!=null && preferences!=null && preferences.getString("category", "").equals("Favorites")) {
            allGames = games;
            setTitle("Favorites");
        }
        else{
            //fill racing games
            try {
                fillGamesArray();
            } catch (IOException e) {
                e.printStackTrace();
            }
            setTitle("Speedy Cars Race 2");
        }

        //initialize all games
        //save all games to contain all games
        allGames = new ArrayList<>(games);

        //get ids
        gridLayout = findViewById(R.id.gridLayout);
        allCategoriesButton = findViewById(R.id.allCategories);
        favoriteButton = findViewById(R.id.favourites);
        searchView = findViewById(R.id.searchView2);
        LinearLayout bottomNavBar = findViewById(R.id.bottomNavBar);
        TextView favoritesText = findViewById(R.id.favoritesText);
        TextView browse = findViewById(R.id.browse);

        //set hint
        searchView.setQueryHint("Search Game");
        //set gae search bar functionality
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                updateFilteredGames(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                updateFilteredGames(newText);
                return true;
            }
        });
        //same as main
        allCategoriesButton.setOnClickListener(v -> loadMainActivity());
        browse.setOnClickListener(v -> loadMainActivity());
        //same as main
        favoriteButton.setOnClickListener(v -> filterFavoriteGames());
        favoritesText.setOnClickListener(v -> filterFavoriteGames());
        //disable clicking on navigation bar to avoid bugs
        bottomNavBar.setOnClickListener(v -> {
        });

        gridLayout.removeAllViews();
        addGamesToGrid();
    }
    //used by search bar to display only searched games
    private void updateFilteredGames(String query) {
        ArrayList<Game> filteredGames = new ArrayList<>();
        games = new ArrayList<>(allGames);
        // Filter and add matching categories to the filtered list
        for (Game game : allGames) {
            if (game.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredGames.add(game);
            }
        }
        // Clear the current categoriesList and add the filtered categories
        games.clear();
        games.addAll(filteredGames);

        // Update the UI with filtered categories
        addGamesToGrid();
    }

    public void addGamesToGrid(){
        gridLayout.removeAllViews();
        for(Game game : games){
            //trying to make fav icon heart

            //create new button for the game
            Button gameButton = new Button(this);
            //dynamically configure button
            //styling
            gameButton.setHeight((int)(screenHeight/6.5));
            gameButton.setWidth((int)(screenWidth/3));

//            Picasso.get().invalidate(game.getImage());
            Util.changeButtonBackground(this, gameButton, game.getImage());
            //add functionality
            gameButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //start game web view
                    intent = new Intent(MainActivity.this, WebGame.class);
                    WebGame.gameURL = game.getGameLink();
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("category", "Not Favorite");
                    editor.apply();
                    startActivity(intent);
                    showAd();
                    overridePendingTransition(0, 0); // Remove animation
                }
            });

            //game container = game image + game text
            //favButtonLayout = heart icon + favorite text
            //verticalLayout = game container + favButtonLayout
            //add container for text and image
            LinearLayout gameContainer = new LinearLayout(this);

            //text for game name
            TextView textBackground = new TextView(this);
            textBackground.setWidth((screenWidth/3));
            textBackground.setText(game.getName());
            textBackground.setTextColor(Color.rgb(244, 67, 54));
            textBackground.setBackgroundColor(Color.rgb(50, 50, 50)); // Set the background color to dark gray
            textBackground.setShadowLayer(10, 0, 0, Color.rgb(0, 0, 0)); // Set the shadow properties
            textBackground.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            textBackground.setGravity(Gravity.CENTER_HORIZONTAL);
            textBackground.setGravity(Gravity.BOTTOM);

            //gameContainer styling
            LinearLayout.LayoutParams layoutParamsText = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT , LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParamsText.setMargins(25,25,25,10);
            gameContainer.setLayoutParams(layoutParamsText);
            gameContainer.setOrientation(LinearLayout.VERTICAL);
            Drawable drawable = ContextCompat.getDrawable(this, R.drawable.text_background);
            gameContainer.setBackground(drawable);
            gameContainer.addView(gameButton);
            gameContainer.addView(textBackground);

            // Create a LinearLayout to hold the game container and favorite button
            LinearLayout verticalLayout = new LinearLayout(this);
            verticalLayout.setOrientation(LinearLayout.VERTICAL);

            // Inflate the favorite button layout
            LinearLayout favButtonLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.favourite_button_layout, null);

            //add onclick to favourite button
            ImageButton favoriteButton = favButtonLayout.findViewById(R.id.favorite_icon);
            //check if button is favorite
            boolean isFavorite = preferences.getBoolean("favorite_game_" + game.getName(), false);
            if(isFavorite) // make it red
                favoriteButton.setImageResource(R.drawable.red_heart);
            else
                favoriteButton.setImageResource(R.drawable.empty_heart);

            favoriteButton.setOnClickListener(v -> {
                boolean isFavorite1 = preferences.getBoolean("favorite_game_" + game.getName(), false);
                if(!isFavorite1){ //save game
                    favoriteButton.setImageResource(R.drawable.red_heart);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("favorite_game_" + game.getName(), true);
                    editor.apply();
                }else{
                    favoriteButton.setImageResource(R.drawable.empty_heart);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("favorite_game_" + game.getName(), false);
                    editor.apply();
                }
            });

            // Add game container and favorite button to the verticalLayout
            verticalLayout.addView(gameContainer);
            verticalLayout.addView(favButtonLayout);
            //add vertical layout to gridLayout
            gridLayout.addView(verticalLayout);
        }
    }

    public static boolean containsGame(ArrayList<Game> games, String gameName){
        for(Game game : games)
            if(game.getName().equals(gameName))
                return true;
        return false;
    }

    public void loadMainActivity(){
        intent = new Intent(MainActivity.this, MainActivity.class);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("category", "Not Favorite");
        editor.apply();
        startActivity(intent);
        overridePendingTransition(0, 0); // Remove animation
    }

    public void filterFavoriteGames(){
        //change src to full heart
        //initialize shared preferences
        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        //clear any games in the game list for the game_list activity
        MainActivity.games = new ArrayList<>();
        //loop through all games in all categories and only add favorite games

            for(Game game : allGames){
                boolean isFavorite = preferences.getBoolean("favorite_game_" + game.getName(), false);
                //if its a favorite game
                if(isFavorite){
                    if(!containsGame(MainActivity.games, game.getName()))
                        MainActivity.games.add(game);
                }
            }

        //start gameList activity
        //set title
        intent = new Intent(MainActivity.this, MainActivity.class);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("category", "Favorites");
        editor.apply();
        startActivity(intent);
        overridePendingTransition(0, 0); // Remove animation
    }

    public void fillGamesArray() throws IOException {
        StringBuilder content = new StringBuilder();
        InputStream inputStream = getResources().openRawResource(R.raw.racing_cars_json);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        String jsonData = content.toString();

        try {
            JSONObject root = new JSONObject(jsonData);
            JSONArray contentArray = root.getJSONArray("Content");

            games = new ArrayList<>();

            for (int i = 0; i < contentArray.length(); i++) {
                JSONObject gameObj = contentArray.getJSONObject(i);

                String name = gameObj.getString("Title");
                String image = gameObj.getString("Thumbnail_Large");
                String gameLink = gameObj.getString("Content");

                Game game = new Game(name, image, gameLink);
                games.add(game);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
    }

    public void loadInterstitialAds(){

        interstitialAd = new MaxInterstitialAd(getString(R.string.applovin_interstitial), this);
        interstitialAd.setListener(new MaxAdListener() {

            @Override
            public void onAdLoaded(MaxAd maxAd) {
            }

            @Override
            public void onAdDisplayed(MaxAd maxAd) {

            }

            @Override
            public void onAdHidden(MaxAd maxAd) {

            }

            @Override
            public void onAdClicked(MaxAd maxAd) {

            }

            @Override
            public void onAdLoadFailed(String s, MaxError maxError) {

            }

            @Override
            public void onAdDisplayFailed(MaxAd maxAd, MaxError maxError) {

            }
        });

        interstitialAd.loadAd();
    }

    public void showAd(){
        if(interstitialAd.isReady())
            interstitialAd.showAd();
        else
            interstitialAd.loadAd();
    }
}