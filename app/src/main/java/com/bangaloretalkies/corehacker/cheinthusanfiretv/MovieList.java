package com.bangaloretalkies.corehacker.cheinthusanfiretv;

import android.os.AsyncTask;
import android.util.Log;

import com.bangaloretalkies.corehacker.cheinthusanfiretv.einthusan.ChEinthusanSearch;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

public final class MovieList implements Serializable {
    static final long serialVersionUID = 727566175075960654L;
    private static final String TAG = "MovieList";

    private ChEinthusanSearch chEinthusanSearch;

    public final String MOVIE_CATEGORY[] = {
            "Hindi",
/*            "Kannada",
            "Tamil",
            "Telugu",
            "Malayalam",
            "Marathi"*/
    };

    public HashMap<String, List<Movie>> latestMoviesMap;

    public MovieList () {
        chEinthusanSearch = new ChEinthusanSearch();
        latestMoviesMap = new HashMap<>();
    }

    public void setupLatestMovies () {
        for( String lang : MOVIE_CATEGORY ) {
            LatestMoviesAsyncTask latestMoviesAsyncTask = new LatestMoviesAsyncTask();
            latestMoviesAsyncTask.setLanguage(lang.toLowerCase());
            latestMoviesAsyncTask.execute(true);
        }
    }

    public List <Movie> getMovieListByLanguage (String lang) {
        Log.v(TAG, "getMovieListByLanguage: " + lang);
        List<Movie> list;
        int count = 10;
        while (true) {
            list = latestMoviesMap.get(lang.toLowerCase());

            if (null == list) {
                Log.d(TAG, "List not populated... Waiting...");
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                count--;
                if (count == 0) {
                    break;
                }
            }
            else {
                break;
            }
        }
        Log.v(TAG, "List size: " + list.size());
        return list;
    }

    public class LatestMoviesAsyncTask extends AsyncTask<Boolean, Integer, Boolean> {
        private String lang;

        public void setLanguage(String language) {
            lang = language;
        }

        @Override
        protected Boolean doInBackground(Boolean... booleen) {
            Log.v(TAG, "Latest Movies String = " + lang);
            List<Movie> latestMovies = chEinthusanSearch.getLatestMovies(lang.toLowerCase());
            latestMoviesMap.put(lang, latestMovies);
            return null;
        }
    }
}
