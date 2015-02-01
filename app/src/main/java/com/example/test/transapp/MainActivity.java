package com.example.test.transapp;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.List;
import java.util.Locale;


public class MainActivity extends ActionBarActivity implements TextToSpeech.OnInitListener {
    private static final int REQUEST_CODE = 0;
    private ArrayAdapter<String> adapter;
    private String fromText;
    private TextToSpeech tts;
    private boolean ttsInitFlag = false;
    private ArrayAdapter<String> adapterLanguage;
    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);

        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);

        adapterLanguage = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        for (Language lang : Language.LANGUAGES) {
            adapterLanguage.add(lang.getName());
        }

        final Spinner spinnerFrom = (Spinner) findViewById(R.id.spinner);
        spinnerFrom.setAdapter(adapterLanguage);

        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Language currentLanguage = null;
                for (Language lang : Language.LANGUAGES) {
                    if (spinnerFrom.getSelectedItem().equals(lang.getName())) {
                        currentLanguage = lang;
                        break;
                    }
                }
                try {
                    // インテント作成
                    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH); // ACTION_WEB_SEARCH
                    intent.putExtra(
                            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                    intent.putExtra(
                            RecognizerIntent.EXTRA_PROMPT,
                            "お話しください");
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, currentLanguage.getLocale().toString());
                    // インテント発行
                    startActivityForResult(intent, REQUEST_CODE);
                } catch (ActivityNotFoundException e) {
                    // このインテントに応答できるアクティビティがインストールされていない場合
                    Toast.makeText(MainActivity.this,
                            "ActivityNotFoundException", Toast.LENGTH_LONG).show();
                }
            }
        });

        tts = new TextToSpeech(getApplicationContext(), this);

        final Spinner spinnerTo = (Spinner) findViewById(R.id.spinner2);
        spinnerTo.setAdapter(adapterLanguage);
        spinnerTo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (ttsInitFlag) {
                    Locale locale = null;
                    String name = null;
                    for (Language lang : Language.LANGUAGES) {
                        if (spinnerTo.getSelectedItem().equals(lang.getName())) {
                            locale = lang.getLocale();
                            name = lang.getName();
                            break;
                        }
                    }

                    if (tts.isLanguageAvailable(locale) >= TextToSpeech.LANG_AVAILABLE) {
                        tts.setLanguage(locale);
                    } else {
                        Toast.makeText(MainActivity.this, "1. TTSが" + name + "に対応していません", Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        pd = new ProgressDialog(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        tts.shutdown();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            String resultsString = "認識失敗";

            // 結果文字列リスト
            List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            if (results.size() > 0) {
                resultsString = results.get(0);
            }

            fromText = resultsString;

            Spinner spinnerFrom = (Spinner) findViewById(R.id.spinner);
            Spinner spinnerTo = (Spinner) findViewById(R.id.spinner2);
            String fromCode = null;
            String toCode = null;
            for (Language lang : Language.LANGUAGES) {
                if (spinnerFrom.getSelectedItem().equals(lang.getName())) {
                    fromCode = lang.getCode();
                    break;
                }
            }
            for (Language lang : Language.LANGUAGES) {
                if (spinnerTo.getSelectedItem().equals(lang.getName())) {
                    toCode = lang.getCode();
                    break;
                }
            }

            System.out.println(fromCode + " -> " + toCode);
            // 翻訳APIにポスト
            TransTask task = new TransTask(fromCode, toCode) {
                @SuppressWarnings("deprecation")
                @Override
                protected void onPostExecute(String o) {
                    super.onPostExecute(o);
                    Spinner spinnerTo = (Spinner) findViewById(R.id.spinner2);
                    adapter.add(fromText + "\n" + o);
                    Language currentLanguage = null;
                    for (Language lang : Language.LANGUAGES) {
                        if (spinnerTo.getSelectedItem().equals(lang.getName())) {
                            currentLanguage = lang;
                            break;
                        }
                    }
                    if (tts.isLanguageAvailable(currentLanguage.getLocale()) >= TextToSpeech.LANG_AVAILABLE) {
                        tts.setLanguage(currentLanguage.getLocale());
                        tts.speak(o, TextToSpeech.QUEUE_FLUSH, null);
                    } else {
                        System.out.println("TTSが" + currentLanguage.getName() + "に非対応");
                        Toast.makeText(MainActivity.this, "2. TTSが" + currentLanguage.getName() + "に対応していません", Toast.LENGTH_LONG).show();
                    }
                }
            };
            task.execute(resultsString);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            ttsInitFlag = true;
            if (tts.isLanguageAvailable(Locale.ENGLISH) >= TextToSpeech.LANG_AVAILABLE) {
                tts.setLanguage(Locale.ENGLISH);
            } else {
                Toast.makeText(this, "3. TTSが英語に対応していません", Toast.LENGTH_LONG).show();
            }
        }
    }
}
