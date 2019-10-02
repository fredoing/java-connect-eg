/**
 * Esta clase se encarga de conectarse al backend server
 * para interactuar con la base de datos, tanto en restaurantes
 * como en usuarios.
 * @author Alfredo Campos
 * @version 1.0
 * @since 25-09-2019
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Connector {

    private static String serverUrl = "https://moviles1-db.herokuapp.com/";
    public static int BARATO = 1;
    public static int MEDIO = 2;
    public static int CARO = 3;

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    private static JSONArray readJsonFromUrl(String url) throws IOException, JSONException {
        url = url.replaceAll(" ", "%20");
        InputStream is = null;
        try {
            URL tempurl = new URL(url);
            URLConnection conn = tempurl.openConnection();
            is = conn.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            JSONArray json = new JSONArray(jsonText);
            return json;
        } catch (Exception e) {
            JSONArray jsonArray = new JSONArray("[{\"algo\":\"something\"}]");
            return jsonArray;
        } finally {
            is.close();
        }
    }

    /**
     * Registra un usuario con email en la base de datos
     * @param nombre
     * @param email
     * @param password
     * @return confirmacion de si se agrego el usuario
     */
    public static boolean registraUsuario(String nombre, String email, String password) {
        boolean inserted = false;
        String url = serverUrl+"newUser/"+nombre+"/"+email+"/"+password;
        JSONArray json=null;
        try {
            json = readJsonFromUrl(url);
        } catch (Exception e) {
            inserted = false;
        }
        if (json == null) {
            inserted = false;
        } else if (json.length() == 0) {
            inserted = true;
        }
        return inserted;
    }

    /**
     * Registra un usuario de facebook
     * @param nombre
     * @param email
     * @param id id del usuario proporcionado por facebook
     * @return confirmacion de si se agrego el usuario
     */
    public static boolean registraUsuarioFacebook(String nombre, String email, int id) {
        boolean inserted = false;
        String url = serverUrl+"newFaceUser/"+nombre+"/"+email+"/"+id;
        JSONArray json=null;
        try {
            json = readJsonFromUrl(url);
        } catch (Exception e) {
            inserted = false;
        }
        if (json == null) {
            inserted = false;
        } else if (json.length() == 0) {
            inserted = true;
        }
        return inserted;
    }

    /**
     * Realiza la autenticacion de un usuario con contrasena y correo
     * @param email
     * @param password
     * @return Confirmacion de autenticacion
     */
    public static boolean autenticaUsuario(String email, String password) {
        boolean passed = false;
        String url = serverUrl+"user/"+email+"/"+password;
        JSONArray json=null;
        try {
            json = readJsonFromUrl(url);
        } catch (Exception e) {
            passed = false;
        }
        if (json == null) {
            passed = false;
        } else if (json.length() > 0) {
            try {
                JSONObject jobject = json.getJSONObject(0);
                if (jobject.getBoolean("passed")) {
                    passed = true;
                }
            } catch (Exception e) {

            }
        }
        return passed;
    }

    /**
     * Autentica un usuario de facebook
     * @param email
     * @param id
     * @return Confirmacion de autenticacion
     */
    public static boolean autenticaUsuarioFacebook(String email, int id) {
        boolean passed = false;
        String url = serverUrl+"facebookuser/"+email+"/"+id;
        JSONArray json=null;
        try {
            json = readJsonFromUrl(url);
        } catch (Exception e) {
            passed = false;
        }
        if (json == null) {
            passed = false;
        } else if (json.length() > 0) {
            try {
                JSONObject jobject = json.getJSONObject(0);
                if (jobject.getBoolean("passed")) {
                    passed = true;
                }
            } catch (Exception e) {

            }
        }
        return passed;
    }

    /**
     * Este metodo busca los restaurantes mas cercanos basado en ubicacion y distancia deseada
     * para tomar los valores hay que iterar sobre el array hacer un get(index) a un JSONObject
     * que tiene las siguientes propiedades: id, nombre, latitud, longitud, contacto, horario,
     * precio y calificacion
     * @param distancia distancia en km a la redonda de la ubicacion
     * @param latitud
     * @param longitud
     * @return JSONArray con los restaurantes(JSONObjects) que cumplen
     */
    public static JSONArray getRestaurantes(int distancia, double latitud, double longitud) {
        String url = serverUrl+"rests/"+distancia+"/"+latitud+"/"+longitud;
        JSONArray json=null;
        try {
            json = readJsonFromUrl(url);
        } catch (Exception e) {

        }
        return json;
    }

    /**
     * Inserta un nuevo restaurante
     * @param nombre nombre del restaurante
     * @param latitud en double
     * @param longitud en double
     * @param contacto es un string
     * @param horario es un string
     * @param precio una variable BARATO, MEDIO, CARO
     * @return confirmacion de agregar el restaurante.
     */
    public static boolean newRestaurante(String nombre, double latitud, double longitud, String contacto,
                                         String horario, int precio) {
        boolean inserted = false;
        String url = serverUrl+"newrest/"+nombre+"/"+latitud+"/"+longitud+"/"+contacto
                +"/"+horario+"/"+precio;
        JSONArray json=null;
        try {
            json = readJsonFromUrl(url);
        } catch (Exception e) {
            inserted = false;
        }
        if (json == null) {
            inserted = false;
        } else if (json.length() == 0) {
            inserted = true;
        }
        return inserted;
    }

    private static int getUserId(String email) {
        String url = serverUrl+"userid/"+email;
        int result = -1;
        JSONArray json=null;
        try {
            json = readJsonFromUrl(url);
            JSONObject jobject = json.getJSONObject(0);
            result = jobject.getInt("id");
        } catch (Exception e) {

        }
        return result;
    }

    /**
     * Inserta un comentario de un restaurante
     * @param idrest id del restaurante
     * @param email correo del usuario
     * @param comentario
     * @return confirmacion de que se agrego el comentario
     */
    public static boolean comentar(int idrest, String email, String comentario) {
        boolean inserted = false;
        int idusuario = getUserId(email);
        String url = serverUrl+"comment/"+idrest+"/"+idusuario+"/"+comentario;
        JSONArray json=null;
        try {
            json = readJsonFromUrl(url);
        } catch (Exception e) {
            inserted = false;
        }
        if (json == null) {
            inserted = false;
        } else if (json.length() == 0) {
            inserted = true;
        }
        return inserted;
    }

    /**
     * Inserta una calificacion que el usuario le da a un restaurante
     * @param idrest id restarante
     * @param email
     * @param estrellas puntuacion de 1 a 5
     * @return confirmacion de insercion de la calificacion
     */
    public static boolean calificar(int idrest, String email, int estrellas) {
        boolean inserted = false;
        int idusuario = getUserId(email);
        String url = serverUrl+"califica/"+idrest+"/"+idusuario+"/"+estrellas;
        JSONArray json=null;
        try {
            json = readJsonFromUrl(url);
        } catch (Exception e) {
            inserted = false;
        }
        if (json == null) {
            inserted = false;
        } else if (json.length() == 0) {
            inserted = true;
        }
        return inserted;
    }

    /**
     * lista los comentarios para un restaurante, trae una JSONArray
     * con estos comentarios cada una es un JSONObject con dos valores
     * nombre del usuario y el comentario ambos como: nombre, comentario
     * @param idrest id del restaurante.
     * @return JSONArray con los comentarios para un restaurante
     */
    public static JSONArray getComments(int idrest) {
        String url = serverUrl+"comments/"+idrest;
        JSONArray json=null;
        try {
            json = readJsonFromUrl(url);
        } catch (Exception e) {

        }
        return json;
    }


    /**
     * Envia una solicitud de cambio de contrasena
     * @param mail String
     * @return boolean con verificacion de si se envio el correo
     */
    public static boolean recoverPassword(String mail) {
        String url = serverUrl+"recover/"+mail;
        boolean enviado = false;
        JSONArray json=null;
        try {
            json = readJsonFromUrl(url);
            JSONObject jsono = json.getJSONObject(0);
            if (jsono.getBoolean("enviado")) {
                enviado = true;
            }
        } catch (Exception e) {

        }
        return enviado;
    }

    public static void main(String[] args) {

    }
}
