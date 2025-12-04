import models.Usuario;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Cliente {
    private static Map<String, Usuario> usuarios = new HashMap<>();
    private static boolean logueado = false;
    private static PublicKey publicaServidor;
    private static ObjectOutputStream out;
    private static ObjectInputStream in;


    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        try {
            String menu = "1.Registro\n2.Iniciar sesion\n3.Comprar billetes\n4.Consultar billetes\n5.Salir";
            Socket cliente = new Socket("localhost", 5555);

            out = new ObjectOutputStream(cliente.getOutputStream());
            in = new ObjectInputStream(cliente.getInputStream());

            //generamos las claves del cliente
            KeyPairGenerator clavecliente = KeyPairGenerator.getInstance("RSA");

            System.out.println("Generando par de claves");
            KeyPair par = clavecliente.generateKeyPair();
            PrivateKey privada=par.getPrivate();
            PublicKey publica=par.getPublic();
            //mandamos la clave del cliente al servidor
            out.writeObject(publica);
            out.flush();


            //clave publica server
            publicaServidor = (PublicKey) in.readObject();


            int opc = -1;

            while(opc != 5){
                System.out.println(menu + "\n  - Ingrese una opcion:");
                if (!sc.hasNextInt()) {
                    System.err.println("Introduce un numero entero");
                    sc.nextLine();
                    continue;
                }
                opc = sc.nextInt();
                sc.nextLine();

                out.writeInt(opc);
                out.flush();


                if(!logueado){
                    if(opc==3 || opc == 4){
                        System.err.println("Tienes que estar logueado");
                        continue;
                    }
                }

                if(opc == 1 || opc == 2){
                    if(logueado){
                        System.err.println("Ya estas logueado");
                        continue;
                    }
                }

                System.out.println("Opcion seleccionada: " + opc);


                switch(opc){
                    case 1:
                        registrarse();
                        break;
                    case 2:
                        login();
                        break;
                    case 3:
                        break;
                    case 4:
                            Billete[] billetes = (Billete[]) in.readObject();
                            System.out.println("******* Lista de billetes: *******");
                            for(Billete b : billetes){
                                System.out.println(b.toString());
                            }

                        break;
                    case 5:
                        System.out.println("saliendo del programa");
                        System.exit(0);
                        break;
                    default:
                        System.err.println("Opcion incorrecta");
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

    public static void registrarse(){
        try {

            List<String> errores = new ArrayList<>();

            Scanner sc = new Scanner(System.in);
            System.out.println("Registrando usuario...");
            System.out.println("Nombre");
            String nombre = sc.nextLine();
            System.out.println("Apellido");
            String apellido = sc.nextLine();
            System.out.println("edad");
            int edad = 0;
            if (!sc.hasNextInt()) {
                errores.add("Edad incorrecta, ingrese una edad valida");
            } else {
                edad = sc.nextInt();

            }
            sc.nextLine();
            System.out.println("Email");
            String email = sc.nextLine();
            Pattern pat = Pattern.compile(".+@.+\\..+");
            if (!validarPatron(pat, email)) {
                errores.add("email incorrecto, ingrese un email valido");
            }

            System.out.println("Usuario:");
            String usuario = sc.nextLine();
            if (!validarPatron(Pattern.compile("^[a-zA-Z0-9]{6}"), usuario)) {
                errores.add("usuario incorrecto, minimo 6 caracteres");

            }

            System.out.println("Contraseña:");
            String passwd = sc.nextLine();
            if (!validarPatron(Pattern.compile("[a-zA-Z0-9]{8,}"), passwd)) {
                errores.add("Contraseña incorrecta, minimo 8 caracteres");
            }
            String contrasenaHasheada = hashContrasena(passwd);


            if (!errores.isEmpty()) {
                for (Object error : errores) {
                    System.err.println(error);
                }
            } else {

                Usuario u = new Usuario(nombre, apellido, edad, email, usuario, contrasenaHasheada);


                out.writeObject(u);
                out.flush();


                System.out.println("Datos enviados cifrados correctamente.");
                String respuesta = in.readUTF();
                if(respuesta.equals("200")){
                    System.out.println("Usuario registrado correctamente: " + u);
                }else{
                    System.err.println("Error resgistrando usuario.- Usuario ya existente");

                }

            }
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static boolean validarPatron(Pattern patron, String valor){
        Matcher mat = patron.matcher(valor);
        return mat.find();
    }

    public static void login(){
        try {
            Scanner sc = new Scanner(System.in);
        System.out.println("Iniciando login...");
        System.out.println("Usuario:");
        String usuario = sc.nextLine();
        System.out.println("Contraseña:");
        String passwd = sc.nextLine();

        // Hasheamos la contraseña ingresada y la comparamos con la almacenada
            String contrasenaHasheada = hashContrasena(passwd);
            out.writeUTF(usuario);

            out.writeUTF(contrasenaHasheada);
            out.flush();


            //leer respuesta
            String respuesta = in.readUTF();

            if(respuesta.equals("200")){
                System.out.println("Login exitoso");
                logueado = true;
            }else if(respuesta.equals("400")){
                System.out.println("Usuario no existente");
                logueado = false;
            }
            else{
                System.err.println("Error, login incorrecto");
                logueado = false;
            }


        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }




    }

    public static String hashContrasena(String contrasena) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = md.digest(contrasena.getBytes());

        // Convertir el hash a una cadena hexadecimal
        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));//convierte a hexadecimal en minuscula y con dos posiciones, 0 a la izquierda
        }
        return sb.toString();
    }



}
