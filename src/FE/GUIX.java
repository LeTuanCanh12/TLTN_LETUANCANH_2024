package FE;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import BE.TwitterInteractionTool;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class GUIX {
    static String filePath = " ";
    static TwitterInteractionTool tool = new TwitterInteractionTool();
    static Thread threadCheckSearch, threadCheckNewfeed;
    static Thread threadNewfeed, threadSearch;

    public static void main(String[] args) {
        // Tạo JFrame để chứa giao diện
        JFrame frame = new JFrame("GPT on Twitter");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Tạo JTabbedPane để chứa các tab
        JTabbedPane tabbedPane = new JTabbedPane();

        // Tạo panel cho tab "Home"
        JPanel newfeedPanel = new JPanel(new GridBagLayout());
        newfeedPanel.setBorder(new EmptyBorder(10, 10, 10, 10)); // Thêm khoảng trống xung quanh panel
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // Thêm khoảng cách giữa các thành phần

        // Thiết lập font chữ và màu sắc
        Font labelFont = new Font("Arial", Font.BOLD, 14);
        Font buttonFont = new Font("Arial", Font.PLAIN, 12);

        // Thêm các thành phần vào panel "Home"
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        JLabel linkLabel = new JLabel("Link:");
        linkLabel.setFont(labelFont);
        newfeedPanel.add(linkLabel, gbc);

        gbc.gridx = 1;
        JTextField quantity = new JTextField(20);
        newfeedPanel.add(quantity, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel textLabel = new JLabel("Text:");
        textLabel.setFont(labelFont);
        newfeedPanel.add(textLabel, gbc);

        gbc.gridx = 1;
        JTextField textInput = new JTextField(20);
        newfeedPanel.add(textInput, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel uploadLabel = new JLabel("Upload your file:");
        uploadLabel.setFont(labelFont);
        newfeedPanel.add(uploadLabel, gbc);

        gbc.gridx = 1;
        JButton importButton = new JButton("Browse");
        importButton.setFont(buttonFont);
        importButton.setToolTipText("Browse and select the file to upload");
        importButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Tạo JFileChooser
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileFilter(new FileNameExtensionFilter("Excel Files", "xls", "xlsx"));

                // Hiển thị hộp thoại chọn file
                int result = fileChooser.showOpenDialog(frame);
                if (result == JFileChooser.APPROVE_OPTION) {
                    // Lấy đường dẫn của file được chọn
                    File selectedFile = fileChooser.getSelectedFile();
                    filePath = selectedFile.getAbsolutePath();
                }
            }
        });
        newfeedPanel.add(importButton, gbc);
        gbc.gridx = 1;
        gbc.gridy = 3;
        JButton btnStart = new JButton("Upload file");
        btnStart.setFont(buttonFont);
        btnStart.setToolTipText("Upload the selected file");
        btnStart.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                btnStart.setText("Loading...");  // Hiển thị trạng thái tải
                btnStart.setEnabled(false);  // Vô hiệu hóa nút khi đang tải

                SwingWorker<Void, Void> worker = new SwingWorker<>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        tool.upLoadFile(filePath);
                        return null;
                    }

                    @Override
                    protected void done() {
                        btnStart.setText("Upload file");  // Khôi phục trạng thái nút
                        btnStart.setEnabled(true);  // Kích hoạt lại nút sau khi hoàn thành
                    }
                };
                worker.execute();
            }
        });

        newfeedPanel.add(btnStart, gbc);
        gbc.gridx = 2;
        gbc.gridy = 4;
        JButton btnStartNew = new JButton("Start");
        btnStartNew.setFont(buttonFont);
        btnStartNew.setToolTipText("Start the interaction tool");
        btnStartNew.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                threadNewfeed = new Thread(() -> {
                    try {
                        tool.start(quantity.getText().trim(), textInput.getText());
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                });
                threadNewfeed.start();
            }
        });
        newfeedPanel.add(btnStartNew, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
       
        JButton btnClose = new JButton("Close");
        btnClose.setFont(buttonFont);
        btnClose.setToolTipText("Close the application");
        btnClose.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        newfeedPanel.add(btnClose, gbc);

        // Thêm panel "Home" vào JTabbedPane
        tabbedPane.addTab("Home", newfeedPanel);

        // Thêm JTabbedPane vào JFrame
        frame.add(tabbedPane);

        // Thiết lập kích thước và hiển thị JFrame
        frame.setSize(500, 300);
        frame.setLocationRelativeTo(null); // Căn giữa cửa sổ trên màn hình
        frame.setVisible(true);
    }
}
