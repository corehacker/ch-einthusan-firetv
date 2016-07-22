package com.bangaloretalkies.corehacker.cheinthusanfiretv.einthusan;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.bangaloretalkies.corehacker.cheinthusanfiretv.Movie;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by corehacker on 7/20/16.
 */
public class ChEinthusanSearch {
    private static final String TAG = "ChEinthusanSearch";

    private static final String E_SEARCH_URL_PREFIX = "http://www.einthusan.com/search?lang=hindi&search_query=";
    private static final String E_LATEST_MOVIES_PREFIX = "http://www.einthusan.com/index.php?lang=";
    private static final String E_MOVIE_COVER_PREFIX = "http://www.einthusan.com";
    private static final String E_MOVIE_GET_URL_PREFIX = "http://cdn.einthusan.com/geturl/";
    private static final String E_MOVIE_GET_URL_SUFFFIX = "/hd/San%2CDallas%2CToronto%2CWashington%2CLondon%2CSydney/";

    public ChEinthusanSearch() {
    }

    public List<Movie> getLatestMovies (String lang) {
        List<Movie> results = new ArrayList<>();
        String url = E_LATEST_MOVIES_PREFIX + lang;
        Log.v(TAG, "Latest Movies URL: " + url);
        String html = getHTML(url);
        Log.d(TAG, "Parsing html...");
        Document doc = Jsoup.parse(html);
        Log.d(TAG, "Parsing html... complete");

        /*
        <a class="movie-cover-wrapper" style="position: absolute;" href="./movies/watch.php?hindimoviesonline=Rocky+Handsome&lang=hindi&id=2974">
            <img src="/images/cache/Rocky-Handsome-Hindi-movie.jpg-resized.jpg" alt="Rocky Handsome hindi movie online" width="200" height="300" />
        </a>
         */
        Elements movieCovers = doc.select("a.movie-cover-wrapper");
        Elements movieCoversUrls = movieCovers.select("img");
        Log.d (TAG, "Num movie covers: " + movieCoversUrls.size());

        /*
        <div class="movie-description">
            <h1 class="seo-h1">Sarbjit Hindi Movie Online</h1>
            <p class="desc_body">
                - Aishwarya Rai, Bachchan, Randeep Hooda, Richa Chadha and Darshan Kumaar. Directed by Omung Kumar. Music by Jeet Gannguli.
                <span>2016 Sarbjit Hindi Movie Online.</span>
            </p>
        </div>
         */
        Elements movieTitlesDiv = doc.select("div.movie-description");
        Elements movieIds = doc.select("div.movie-showcase-wrapper");
        movieIds = movieIds.select("a.movie-title");
        Elements movieTitles = movieTitlesDiv.select("h1");
        Elements movieDescriptions = movieTitlesDiv.select("p");

        int i = 0;
        for (Element movieTitle : movieTitles) {
            String title = movieTitle.text();
            String description = movieDescriptions.get(i).text();
            String coverUrl = E_MOVIE_COVER_PREFIX + movieCoversUrls.get(i).attr("src");


            Uri uri = Uri.parse(movieIds.get(i).attr("href"));
            String movieId = uri.getQueryParameter("id");

            Log.d (TAG, "Movie ID: " + movieId);
            Log.d(TAG, "Movie Title: " + title + ", Description: " +
                    description + ", Cover: " + coverUrl);
            Movie movie = new Movie();
            movie.setId(Integer.parseInt(movieId));
            movie.setBackgroundImageUrl(coverUrl);
            movie.setCardImageUrl(coverUrl);
            movie.setTitle(title);
            movie.setDescription(description);

            GetMovieUrlAsyncTask getMovieUrlAsyncTask = new GetMovieUrlAsyncTask();
            getMovieUrlAsyncTask.setLanguage(lang.toLowerCase());
            getMovieUrlAsyncTask.setId(movieId);
            getMovieUrlAsyncTask.setMovie(movie);
            getMovieUrlAsyncTask.execute(true);

            results.add(movie);

            i++;
        }
        return results;
    }

    public List<String> searchMovies (String query) {
        List<String> results;
        String url = E_SEARCH_URL_PREFIX + query;
        Log.v(TAG, "Search URL: " + url);
        results =  connect(url);
        return results;
    }

    private String getHTML (String url) {
        String html;
        int responseCode = 0;
        HttpURLConnection con = null;
        URL obj = null;

        Log.d(TAG, "\nSending 'GET' request to URL : " + url);

        try {
            obj = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try {
            con = (HttpURLConnection) obj.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            con.setRequestMethod("GET");
        } catch (ProtocolException e) {
            e.printStackTrace();
        }

        /*
            curl -i 'http://cdn.einthusan.com/geturl/644/hd/San%2CDallas%2CToronto%2CWashington%2CLondon%2CSydney/' -H 'Origin: http://www.einthusan.com' -H 'Referer: http://www.einthusan.com'
        */
        con.setRequestProperty("Origin", "http://www.einthusan.com");
        con.setRequestProperty("Referer", "http://www.einthusan.com");


        try {
            responseCode = con.getResponseCode();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "Response Code : " + responseCode);

        BufferedReader in = null;
        try {
            in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        String inputLine;
        StringBuffer response = new StringBuffer();

        try {
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        html = response.toString();
        return html;
    }

    protected List<String> connect(String url)
    {
        List<String> results = new ArrayList<>();
        String html = getHTML (url);

        Log.d(TAG, "Parsing html...");
        Document doc = Jsoup.parse(html);
        Log.d(TAG, "Parsing html... complete");
        Elements info = doc.select("div#non-realtime-search");
        info = info.select("div.search-category");
        info = info.select("ul:has(li)");
        Elements searchedLinks = info.select("a[href]");

        Log.d(TAG, "Search Results Size: " + searchedLinks.size());

        for (Element link : searchedLinks) {
            Log.d(TAG, " * a: <" + link.attr("href") + ">  (" + link.text() + ")");

            Uri uri = Uri.parse(link.attr("href"));
            String movieId = uri.getQueryParameter("id");
            String movieName = uri.getQueryParameter("hindimoviesonline");
            String movieLang = uri.getQueryParameter("lang");
            Log.d(TAG, "movieId: " + movieId + ", movieName: " + movieName + ", movieLang: " + movieLang);

            results.add(link.text());
        }
        return results;
    }

    public class GetMovieUrlAsyncTask extends AsyncTask<Boolean, Integer, Boolean> {
        private String lang;
        private String id;
        private Movie movie;

        public void setLanguage(String language) {
            lang = language;
        }

        public void setId (String movieId) {
            id = movieId;
        }

        public void setMovie (Movie movie) {
            this.movie = movie;
        }

        private void getMovieUrl () {
            /*
            http://cdn.einthusan.com/geturl/" + movieId + "/hd/San%2CDallas%2CToronto%2CWashington%2CLondon%2CSydney/
             */
            String url = E_MOVIE_GET_URL_PREFIX + id + E_MOVIE_GET_URL_SUFFFIX;
            Log.v(TAG, "Get Movie URL URL: " + url);
            String html = getHTML(url);
            movie.setVideoUrl(html);
            Log.v(TAG, "Movie URL: " + html);
        }

        @Override
        protected Boolean doInBackground(Boolean... booleen) {
            Log.v(TAG, "Latest Movies String = " + lang);
            getMovieUrl();
            return null;
        }
    }
}
