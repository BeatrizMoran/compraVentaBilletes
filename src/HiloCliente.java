import javax.crypto.Cipher;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HiloCliente extends Thread {
    private Socket cliente;


    public HiloCliente(Socket cliente) {
        this.cliente = cliente;
    }

    @Override
    public void run() {
        try{
            //Generar par de claves
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            System.out.println("Generando claves...");
            KeyPair keys = keyGen.generateKeyPair();
            PrivateKey privada  = keys.getPrivate();
            PublicKey publica  = keys.getPublic();

            ObjectOutputStream salida = new ObjectOutputStream(cliente.getOutputStream());
            ObjectInputStream entrada = new ObjectInputStream(cliente.getInputStream());

            //intercambiar claves
            salida.writeObject(publica);
            PublicKey publicaUsuario = (PublicKey) entrada.readObject();



            int opc = -1;

            while(opc != 5){
                byte[] opcion = (byte[]) entrada.readObject();
                System.out.println("Opcion seleccionada: " + new String(opcion));
                opc = Integer.parseInt(descifrar(opcion, privada));

                switch(opc){
                    case 1:
                        registrarse();
                        break;
                    case 5:
                        System.out.println("saliendo del programa");
                        System.exit(0);
                        break;
                }
            }
            cliente.close();
        }catch (IOException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void comprarBilletes(){

    }

    public void registrarse(){
        List<String> errores = new ArrayList<>();

        Scanner sc = new Scanner(System.in);
        System.out.println("Registrando usuario...");
        System.out.println("Nombre");
        String nombre = sc.nextLine();
        System.out.println("Apellido");
        String apellido = sc.nextLine();
        System.out.println("edad");
        if(!sc.hasNextInt()){
            errores.add("Edad incorrecta, ingrese una edad valida");
        }
        int edad = sc.nextInt();
        System.out.println("Email");
        String email = sc.nextLine();
        Pattern pat = Pattern.compile(".+@.+\\..+");
        if(!validarPatron(pat, email)){
            errores.add("email incorrecto, ingrese un email valido");
        }

        System.out.println("Usuario:");
        String usuario = sc.nextLine();
        if(!validarPatron(Pattern.compile("^[a-zA-Z]{6}"), usuario)){
            errores.add("usuario incorrecto, minimo 6 caracteres");

        }

        System.out.println("Contraseña:");
        String passwd = sc.nextLine();
        if(!validarPatron(Pattern.compile("[a-zA-Z0-9]{8,}"), passwd)){
            errores.add("Contraseña incorrecta, minimo 8 caracteres");
        }

        if(!errores.isEmpty()){
            for(Object error: errores){
                System.err.println(error);
            }
        }


    }

    public static String descifrar(byte[] msg, PrivateKey clave) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, clave);
        return new String(cipher.doFinal(msg));
    }

    public static byte[] cifrar(String msg, PublicKey clave) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, clave);
        return cipher.doFinal(msg.getBytes());
    }

    public static boolean validarPatron(Pattern patron, String valor){
        Matcher mat = patron.matcher(valor);
        return mat.find();
    }

}
