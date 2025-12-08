import models.Billete;
import models.Usuario;
import models.Transaccion;


import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
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
    private static int opc;


    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        try {
            String menu = "1.Registro\n2.Iniciar sesion\n3.Comprar billetes\n4.Consultar billetes\n5.Logout\n6.Salir";
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


            opc = -1;

            while(opc != 6){
                System.out.println(menu + "\n  - Ingrese una opcion:");
                if (!sc.hasNextInt()) {
                    System.err.println("Introduce un numero entero");
                    sc.nextLine();
                    continue;
                }
                opc = sc.nextInt();
                sc.nextLine();



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
                out.writeObject(opc);
                out.flush();


                switch(opc){
                    case 1:
                        registrarse();
                        break;
                    case 2:
                        login();
                        break;
                    case 3:
                        comprar(privada);
                        break;
                    case 4:
                            Billete[] billetes = (Billete[]) in.readObject();
                            if(billetes!=null){
                                System.out.println("******* Lista de billetes: *******");
                                for(Billete b : billetes){
                                    System.out.println(b.toString());
                                }
                            }
                        break;
                    case 5:
                        logueado = false;
                        break;
                    case 6:
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

    public static void comprar(PrivateKey privada) {
        try {
            Billete[] billetes = (Billete[]) in.readObject();
            if (billetes != null) {
                System.out.println("******* Selecciona un billete *******");
                for (int i = 0; i < billetes.length; i++) {
                    System.out.println((i + 1) + " - " + billetes[i]);
                }
            }

            Scanner sc = new Scanner(System.in);
            System.out.println("Introduzca el número del billete a comprar:");
            int indice = sc.nextInt();

            Billete seleccionado = billetes[indice - 1];
            if (seleccionado.getPlazasDisponibles() == 0) {
                System.err.println("No hay plazas disponibles");
                out.writeObject("CANCEL");
                out.flush();
                return;
            }

            // Enviar inicial OK
            out.writeObject("OK");
            out.flush();

            // Firmar billete
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(seleccionado);
            oos.flush();
            byte[] mensajeBytes = bos.toByteArray();

            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initSign(privada);
            sig.update(mensajeBytes);
            byte[] firma = sig.sign();

            boolean comprado = false;
            while (!comprado) {
                out.writeObject(seleccionado);
                out.writeObject(firma);
                out.flush();
                System.out.println("Billete y firma enviados al servidor");

                Object resp = in.readObject();
                if (resp instanceof Transaccion) {
                    Transaccion transaccion = (Transaccion) resp;
                    System.out.println("Resultado de la compra: " + transaccion);
                    comprado = true;
                } else if ("OTRO_CLIENTE_ESPERANDO".equals(resp)) {
                    System.out.println("Otro cliente está comprando este billete. Esperando turno...");
                    System.out.println(opc);

                    // Enviar algo para mantener flujo
                    out.writeObject(opc); // dummy
                    out.writeObject("OK");
                    out.flush();
                    Thread.sleep(1000);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
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
                out.writeObject("ERROR");
                out.flush();
                for (String error : errores) {
                    System.err.println(error);
                }
                return;
            } else {

                out.writeObject("OK");
                out.flush();

                Usuario u = new Usuario(nombre, apellido, edad, email, usuario, contrasenaHasheada);

                out.writeObject(u);
                out.flush();

                System.out.println("Datos enviados cifrados correctamente.");
                String respuesta = (String) in.readObject();
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
        } catch (ClassNotFoundException e) {
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

            //cifrar usuario con la clave publica del servidor
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, publicaServidor);
            //cifrarlo en un array de bytes
            byte[] usuarioCifrado = cipher.doFinal(usuario.getBytes());

            // Hasheamos la contraseña ingresada
            String contrasenaHasheada = hashContrasena(passwd);
            //out.writeUTF(usuario);
            out.writeObject(usuarioCifrado);
            out.writeUTF(contrasenaHasheada);
            out.flush();

            //leer respuesta
            String respuesta = (String) in.readObject();

            if(respuesta.equals("200")){
                System.out.println("Login exitoso");
                logueado = true;
            }else if(respuesta.equals("400")){
                System.err.println("Usuario no existente");
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
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (NoSuchPaddingException e) {
            throw new RuntimeException(e);
        } catch (IllegalBlockSizeException e) {
            throw new RuntimeException(e);
        } catch (BadPaddingException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
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
