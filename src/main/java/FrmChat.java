import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.net.Socket;

public class FrmChat extends javax.swing.JFrame {
    private Socket socket;
    private BufferedReader in;
    private BufferedWriter out;
    private String username;

    public FrmChat(String username, Socket socket, BufferedReader in, BufferedWriter out) {
        this.username = username;
        this.socket = socket;
        this.in = in;
        this.out = out;
        initComponents();
        listenForMessage();
    }

    public void listenForMessage(){
        new Thread(() -> {
            try {
                while (socket.isConnected()) {
                    String line = in.readLine();
                    if (line != null) {
                        System.out.println("Chat: " +line);
                        String tokens[] = line.split(";");
                        if (tokens[0].equals("/ALERT") && tokens[1].equals("/ROOM_FOUND")) {
                            /* Y/N option */
                            int option = JOptionPane.showConfirmDialog(null, tokens[2], "Room found", JOptionPane.YES_NO_OPTION);
                            if (option == JOptionPane.YES_OPTION) {
                                out.write("/ACCEPT");
                                out.newLine();
                                out.flush();
                            }
                            if (option == JOptionPane.NO_OPTION) {
                                out.write("/DECLINE");
                                out.newLine();
                                out.flush();
                            }
                        }
                        if (tokens[0].equals("/ALERT") && tokens[1].equals("/ROOM_DECLINED")) {
                            JOptionPane.showMessageDialog(null, tokens[2], tokens[2], JOptionPane.INFORMATION_MESSAGE);
                        }
                        if (tokens[0].equals("/ALERT") && tokens[1].equals("/ROOM_ACCEPTED")) {
                            lblNickname.setText(tokens[2]);
                        }
                        if (tokens[0].equals("/ALERT") && tokens[1].equals("/OTHER_USER_QUIT")) {
                            String message = lblNickname.getText() + " đã rời khỏi phòng, ban sẽ được đưa vào phòng chờ.";
                            JOptionPane.showMessageDialog(null, message, "Người kia thoát", JOptionPane.INFORMATION_MESSAGE);
                            lblNickname.setText("");
                        }
                        if (tokens[0].equals("/MESSAGE")) {
                            String message = tokens[1];
                            String oldMessage = jtextPanel.getText();
                            jtextPanel.setText(oldMessage + "\n" + message);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                closeAll(socket, in, out);
            }
        }).start();
    }

    public void closeAll(Socket socket, BufferedReader in, BufferedWriter out) {
        try {
            socket.close();
            in.close();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jtextPanel = new javax.swing.JTextPane();
        jTextField1 = new javax.swing.JTextField();
        btnSend = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        lblNickname = new javax.swing.JLabel();

        setTitle("Chào mừng " + username + " đến với ứng dụng chat");
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jScrollPane1.setViewportView(jtextPanel);

        btnSend.setText("Gữi");
        btnSend.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSendActionPerformed(evt);
            }

            private void btnSendActionPerformed(ActionEvent evt) {
                if (jTextField1.getText().equals("")) {
                    return;
                }
                try {
                    out.write("/SEND_MESSAGE;" + username + ": " + jTextField1.getText());
                    out.newLine();
                    out.flush();
                    jTextField1.setText("");
                    jTextField1.requestFocus();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        jLabel1.setText("Bạn đang chat với: ");

        lblNickname.setText("");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jScrollPane1)
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 380, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(btnSend, javax.swing.GroupLayout.DEFAULT_SIZE, 91, Short.MAX_VALUE))
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(jLabel1)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(lblNickname)
                                                .addGap(0, 0, Short.MAX_VALUE)))
                                .addContainerGap())
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel1)
                                        .addComponent(lblNickname))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 265, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jTextField1, javax.swing.GroupLayout.DEFAULT_SIZE, 37, Short.MAX_VALUE)
                                        .addComponent(btnSend))
                                .addContainerGap())
        );

        pack();
    }

    private javax.swing.JButton btnSend;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextPane jtextPanel;
    private JLabel lblNickname;
    private JLabel jLabel1;

}
