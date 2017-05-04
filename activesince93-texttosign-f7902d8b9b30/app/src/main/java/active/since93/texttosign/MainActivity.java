package active.since93.texttosign;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Movie;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;

import pl.droidsonroids.gif.GifImageView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private GifImageView gifImageView;
    private ImageView btnSpeak;
    private TextView txtSpeechInput;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    int duration = 0;
    private LinearLayout inputTextsLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gifImageView = (GifImageView) findViewById(R.id.gifImageView);
        btnSpeak = (ImageView) findViewById(R.id.btnSpeak);
        inputTextsLayout = (LinearLayout) findViewById(R.id.inputTextsLayout);
        txtSpeechInput = (TextView) findViewById(R.id.txtSpeechInput);

        btnSpeak.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showGifView();
                duration = 0;
                inputTextsLayout.removeAllViews();
                promptSpeechInput();
            }
        });
    }

    /**
     * Showing google speech input dialog
     */
    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Receiving speech input
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                    String inputString = result.get(0);
                    String inputStringArray[] = inputString.split(" ");
                    for (String str : inputStringArray) {
                        TextView txtItem = generateTextView(str);
                        txtItem.setOnClickListener(this);
                        inputTextsLayout.addView(txtItem);
                    }
                    displayGifForSentence(result.get(0));
                }
                break;
            }
        }
    }

    private TextView generateTextView(String str) {
        LayoutParams lpView = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lpView.setMargins(20, 20, 20, 20);
        TextView txtItem = new TextView(this);
        txtItem.setText(str);
        txtItem.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        txtItem.setTypeface(Typeface.DEFAULT_BOLD);
        txtItem.setLayoutParams(lpView);
        txtItem.setPadding(20, 20, 20, 20);
        txtItem.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.white));
        txtItem.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.black));
        return txtItem;
    }

    private void displayGifForSentence(String strComplete) {
        final String strings[] = strComplete.split(" ");
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                for (final String str : strings) {
                    try {
                        duration = getDuration(str);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                playGif(str);
                            }
                        });

                        Log.e(MainActivity.class.getSimpleName(), "Word: " + str + " : " + duration);

                        sleep(duration);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                hideGifView();
                txtSpeechInput.setText("");
                Log.e(MainActivity.class.getSimpleName(), "End");
            }
        }.execute();
    }

    private void sleep(long time) throws InterruptedException {
        Thread.sleep(time);
    }

    private void playGif(String str) {
        str = str.toLowerCase();
        int id = getResources().getIdentifier(str, "raw", getPackageName());
        gifImageView.setImageResource(id);
    }

    private int getDuration(String str) {
        str = str.toLowerCase();
        int resource = getResources().getIdentifier(str, "raw", getPackageName());

        if (resource != -0) {
            InputStream is = getResources().openRawResource(resource);
            Movie movie = Movie.decodeStream(is);
            return movie.duration();
        }
        return 0;
    }

    @Override
    public void onClick(View view) {
        if (view instanceof TextView) {
            String str = ((TextView) view).getText().toString();
            showGifView();
            displayGifForSentence(str);
        }
    }

    public void showGifView() {
        gifImageView.setImageResource(0);
        gifImageView.setVisibility(View.VISIBLE);
    }

    public void hideGifView() {
        gifImageView.setImageResource(0);
        gifImageView.setVisibility(View.INVISIBLE);
    }
}
