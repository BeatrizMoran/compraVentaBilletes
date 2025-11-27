import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Servidor {
    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(5555);
            while(true) {
                Socket cliente = serverSocket.accept();
                System.out.println("cliente activo ");
                new HiloCliente(cliente).start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
