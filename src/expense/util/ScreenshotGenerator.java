package expense.util;

import expense.Main;
import expense.controller.ExpenseController;
import expense.view.MainFrame;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Automates rendering and capturing authentic, high-resolution PNG screenshots
 * of every single screen in the running application for academic documentation.
 */
public class ScreenshotGenerator {

    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        SwingUtilities.invokeLater(() -> {
            try {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception ignored) {}

                ExpenseController controller = new ExpenseController();
                controller.getSettings().setFirstLaunchPromptShown(true);
                controller.loadSampleData();

                MainFrame frame = new MainFrame(controller);
                frame.setSize(1280, 820);
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);

                // Give Swing a short moment to realize native peers and layout components
                Timer timer = new Timer(500, e -> {
                    ((Timer) e.getSource()).stop();
                    new Thread(() -> {
                        try {
                            File screenshotsDir = new File("screenshots");
                            if (!screenshotsDir.exists()) {
                                screenshotsDir.mkdirs();
                            }

                            String[][] pages = new String[][] {
                                    { MainFrame.PAGE_DASHBOARD, "01_dashboard.png" },
                                    { MainFrame.PAGE_TRANSACTIONS, "02_transactions.png" },
                                    { MainFrame.PAGE_ADD_TRANSACTION, "03_add_transaction.png" },
                                    { MainFrame.PAGE_CATEGORIES, "04_categories.png" },
                                    { MainFrame.PAGE_BUDGET, "05_budget_management.png" },
                                    { MainFrame.PAGE_REPORTS, "06_reports_analytics.png" },
                                    { MainFrame.PAGE_SEARCH, "07_search_filters.png" },
                                    { MainFrame.PAGE_EXPORT, "08_export_data.png" },
                                    { MainFrame.PAGE_SETTINGS, "09_settings.png" },
                                    { MainFrame.PAGE_ABOUT, "10_about_project.png" }
                            };

                            for (String[] page : pages) {
                                SwingUtilities.invokeAndWait(() -> {
                                    frame.navigateTo(page[0]);
                                    frame.getContentPane().validate();
                                    frame.getContentPane().repaint();
                                });

                                Thread.sleep(250);

                                SwingUtilities.invokeAndWait(() -> {
                                    try {
                                        Container content = frame.getContentPane();
                                        BufferedImage image = new BufferedImage(content.getWidth(), content.getHeight(), BufferedImage.TYPE_INT_RGB);
                                        Graphics2D g2 = image.createGraphics();
                                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                                        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                                        content.paint(g2);
                                        g2.dispose();

                                        File outputFile = new File(screenshotsDir, page[1]);
                                        ImageIO.write(image, "PNG", outputFile);
                                        System.out.println("Captured screenshot: " + outputFile.getName() + " (" + outputFile.length() + " bytes)");
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                });
                            }

                            System.out.println("All authentic screenshots successfully generated!");
                            SwingUtilities.invokeLater(frame::dispose);
                            System.exit(0);

                        } catch (Exception ex) {
                            ex.printStackTrace();
                            System.exit(1);
                        }
                    }).start();
                });
                timer.setRepeats(false);
                timer.start();

            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        });
    }
}
