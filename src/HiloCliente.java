import models.Billete;
import models.Usuario;
import models.Transaccion;


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
                            //mandar billetes
                            salida.writeObject(billetes);
                            salida.flush();
                            comprarBilletes(publicaUsuario);
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

    public synchronized void comprarBilletes(PublicKey publicaUsuario) {
        try {
            // 1. Recibir objeto y firma
            Billete billeteRecibido = (Billete) entrada.readObject();
            byte[] firmaCliente = (byte[]) entrada.readObject();

            // 2. Convertir a bytes para verificar firma
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(billeteRecibido);
            oos.flush();
            byte[] mensajeBytes = bos.toByteArray();

            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initVerify(publicaUsuario);
            sig.update(mensajeBytes);
            boolean valida = sig.verify(firmaCliente);

            // 3. Crear transacción
            Transaccion transaccion = new Transaccion(billeteRecibido);

            if (valida) {
                transaccion.setEstado("EXITOSA");
                System.out.println("Compra verificada correctamente: " + billeteRecibido);
                // Buscar el billete original en el servidor
                for (Billete b : billetes) {
                    if (b.equals(billeteRecibido)) {
                        if (b.getPlazasDisponibles() > 0) {
                            b.setPlazasDisponibles(b.getPlazasDisponibles() - 1);
                            transaccion.setEstado("EXITOSA");
                            System.out.println("Compra verificada correctamente: " + b);
                        } else {
                            transaccion.setEstado("RECHAZADA");
                            System.err.println("No quedan plazas disponibles para: " + b);
                        }
                        break;
                    }
                }
            } else {
                transaccion.setEstado("RECHAZADA");
                System.err.println("Firma incorrecta, compra rechazada");
            }

            // 4. Enviar transacción al cliente
            salida.writeObject(transaccion);
            salida.flush();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
                        salida.writeObject("200");
                        salida.flush();

                    } else {
                        System.err.println("Login incorrecto");
                        salida.writeObject("500");
                        salida.flush();
                    }

            }else{
                System.err.println("Usuario no encontrado, ingrese un usuario valido");
                salida.writeObject("400");
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
                salida.writeObject("500");
                salida.flush();

            }else{
                usuarios.put(u.getUsuario(), u);
                salida.writeObject("200");
                salida.flush();

            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }



}
