package co.speedycar.speedyracetwo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Display;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
    public ArrayList<Game> games;
    public ArrayList<Game> allGames;
    RecyclerView gamesGrid;
    GamesAdapter gamesAdapter;
    Intent intent;
    Button allCategoriesButton, favoriteButton;
    SearchView searchView;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    private MaxInterstitialAd interstitialAd;
    AppOpenManager appOpenManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_list);

        // Get the screen width and height
        Display display = getWindowManager().getDefaultDisplay();
        screenWidth = display.getWidth();
        screenHeight = display.getHeight();

        //initialize appOpen
        appOpenManager = new AppOpenManager(this);

        //initialize banner
        LinearLayout bannerLayout = findViewById(R.id.banner);
        Banner banner = new Banner(this, bannerLayout);
        banner.createBannerAd();

        //load ads
        loadInterstitialAds();

        //initialize shared preferences
        preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        editor = preferences.edit();

        //fill racing games
        try {
            fillGamesArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        setTitle("Speedy Cars Race 2");

        //get ids
        gamesGrid = findViewById(R.id.gridLayout);
        gamesGrid.setLayoutManager(new GridLayoutManager(this, 2));
        gamesAdapter = new GamesAdapter(this, games, new GamesAdapter.GameListener() {
            @Override
            public void onGameClick(Game game) {
                intent = new Intent(MainActivity.this, WebGame.class);
                WebGame.gameURL = game.getGameLink();
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("category", "Not Favorite");
                editor.apply();
                startActivity(intent);
                showAd();
                overridePendingTransition(0, 0);
            }

            @Override
            public void onFavoriteClick(Game game, int position) {
                boolean isFavorite1 = preferences.getBoolean("favorite_game_" + game.getName(), false);
                SharedPreferences.Editor editor = preferences.edit();
                if (!isFavorite1) { //save game
                    editor.putBoolean("favorite_game_" + game.getName(), true);
                } else {
                    editor.putBoolean("favorite_game_" + game.getName(), false);
                }
                editor.apply();
                gamesAdapter.notifyItemChanged(position);
                //if we are in the favourites tab reload the favorites after we unfav a game
                if(preferences.getString("mode", "").equals("f"))
                    filterFavoriteGames();
            }
        }, preferences);
        gamesGrid.setAdapter(gamesAdapter);
        allCategoriesButton = findViewById(R.id.allCategories);
        favoriteButton = findViewById(R.id.favourites);
        searchView = findViewById(R.id.searchView2);
        LinearLayout bottomNavBar = findViewById(R.id.bottomNavBar);
        TextView favoritesText = findViewById(R.id.favoritesText);
        TextView browse = findViewById(R.id.browse);

        //set hint
        searchView.setQueryHint("Search Game");
        //set game search bar functionality
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                updateFilteredGames(query);
                gamesAdapter.notifyDataSetChanged();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                updateFilteredGames(newText);
                gamesAdapter.notifyDataSetChanged();
                return true;
            }
        });

        //same as main
        allCategoriesButton.setOnClickListener(v -> {loadMainActivity();editor.putString("mode", "a");editor.apply();});
        browse.setOnClickListener(v -> {loadMainActivity();editor.putString("mode", "a");editor.apply();});
        //same as main
        favoriteButton.setOnClickListener(v -> {filterFavoriteGames();editor.putString("mode", "f");editor.apply();});
        favoritesText.setOnClickListener(v -> {filterFavoriteGames();editor.putString("mode", "f");editor.apply();});
        //disable clicking on navigation bar to avoid bugs
        bottomNavBar.setOnClickListener(v -> {
        });
    }

    //used by search bar to display only searched games
    private void updateFilteredGames(String query) {
        games.clear();
        ArrayList<Game> filteredGames = new ArrayList<>();
        // Filter and add matching categories to the filtered list
        for (Game game : allGames) {
            if (game.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredGames.add(game);
            }
        }
        // Clear the current categoriesList and add the filtered categories
        games.addAll(filteredGames);
    }

    public static boolean containsGame(ArrayList<Game> games, String gameName) {
        for (Game game : games)
            if (game.getName().equals(gameName))
                return true;
        return false;
    }

    public void loadMainActivity() {
        games.clear();
        games.addAll(allGames);
        gamesAdapter.notifyDataSetChanged();
    }

    public void filterFavoriteGames() {
        //change src to full heart
        //initialize shared preferences
        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        //clear any games in the game list for the game_list activity
        games.clear();
        //loop through all games in all categories and only add favorite games

        for (Game game : allGames) {
            boolean isFavorite = preferences.getBoolean("favorite_game_" + game.getName(), false);
            //if its a favorite game
            if (isFavorite) {
                if (!containsGame(games, game.getName()))
                    games.add(game);
            }
        }
        gamesAdapter.notifyDataSetChanged();
    }

    public void fillGamesArray() throws IOException {
        games = new ArrayList<>();
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
            for (int i = 0; i < contentArray.length(); i++) {
                JSONObject gameObj = contentArray.getJSONObject(i);

                String name = gameObj.getString("Title");
                String image = gameObj.getString("Thumbnail_Large");
                String gameLink = gameObj.getString("Content");

                Game game = new Game(name, image, gameLink);
                games.add(game);
            }

            allGames = new ArrayList<>(games);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
    }

    public void loadInterstitialAds() {

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
                interstitialAd.loadAd();
            }

            @Override
            public void onAdClicked(MaxAd maxAd) {

            }

            @Override
            public void onAdLoadFailed(String s, MaxError maxError) {

            }

            @Override
            public void onAdDisplayFailed(MaxAd maxAd, MaxError maxError) {
                interstitialAd.loadAd();
            }
        });

        interstitialAd.loadAd();
    }

    public void showAd() {
        if (interstitialAd.isReady()) {
            interstitialAd.showAd();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        appOpenManager.showAdIfReady();
    }
}