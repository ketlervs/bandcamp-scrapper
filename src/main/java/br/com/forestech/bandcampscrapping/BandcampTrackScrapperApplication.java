package br.com.forestech.bandcampscrapping;

import org.springframework.boot.autoconfigure.SpringBootApplication;

// import org.springframework.boot.SpringApplication;

import br.com.forestech.bandcampscrapping.service.Scrapper;

@SpringBootApplication
public class BandcampTrackScrapperApplication {
	public static void main(String[] args) throws InterruptedException {
		(new Scrapper()).downloadTracksByLinksFile();
	}
    
}
