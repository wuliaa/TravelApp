package com.example.travelapp.utils;

import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;


public class BaikeUtil {

    public static List<String> queryBaike(String keyword) throws IOException {
        List<String> result = new ArrayList<String>();
        Document doc = Jsoup.connect(
                "http://baike.baidu.com/search/none?word=" + keyword
                        + "&pn=0&rn=10&enc=utf8").get();
        if(doc.select("a.result-title").isEmpty()){
            return result;
        }
        String url = doc.select("a.result-title").first().attr("href");
        Log.e("111",url);
        doc = Jsoup.connect(url).get();
        for (Element element : doc.select("div.lemma-summary")
                .select("div.para")) {
            result.add(element.text());
        }
        return result;
    }
}
