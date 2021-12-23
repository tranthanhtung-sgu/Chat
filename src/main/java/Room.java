import java.util.ArrayList;

public class Room {
    ArrayList<ClientHandler> users = new ArrayList<>(2);

    public Room() {
    }

    public void addClient(ClientHandler clientHandler) {
        users.add(clientHandler);
    }

    public int getNumberOfClients() {
        return users.size();
    }

    public int getMaxNumberOfClients() {
        return 2;
    }

    public ClientHandler[] getClients() {
        return users.toArray(new ClientHandler[users.size()]);
    }

    public void removeClient(ClientHandler clientHandler) {
        users.remove(clientHandler);
    }

    public boolean getAccepted() {
        for (ClientHandler clientHandler : users) {
            if (!clientHandler.isAccepted()) {
                return false;
            }
        }
        return true;
    }

    public ClientHandler GetRestUser(ClientHandler clientHandler) {
        for (ClientHandler user : users) {
            if (user != clientHandler) {
                return user;
            }
        }
        return null;
    }
}


