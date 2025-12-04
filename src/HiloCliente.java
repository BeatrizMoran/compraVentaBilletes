import models.Usuario;

import javax.crypto.Cipher;
import java.io.*;
import java.net.Socket;
import java.security.*;
import java.util.*;

public class HiloCliente extends Thread {
    private  ObjectOutputStream salida;
    private ObjectInputStream entrada;

    private static Map<String, Usuario> usuarios = new HashMap<>();


    private Socket cliente;
    public static Billete[] billetes = {
            new Billete("Madrid", "Barcelona", 45.50, 12),
            new Billete("Sevilla", "Madrid", 38.00, 8),
            new Billete("Valencia", "Bilbao", 50.25, 5),
            new Billete("Zaragoza", "Málaga", 32.10, 10),
            new Billete("Madrid", "Lisboa", 80.00, 7)
    };

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

            salida = new ObjectOutputStream(cliente.getOutputStream());
            entrada = new ObjectInputStream(cliente.getInputStream());


            //intercambiar claves
            PublicKey publicaUsuario = (PublicKey) entrada.readObject();
            salida.writeObject(publica);
            salida.flush();

                while(true){
                    int opcion = entrada.readInt();

                    switch(opcion){
                        case 1:
                            registro();
                            break;
                        case 2:
                            login();
                            break;
                        case 3:
                            break;
                        case 4:
                            salida.writeObject(billetes);
                            salida.flush();
                            break;
                    }
                }
            //cliente.close();
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

    public static void listarBilletes(){

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

    public void login(){
        try {
            String usuario = entrada.readUTF();
            String contrasenaHasheada = entrada.readUTF();

            if (usuarios.containsKey(usuario)) {
                    System.out.println("Usuario existente");
                    Usuario u = usuarios.get(usuario);

                    System.out.println("usuario encontrado: " + u.getNombre());
                    boolean passwdCorrecta =  contrasenaHasheada.equals(u.getPasswd());

                    if (passwdCorrecta) {
                        System.out.println("Login correctamente");
                        salida.writeUTF("200");
                        salida.flush();

                    } else {
                        System.err.println("Login incorrecto");
                        salida.writeUTF("500");
                        salida.flush();
                    }

            }else{
                System.err.println("Usuario no encontrado, ingrese un usuario valido");
                salida.writeUTF("400");
                salida.flush();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void registro(){
        try {
            Usuario u = (Usuario) entrada.readObject();

            if (usuarios.containsKey(u.getUsuario())) {
                salida.writeUTF("500");
                salida.flush();

            }else{
                usuarios.put(u.getUsuario(), u);
                salida.writeUTF("200");
                salida.flush();

            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }



}
