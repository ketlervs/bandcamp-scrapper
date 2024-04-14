package br.com.forestech.bandcampscrapping.helper;

import org.jsoup.nodes.Document;

public class JsoupHelper {
    
    public static boolean albumOrTrackFree(Document document) {
        return document.select("span:contains(name your price)").size() > 0;
    }

}
