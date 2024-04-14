package br.com.forestech.bandcampscrapping.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.select.NodeFilter;

import br.com.forestech.bandcampscrapping.helper.FileHelper;
import br.com.forestech.bandcampscrapping.helper.JsoupHelper;
import br.com.forestech.bandcampscrapping.helper.LogHelper;
import br.com.forestech.bandcampscrapping.model.Link;

public class Scrapper {
    List<String> linksError = new ArrayList<>();
    LogHelper logHelper = null;
    String textLog = null;
    Integer tracksBaixadas = 0;

    public List<String> getAlbunsOrTracksFreeByRecorder(List<String> records) {
        List<String> links = new ArrayList<>();
        for (String record : records) {
            String recorderUrl = "https://" + record;
            if (!record.contains(".com") && !record.contains(".ru"))
                recorderUrl += ".bandcamp.com";

            Document document = null;
            try {
                document = Jsoup.connect(recorderUrl).get();

                Elements elements = document.getElementsByClass("art");
                List<String> albunsPages = new ArrayList<>();
                List<String> tracksPages = new ArrayList<>();
                if (elements.size() > 1) {
                    for (Element element : elements) {
                        String href = element.parent().attributes().get("href");
                        if (href.startsWith("/album")) {
                            albunsPages.add(href);
                        }
                        if (href.startsWith("/track")) {
                            tracksPages.add(href);
                        }
                    }
                }
                for (String albumPage : albunsPages) {
                    String urlAlbum = recorderUrl + albumPage;
                    try {
                        if (JsoupHelper.albumOrTrackFree(document))
                            document = Jsoup.connect(urlAlbum).get();
                        links.add(urlAlbum);
                    } catch (IOException e) {
                    }
                }
                for (String trackPage : tracksPages) {
                    String urlTrack = recorderUrl + trackPage;
                    try {
                        document = Jsoup.connect(urlTrack).get();
                        if (JsoupHelper.albumOrTrackFree(document))
                            links.add(urlTrack);
                    } catch (IOException e) {
                    }
                }
            } catch (IOException e) {
            }
        }
        return links;
    }

    public void downloadTracksMp3ByLinkRecord() {

    }

    public void downloadTracksByLinksFile() {
        logHelper = new LogHelper();
        logHelper.logMessage("[Inicio do programa Bandcamp Scrapper]\n");
        List<Link> links = getListLinkFromCsv();

        List<Link> albunsPages = new ArrayList<>();
        List<Link> tracksPages = new ArrayList<>();
        List<Link> recordsPages = new ArrayList<>();
        List<Link> linksWav = new ArrayList<>();
        boolean isWav = false;
        if (links.size() > 0) {
            for (Link link : links) {
                isWav = link.getLink().contains("/wav/") || link.getLink().contains("/mp3-320/");
                if (isWav) {
                    linksWav.add(link);
                } else {
                    if (link.getLink().contains("/track/")) {
                        tracksPages.add(link);
                    } else if (link.getLink().contains("/album/")) {
                        albunsPages.add(link);
                    } else if (link.getLink().endsWith(".bandcamp.com")) {
                        recordsPages.add(link);
                    }
                }
            }
        }
        if (recordsPages.size() > 0) {

        }
        if (albunsPages.size() > 0) {
            logHelper.logMessage("\n" + albunsPages.size() + " Albuns encontrados");

            for (Link albumPage : albunsPages) {
                try {
                    Document document = Jsoup.connect(albumPage.getLink()).get();
                    Elements elements = document.getElementsByClass("track-title");
                    String recordUrl = StringUtils.substringBetween(albumPage.getLink(), "", "/album");
                    for (Element element : elements) {
                        tracksPages.add(
                                new Link(recordUrl + element.parent().attributes().get("href"), albumPage.getGenero()));
                    }
                } catch (Exception e) {
                    linksError.add(albumPage.getLink());
                    e.printStackTrace();
                }
            }
        }

        if (tracksPages.size() > 0) {
            logHelper.logMessage(tracksPages.size() + " Tracks encontradas");
            textLog = logHelper.getText();
            tracksPages.forEach(trackPage -> {
                downloadMp3FileByTrackLink(trackPage);
            });
        } else
            logHelper.logMessage("Nenhuma track encontrada");

        if (linksWav.size() > 0) {
            logHelper.logMessage(linksWav.size() + " Tracks em .wav encontradas");
            for (Link link : linksWav) {
                try {
                    FileHelper.unpackArchive(link.getLink());
                } catch (Exception e) {
                    linksError.add(link.getLink());
                    e.printStackTrace();
                }
            }
        }

        printErros();

        logHelper.logMessage("\n\n[Fim do programa Bandcamp Scrapper]");
        logHelper.logMessage("\nPrograma será finalizado em 5 segundos...");
        try {
            Thread.sleep(6000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.exit(0);
    }

    public void downloadMp3FileByTrackLink(Link trackLink) {
        Document trackPage = null;
        String trackName = null;
        try {
            trackPage = Jsoup.connect(trackLink.getLink()).get();

            String strScript = URLDecoder.decode(
                    trackPage.getElementsByTag("head").get(0).getElementsByTag("script").get(4).outerHtml(),
                    StandardCharsets.UTF_8.toString());
            trackName = trackPage.select("h2.trackTitle").text().replaceAll("[\\\\/:*?\"<>|]", "_");
            String mp3Url = StringUtils.substringBetween(strScript, "mp3-128&quot;:&quot;", "&quot;},&quot;artist");
            if (StringUtils.isNotBlank(mp3Url)) {
                URL url = new URL(mp3Url);
                File file = new File("./downloads/" + trackName + ".mp3");

                if (!(file.exists())) {
                    if (!file.getParentFile().exists()) {
                        file.getParentFile().mkdirs();
                    }
                    FileUtils.copyURLToFile(url, file);
                    tracksBaixadas++;
                    logHelper.setText(textLog + "\n\n Track baixadas: " + tracksBaixadas);
                }
            }
        } catch (Exception e) {
            linksError.add(trackLink.getLink());
        }
    }

    // Le arquivo de links em formato csv : link*;genero*;zipFileName--obrigatorio
    // apenas para arquivo zip
    public List<Link> getListLinkFromCsv() {
        List<Link> returnLinks = new ArrayList<>();
        logHelper.logMessage("[Inicio da leitura do arquivo de links...]\n");
        BufferedReader reader;

        try {
            reader = new BufferedReader(new FileReader("./links.csv"));
            String line = reader.readLine();

            while (line != null) {
                String[] linkInfo = line.split(";");

                if (linkInfo.length > 0 && linkInfo[0] != null && !linkInfo[0].equals("")) {
                    Link link = new Link(linkInfo[0]);

                    if (linkInfo.length > 1)
                        link.setGenero(linkInfo[1]);

                    if (!linkExistsInList(returnLinks, link))
                        returnLinks.add(link);
                }

                line = reader.readLine();
            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        logHelper.logMessage("[Fim da leitura do arquivo de links...]");
        return returnLinks;
    }

    public void printErros() {
        if (linksError.size() > 0) {
            logHelper.logMessage("Erro ao baixar algumas tracks: \n\t" + StringUtils.join(linksError, "\n\t"));
        }
    }

    public boolean linkExistsInList(List<Link> links, Link link) {
        if (links != null && link != null && link.getLink() != null) {
            return links.stream().anyMatch(x -> x.getLink().equals(link.getLink()));
        }
        return false;
    }

    public void readFolderBandcampTracksEmail() {
        Properties props = System.getProperties();
        props.setProperty("mail.store.protocol", "imaps");
        Session session = Session.getDefaultInstance(props, null);
        try (Store store = session.getStore("imaps")) {
            store.connect("imap.outlook.com", "ketler1998@hotmail.com", "Rtk.2024");

            Folder[] f = store.getDefaultFolder().list();
            for (Folder fd : f){
                if(fd.getName().equalsIgnoreCase("Bandcamp tracks")){
                    fd.open(Folder.READ_ONLY);
                    for(Message message : fd.getMessages()){
                        String content = null;
                        if (message.isMimeType("text/plain")) {
                            content = message.getContent().toString();
                        } 
                        if (message.isMimeType("multipart/*")) {
                            MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
                            content = getTextFromMimeMultipart(mimeMultipart);
                        }
                        String linkPage = "http://bandcamp.com/download" + StringUtils.substringBetween(content, "bandcamp.com/download", "\n");
                        System.out.println(linkPage);
                    }
                    fd.close();
                }
            }
        } catch (MessagingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private String getTextFromMimeMultipart(
            MimeMultipart mimeMultipart) throws MessagingException, IOException {
        String result = "";
        for (int i = 0; i < mimeMultipart.getCount(); i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain")) {
                return result + "\n" + bodyPart.getContent();
            }
            result += this.parseBodyPart(bodyPart);
        }
        return result;
    }

    private String parseBodyPart(BodyPart bodyPart) throws MessagingException, IOException {
        if (bodyPart.isMimeType("text/html")) {
            return "\n" + Jsoup.parse(bodyPart.getContent().toString()).text();
        }
        if (bodyPart.getContent() instanceof MimeMultipart) {
            return getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent());
        }

        return "";
    }

}