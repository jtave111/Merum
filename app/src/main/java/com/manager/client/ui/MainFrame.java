package com.manager.client.ui;

import com.manager.client.Branding;
import com.manager.client.data.MerumData;
import com.manager.client.ui.theme.Theme;

import javax.swing.JFrame;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public final class MainFrame extends JFrame {
    private final MainContentPane mainContent;

    public MainFrame() {
        super(Branding.DISPLAY_NAME);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBackground(Theme.CHROME);
        setMinimumSize(new Dimension(1024, 700));
        setSize(1440, 900);
        setLocationRelativeTo(null);

        mainContent = new MainContentPane(new MerumData());
        setContentPane(mainContent);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent event) {
                mainContent.shutdown();
            }

            @Override
            public void windowClosing(WindowEvent event) {
                mainContent.shutdown();
            }
        });
    }
}
