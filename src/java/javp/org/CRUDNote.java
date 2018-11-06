/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javp.org;

import DataBase.DBConnection;
import DataBase.PropsManager;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Julio Avila
 */
@WebServlet(name = "CRUDNote", urlPatterns = {"/CRUDNote"})
public class CRUDNote extends HttpServlet {

    protected JSONObject myjson;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            read(DBConnection.getConnection(), request, response);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(SignUp.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if(request.getHeader("Origin") != null) { 
        System.out.println("Recibi un header");
        myjson = new JSONObject(request.getReader().lines().collect(Collectors.joining(System.lineSeparator())));
            try {
                add(DBConnection.getConnection(), request, response);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(SignUp.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SQLException ex) {
                Logger.getLogger(CRUDNote.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
      else {
          System.out.println("Ocurrio un error");
      }
    }
    
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            delete(DBConnection.getConnection(), request, response);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(SignUp.class.getName()).log(Level.SEVERE, null, ex);
        }   
    }
    
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        myjson = new JSONObject(request.getReader().lines().collect(Collectors.joining(System.lineSeparator())));
            try {
                update(DBConnection.getConnection(), request, response);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(SignUp.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SQLException ex) {
                Logger.getLogger(CRUDNote.class.getName()).log(Level.SEVERE, null, ex);
            }

    }
    
    private void add(Connection connection, HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
        System.out.println("entrando a metodo add para agregar board");
        response.setContentType("aplication/json");
        PropsManager props = PropsManager.getInstance();
        PrintWriter out = response.getWriter();
        PreparedStatement stmt = null;
        String createnote = props.getProps("createnote");
        HttpSession mySession;
        //Importamos los campos
        String title = myjson.getString("note_title");
        String content = myjson.getString("note_content");
        Integer user_id = myjson.getInt("user_id");
        try{
            mySession = request.getSession();
            stmt = connection.prepareStatement(createnote);
            //Datos para la tabla Board
            stmt.setInt(1, user_id);
            stmt.setString(2, title);
            stmt.setString(3, content);
            System.out.println("Este es el query del createnote ---->"+stmt.toString());        
            stmt.executeUpdate();
            System.out.println("Agregado con exito a la Base de datos");
            myjson.put("status", 200).put("url","html/dashboard.html");
            //Datos para el userboard
        }
        catch(SQLException | JSONException e){
            System.out.println("Error al conectar..."+e.getMessage());
            myjson.put("status", 404).put("url", "html/register.html");
        }//Final del catch
        out.print(myjson.toString());	       
    }
    
    private void update(Connection connection, HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
        PropsManager myprops = PropsManager.getInstance();
        PreparedStatement mySt = null;
        String updatenote = myprops.getProps("updatenote");
        PrintWriter out = response.getWriter();

        try {  
            mySt = connection.prepareStatement(updatenote);
            mySt.setString(1, myjson.getString("note_title"));
            mySt.setString(2, myjson.getString("note_content"));
            mySt.setInt(3, myjson.getInt("note_id"));
            
            mySt.executeUpdate(); //use if no data will be returned... else use, executeQuery();
            System.out.println("AGREGADO A LA BBDD CON ÉXITO");

            myjson.put("status", 200);

        } catch (SQLException | JSONException e) {
            System.out.println("ERROR AL CONECTAR... -> " + e.getMessage());
            myjson.put("success", false).put("url", "index.html");
        }

        out.print(myjson.toString());	       
    }

    private void read(Connection connection, HttpServletRequest request, HttpServletResponse response) throws IOException {		   
        System.out.println("entrando a metodo leer nota");
        response.setContentType("aplication/json");
        PropsManager props = PropsManager.getInstance();
        PrintWriter out = response.getWriter();
        JSONArray myjsonarray = new JSONArray();
        PreparedStatement stmt = null;
        String getnote = props.getProps("getnote");
        ResultSetMetaData rsmd = null;
        //Importamos el id del usuario para realizar la consulta
        Integer user = Integer.parseInt(request.getParameter("user_id"));

        try{
            stmt = connection.prepareStatement(getnote);
            stmt.setInt(1, user);
            System.out.println("Este es el query del read column---->"+stmt.toString()); 
            ResultSet rs = stmt.executeQuery();
            rsmd = rs.getMetaData();//Importamos la Meta Data
            while(rs.next()){
                String content = rs.getString("note_content");
                JSONObject json = new JSONObject();
                for (int i = 1; i < rsmd.getColumnCount(); i++) {
                    json.put(rsmd.getColumnLabel(i), rs.getObject(i)).put("note_content", content).put("status", 200);
                    System.out.println("JSON->"+json);
                }
                myjsonarray.put(json);
                System.out.println("JSONArray ----->"+myjsonarray);
            }
	            
            out.print(myjsonarray);
	}//Final del try
        catch(SQLException | JSONException e){
            System.out.println("Error ... -> " + e.getMessage());
        }		
    }

    private void delete(Connection connection, HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("entrando a metodo add para agregar board");
        response.setContentType("aplication/json");
        JSONObject json = new JSONObject();
        PropsManager props = PropsManager.getInstance();
        PrintWriter out = response.getWriter();
        PreparedStatement stmt = null;
        String deletenote = props.getProps("deletenote");
        HttpSession mySession;
        //Importamos los campos
        Integer note_id = Integer.parseInt(request.getParameter("note_id"));
        try{
            mySession = request.getSession();
            stmt = connection.prepareStatement(deletenote);
            stmt.setInt(1, note_id);
            System.out.println("Este es el query del delete ---->"+stmt.toString());        
            stmt.executeUpdate();
            System.out.println("Eliminado con exito a la Base de datos");
            json.put("status", 200);
            //Datos para el userboard
        }
        catch(SQLException | JSONException e){
            System.out.println("Error al conectar..."+e.getMessage());
            json.put("status", 404).put("url", "html/register.html");
        }//Final del catch
        out.print(myjson.toString());        
    }

}
