package com.sharletpc;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Log extends JFrame {
    private final JTextArea textArea;

    public Log(LogControls controls) {
        super("SHARLET PC Server LOGS");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        textArea = new JTextArea();

        Font font = new Font("Arial", Font.PLAIN, 16);
        textArea.setFont(font);

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                super.windowClosed(e);
                controls.onClose();
            }
        });

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
