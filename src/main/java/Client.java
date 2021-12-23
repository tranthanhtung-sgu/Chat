import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client extends JFrame {
    private Socket socket;
    private BufferedReader in;
    private BufferedWriter out;
    private String username = "";

    public static void main(String[] args) throws Exception {
        Client client = new Client();
    }

    public Client(){
        Init();
    }


    public void sendMessage() throws Exception {
        try {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                String line = scanner.nextLine();
                out.write(username + ": " + line);
                out.newLine();
                out.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
            closeAll(socket, in, out);
        }
    }

    public void listenForMessage(){
        new Thread(() -> {
            try {
                while (socket.isConnected()) {
                    String line = in.readLine();
                    if (line != null) {
                        System.out.println("client: " + line);
                        String tokens[] = line.split(";");
                        if (tokens[0].equals("/ALERT") && tokens[1].equals("/NICKNAME_ERROR")) {
                            JOptionPane.showMessageDialog(null, tokens[2]);
                            return;
                        }
                        if (tokens[0].equals("/ALERT") && tokens[1].equals("/NICKNAME_SUCCESS")) {
                            dispose();
                            FrmChat chat = new FrmChat(username, socket, in, out);
                            chat.setVisible(true);
                            break;
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

    private void Init() {
        JLabel jLabel3 = new JLabel();
        JTextField txtNickname = new JTextField();
        JButton btnSubmit = new JButton();
        JLabel jLabel1 = new JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel3.setText("Nickname:");

        btnSubmit.setText("OK");

        btnSubmit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSubmitActionPerformed(evt);
            }

            private void btnSubmitActionPerformed(ActionEvent evt) {
                //check if nickname is valid
                //if valid, set username
                //if invalid, show error message
                username = txtNickname.getText();
                if (username.equals("")) {
                    JOptionPane.showMessageDialog(null, "Please enter a nickname");
                } else {
                    try {
                        socket = new Socket("localhost", 9999);
                        in = new BufferedReader(new java.io.InputStreamReader(socket.getInputStream()));
                        out = new BufferedWriter(new java.io.OutputStreamWriter(socket.getOutputStream()));

                        listenForMessage();

                        out.write("/SEND_NICKNAME;" + username);
                        out.newLine();
                        out.flush();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        jLabel1.setText("Enter your nickname");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addGap(122, 122, 122)
                                                .addComponent(jLabel1))
                                        .addGroup(layout.createSequentialGroup()
                                                .addGap(14, 14, 14)
                                                .addComponent(jLabel3)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(txtNickname, javax.swing.GroupLayout.PREFERRED_SIZE, 198, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(btnSubmit)))
                                .addContainerGap(19, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGap(17, 17, 17)
                                .addComponent(jLabel1)
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel3)
                                        .addComponent(txtNickname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(btnSubmit))
                                .addContainerGap(32, Short.MAX_VALUE))
        );

        pack();
        setVisible(true);
    }
}
