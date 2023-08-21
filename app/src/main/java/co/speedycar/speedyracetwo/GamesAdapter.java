package co.speedycar.speedyracetwo;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class GamesAdapter extends RecyclerView.Adapter<GamesAdapter.ViewHolder> {

    ArrayList<Game> games;
    GameListener listener;
    SharedPreferences preferences;
    Context context;

    public GamesAdapter(Context context, ArrayList<Game> games, GameListener listener, SharedPreferences preferences) {
        this.context = context;
        this.games = games;
        this.listener = listener;
        this.preferences = preferences;
    }

    @NonNull
    @Override
    public GamesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.game_list_item, parent, false);
        GridLayoutManager.LayoutParams lp = (GridLayoutManager.LayoutParams) itemView.getLayoutParams();
        lp.height = parent.getMeasuredHeight() / 3;
        itemView.setLayoutParams(lp);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull GamesAdapter.ViewHolder holder, int position) {
        Game game = games.get(position);
        if (game != null) {
            holder.bindView(game, position);
        }
    }

    @Override
    public int getItemCount() {
        return games.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title;
        TextView favorite;
        View itemView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            image = itemView.findViewById(R.id.image);
            title = itemView.findViewById(R.id.title);
            favorite = itemView.findViewById(R.id.fav_btn);
        }

        public void bindView(Game game, int position) {
            Picasso.get()
                    .load(game.getImage())
                    .into(image);
            title.setText(game.getName());
            //check if favourite and change heart icon
            boolean isFavorite = preferences.getBoolean("favorite_game_" + game.getName(), false);
            int newDrawableResource = isFavorite ? R.drawable.ic_heart_filled : R.drawable.ic_heart;
            Drawable drawable = ContextCompat.getDrawable(context, newDrawableResource);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            favorite.setCompoundDrawablesRelative(drawable, null, null, null);
            favorite.setOnClickListener(view -> listener.onFavoriteClick(game, position));
            itemView.setOnClickListener(view -> listener.onGameClick(game));
        }
    }

    public interface GameListener {
        void onGameClick(Game game);

        void onFavoriteClick(Game game, int position);
    }

}
