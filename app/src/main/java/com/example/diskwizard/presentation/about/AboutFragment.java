package com.example.diskwizard.presentation.about;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import com.example.diskwizard.databinding.FragmentAboutBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import okhttp3.*;

public class AboutFragment extends Fragment {

    private final Runnable onAppAboutClick;
    private final Runnable onDeveloperAboutClick;

    FragmentAboutBinding binding;
    private final OkHttpClient client = new OkHttpClient();
    private ArrayList<String> news = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    public AboutFragment(Runnable onAppAboutClick, Runnable onDeveloperAboutClick) {
        this.onDeveloperAboutClick = onDeveloperAboutClick;
        this.onAppAboutClick = onAppAboutClick;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAboutBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        Button appAboutButton = binding.appAboutButton;
        Button developerAboutButton = binding.developerAboutButton;

        appAboutButton.setOnClickListener(v -> onAppAboutClick.run());
        developerAboutButton.setOnClickListener(v -> onDeveloperAboutClick.run());

        ListView listView = binding.listView;
        adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, news);
        listView.setAdapter(adapter);

        loadRSS();

        FloatingActionButton moreInfoButton = binding.moreInfo;
        moreInfoButton.setOnClickListener(v -> showInfoDialog());

        return view;
    }

    private void loadRSS() {
        Request request = new Request.Builder()
                .url("http://www.ixbt.com/export/sec_optical.rss") //  URL RSS-ленты
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                parseRSS(response.body().string());
            }
        });
    }

    private void parseRSS(String rss) {
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser xpp = factory.newPullParser();

            xpp.setInput(new StringReader(rss));
            int eventType = xpp.getEventType();
            String title = null;
            String date = null;
            boolean isFirstTitle = true;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    if (xpp.getName().equalsIgnoreCase("title")) {
                        eventType = xpp.next();
                        if (isFirstTitle) {
                            isFirstTitle = false;
                        } else {
                            title = xpp.getText();
                        }
                    } else if (xpp.getName().equalsIgnoreCase("pubDate")) {
                        eventType = xpp.next();
                        date = xpp.getText().replaceFirst("\\+\\d{4}$", "");
                    }
                } else if (eventType == XmlPullParser.END_TAG) {
                    if (xpp.getName().equalsIgnoreCase("item")) {
                        if (title != null && date != null) {
                            news.add(title + "\n" + date);
                            title = null;
                            date = null;
                        }
                    }
                }
                eventType = xpp.next();
            }

            requireActivity().runOnUiThread(new Runnable() {
                public void run() {
                    adapter.notifyDataSetChanged();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showInfoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("iXBT.com: Носители информации");
        builder.setMessage("iXBT.com (https://www.ixbt.com) -- специализированный российский информационно-аналитический сервер, освещающий вопросы аппаратного обеспечения персональных компьютеров, коммуникаций и серверов, 3D-графики и звука, цифрового фото и видео, Hi-Fi аппаратуры и проекторов, мобильной связи и периферии, игровых приложений и многого другого.");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Код, который выполняется при нажатии на кнопку "OK"
                dialog.dismiss(); // Закрываем диалоговое окно
            }
        });
        builder.show();
    }
}