package com.sharletpc;

import javax.swing.*;
import java.awt.*;
public class Log extends JFrame {
    private final JTextArea textArea;

    public Log() {
        super("SHARLET PC Server LOGS");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        textArea = new JTextArea();

        Font font = new Font("Arial", Font.PLAIN, 16);
        textArea.setFont(font);

        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(600, 500));
        add(scrollPane);
        pack();
        setLocationRelativeTo(null);
    }

    public void println(String text) {
        textArea.append(text + "\n");
        textArea.setCaretPosition(textArea.getDocument().getLength());
    }
}
