package co.speedycar.speedyracetwo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class GamesAdapter extends RecyclerView.Adapter<GamesAdapter.ViewHolder> {

    ArrayList<Game> games;
    GameListener listener;

    public GamesAdapter(ArrayList<Game> games, GameListener listener) {
        this.games = games;
        this.listener = listener;
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
            holder.bindView(game);
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

        public void bindView(Game game) {
            Picasso.get()
                    .load(game.getImage())
                    .into(image);
            title.setText(game.getName());
            favorite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onFavoriteClick(game);
                }
            });
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onGameClick(game);
                }
            });
        }
    }

    public interface GameListener {
        void onGameClick(Game game);

        void onFavoriteClick(Game game);
    }

}
