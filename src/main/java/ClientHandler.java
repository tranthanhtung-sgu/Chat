import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Random;

public class ClientHandler extends Thread {
    protected Socket socket;
    BufferedReader in;
    BufferedWriter out;
    String username;
    boolean isChatting = false;
    boolean accepted = false;
    static ArrayList<ClientHandler> clients = new ArrayList<>();
    static ArrayList<Room> rooms = new ArrayList<>();

    public ClientHandler(Socket socket) {
        try {
            this.socket = socket;
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            e.printStackTrace();
            closeAll(socket, in, out);
        }
    }

    private boolean checkUsername(String username) {
        for (ClientHandler client : clients) {
            if (client.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }

    private void joinRoom(ClientHandler client) {
        /* Nếu số phòng khác 0 */
        if (rooms.size() != 0) {
            /* thì kiểm tra xem có phòng nào còn thiếu 1 slot không */
            for (Room room : rooms) {
                /* nếu có thì ghép cập với phòng đó và gữi tin yêu cầu ghép cập đén cả 2 */
                if (room.getNumberOfClients() < room.getMaxNumberOfClients()) {
                    room.addClient(client);
                    sendMessageTo2User("/ALERT;/ROOM_FOUND;"+ "Đã tìm thấy phòng, bạn có muốn tham gia ?" , room);
                    System.out.println("có 2 client để ghép cập là " + room.getClients()[0].getUsername() + " và " + room.getClients()[1].getUsername());
                    break;
                    /* nếu không thì tạo phòng mới cho người dùng này */
                } else {
                    Room newRoom = new Room();
                    newRoom.addClient(client);
                    rooms.add(newRoom);
                }
            }
        } else {
            Room room = new Room();
            room.addClient(client);
            rooms.add(room);
        }
        System.out.println("Số phòng hiện tại là " + rooms.size());
    }

    private void sendMessageTo2User(String s, Room room) {
        for (ClientHandler client : room.getClients()) {
            try {
                client.out.write(s + "\n");
                client.out.flush();
            } catch (IOException e) {
                e.printStackTrace();
                closeAll(socket, in, out);
            }
        }
    }

    public void run() {
        String line = "";
        while (true) {
            try {
                line = in.readLine();
                if (line == null) {
                    closeAll(socket, in, out);
                    break;
                }
                System.out.println("Client " + socket.getInetAddress() + ": " + line);
                handleClientCommand(line);
            } catch (Exception e) {
                e.printStackTrace();
                closeAll(socket, in, out);
                break;
            }
        }
    }

    private void handleClientCommand(String line) {
        String[] tokens = line.split(";");
        String command = tokens[0];
        switch (command) {
            case "/SEND_NICKNAME":
                if (checkUsername(tokens[1])) {
                    sendMessage("/ALERT;/NICKNAME_ERROR;" + "Tên này đã được sử dụng");
                } else {
                    sendMessage("/ALERT;/NICKNAME_SUCCESS");
                    this.username = tokens[1];
                    clients.add(this);
                    joinRoom(this);
                }
                break;
            case "/ACCEPT":
                setAccepted(true);
                /* Kiểm tra phòng đã accept = true hết chưa */
                if (getCurrentRoom().getAccepted()) {
                    /* Nếu đã accept hết thì gửi tin cho 2 người chơi */
                    sendDiffMessageTo2User(getCurrentRoom());
                }
                break;
            case "/DECLINE":
                setAccepted(false);
                /* Kiểm tra số lượng người trong phòng của mình hiện tại */
                /* Nếu là 2 thì thoát khỏi phòng đó và tạo một phòng mới */
                if (getCurrentRoom().getNumberOfClients() == 2) {
                    Room room = getCurrentRoom();
                    room.removeClient(this);

                    /* gữi thông báo đến người còn lại là người choi kia rằng tôi đã thoát */
                    ClientHandler clientHandler = room.users.get(0);
                    clientHandler.sendMessage("/ALERT;/ROOM_DECLINED");

                    /* Tạo phòng mới và vào chờ */
                    Room newRoom = new Room();
                    newRoom.addClient(this);
                    rooms.add(newRoom);
                } else if (getCurrentRoom().getNumberOfClients() == 1) {
                    /* Nếu là 1 thì giữ nguyên */
                }
                System.out.println("Số phòng hiện tại là " + rooms.size());
                break;
            case "/SEND_MESSAGE":
                sendMessageTo2User("/MESSAGE;" + tokens[1] , getCurrentRoom());
                break;
            default:
                break;
        }
    }

    private void sendDiffMessageTo2User(Room currentRoom) {
        ClientHandler client1 = currentRoom.getClients()[0];
        ClientHandler client2 = currentRoom.getClients()[1];
        try {
            client1.out.write("/ALERT;/ROOM_ACCEPTED;" + client2.getUsername() + "\n");
            client1.out.flush();

            client2.out.write("/ALERT;/ROOM_ACCEPTED;" + client1.getUsername() + "\n");
            client2.out.flush();
        } catch (Exception e) {
            e.printStackTrace();
            closeAll(socket, in, out);
        }
    }

    private Room getCurrentRoom() {
        for (Room room : rooms) {
            if (room.getNumberOfClients() == 2) {
                if (room.getClients()[0].equals(this) || room.getClients()[1].equals(this)) {
                    return room;
                }
            } else if (room.getNumberOfClients() == 1) {
                if (room.getClients()[0].equals(this)) {
                    return room;
                }
            }
        }
        return null;
    }

    private void sendMessage(String line) {
        try {
            out.write(line + "\n");
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
            closeAll(socket, in, out);
        }
    }

    private void broadcastMessage(String line) {
        for (ClientHandler client : clients) {
            try {
                if (client != this) {
                    client.out.write(line + "\n");
                    client.out.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
                closeAll(socket, in, out);
            }
        }
    }

    private void closeAll(Socket socket, BufferedReader in, BufferedWriter out) {
        try {
            socket.close();
            in.close();
            out.close();
            removeClient();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void removeClient() {
        clients.remove(this);
        Room room = getCurrentRoom();
        if (room != null) {
            room.removeClient(this);
            if (room.getNumberOfClients() > 0) {
                HandleUserLeft(room);
            }
            if (room.getClients().length == 0) {
                rooms.remove(room);
            }
        }
        System.out.println("Số phòng hiện tại là " + rooms.size());
    }

    private void HandleUserLeft(Room room) {
        ClientHandler client = room.users.get(0);
        try {
            client.setAccepted(false);

            client.out.write("/ALERT;/OTHER_USER_QUIT");
            client.out.newLine();
            client.out.flush();
        } catch (IOException e) {
            e.printStackTrace();
            closeAll(socket, in, out);
        }
    }

    public boolean isChatting() {
        return isChatting;
    }

    public void setChatting(boolean chatting) {
        isChatting = chatting;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }
}
