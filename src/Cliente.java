import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Cliente {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        try {
            String menu = "1.Registro\n2.Iniciar sesion\n3.Comprar billetes\n4.Consultar billetes\n5.Salir";
            Socket cliente = new Socket("localhost", 5555);

            DataOutputStream salida = new DataOutputStream(cliente.getOutputStream());
            ObjectOutputStream out = new ObjectOutputStream(cliente.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(cliente.getInputStream());

            int opc = -1;
            System.out.println(menu + "\n  - Ingrese una opcion:");

            while(opc != 5){
                opc = sc.nextInt();
                salida.writeInt(opc);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }





}
