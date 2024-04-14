package br.com.forestech.bandcampscrapping.helper;


import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class LogHelper extends JFrame {
    private JTextArea textArea;
    public Logger logger;

    public LogHelper() {
        // Configurar o logger
        logger = Logger.getLogger("Bandcamp Scrapping");
        logger.setLevel(Level.INFO);

        // Configurar o manipulador de console para redirecionar logs para a janela
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.ALL);
        logger.addHandler(handler);

        // Configurar a janela
        setTitle("Baixando musicas");
        Dimension windowSize = new Dimension(1024, 768);
        setSize(windowSize);
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        textArea = new JTextArea();
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        add(scrollPane);
        setLocation(MAXIMIZED_BOTH, ABORT);

        // Obtenha informações sobre a tela
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();

        // Calcule as coordenadas para centralizar a janela
        int x = (screenSize.width - windowSize.width) / 2;
        int y = (screenSize.height - windowSize.height) / 2;

        // Defina a localização da janela para o centro
        setLocation(x, y);

        // Tornar a janela visível
        setVisible(true);
    }

    public void logMessage(String message) {
        textArea.append(message + "\n");
    }
    
    public String getText() {
        return textArea.getText();
    }
    
    public void setText(String text) {
        textArea.setText(text);
    }
}
