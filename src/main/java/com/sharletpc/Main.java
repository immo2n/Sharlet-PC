package com.sharletpc;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import javax.swing.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;
import java.util.Scanner;

import static org.apache.commons.net.io.Util.copyStream;

public class Main {
    public static HttpServer main_server;
    public static File bundle_file;
    public static LogControls logControls;
    public static Log log = new Log(null);
    public static String ip = "", parent = "";
    public static File dir;
    public static int port = 1000;

    public static void main(String[] args) {
        //Fields
        Scanner s = new Scanner(System.in);
        try {
            System.out.println("[***WELCOME TO SHARLET PC SERVER***]");
            System.out.println("[You will need your IPV4 address. To get run \"ipconfig\" in terminal]");
            System.out.print("Enter your IPV4 Address: ");
            ip = s.nextLine();
            System.out.print("Enter port to operate (e.g., 1000): ");
            port = s.nextInt();

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select Target Folder");
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int userSelection = fileChooser.showDialog(null, "Select");
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File selectedFolder = fileChooser.getSelectedFile();
                parent = selectedFolder.getAbsolutePath();
                try {
                    log.setVisible(true);
                    log.println("LOG: Running for directory => "+parent);
                    startMain();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                System.out.print("Failed to choose target folder!");
                System.exit(0);
            }

        } catch (Exception e) {
            System.out.print("Wrong info given! Try restarting");
            System.exit(0);
        }
    }

    public static void startMain() throws IOException {
        logControls = () -> {
            log.println("STATUS CHANGE: Server stopped!");
            System.out.println("Stopping...");
            main_server.stop(0);
            bundle_file.delete();
        };
        dir = new File(parent);
        bundle_file = new File(dir + "\\index.html");

        Main main = new Main();

        //Turbo mode simulation
        main_server = HttpServer.create(new InetSocketAddress(ip, port), 0);

        //File bucket
        main_server.createContext("/", main.load_bucket());

        //File contexts
        StringBuilder new_bucket = new StringBuilder("<html><head><title>Sharlet PC Client - files INDEX</title></head><body><p>File index - Click on the links to save the files.</p>");
        File[] files = dir.listFiles();
        assert null != files;
        int flag_run = 0;
        int flag_last = files.length-1;
        for(File file:files){
            String link_random = getRandomFilename(), file_password = getRandomFilename();
            main_server.createContext("/"+link_random, main.load_file_path(file, true, file_password));
            main_server.createContext("/"+link_random, main.load_file_path(file, false, file_password));
            String obj;
            if(flag_run < flag_last) {
                obj = "<a href=\"/" +link_random+ "\" download=\"" +file.getName()+ "\">"+ file.getName() +"</a><br>";
            }
            else {
                obj = "<a href=\"/" +link_random+ "\" download=\"" +file.getName()+ "\">"+ file.getName() +"</a>";
            }
            new_bucket.append(obj);
            flag_run++;
        }
        new_bucket.append("</body></html>"); // Close the array
        PrintWriter writer = new PrintWriter(bundle_file.getPath(), "UTF-8");
        writer.println(new_bucket);
        writer.close();

        main_server.start();

        System.out.println("Sharlet PC server running on: http://"+ip+":"+port+"/");
        log.println("STATUS CHANGE: Sharlet PC server running on: http://"+ip+":"+port+"/");
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter q to stop: ");
        String input = scanner.nextLine();

        if (input.equalsIgnoreCase("q")) {
            log.println("STATUS CHANGE: Server stopped!");
            System.out.println("Stopping...");
            main_server.stop(0);
            bundle_file.delete();
        }
    }

    private HttpHandler load_file_path(File path, boolean is_main_server, String file_password){
        load_file file_loader = new load_file();
        file_loader.setOptions(path, true, is_main_server, file_password);
        return file_loader;
    }

    private class load_file implements HttpHandler {
        File path;
        String password = null;
        Boolean media_file = false, main_server = false;
        public void setOptions(File path_of_file, Boolean is_media, boolean is_main_server, String file_password){
            path = path_of_file;
            media_file = is_media;
            main_server = is_main_server;
            password = file_password;
        }
        public void handle(HttpExchange t) throws IOException {

            //CHECK FOR THE PASSWORD AT PARAM p
            //IF MATCHES, GO AHEAD, otherwise error - ONLY MEDIA FILE
            if(media_file){
                // Read the request body
                /* OMITTED THE PASSWORD CHECK MECHANISM
                InputStreamReader isr = new InputStreamReader(t.getRequestBody());
                BufferedReader br = new BufferedReader(isr);
                StringBuilder requestBody = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    requestBody.append(line);
                }
                br.close();
                isr.close();
                // Extract the value of the 'p' parameter
                String pValue = extractParameterValue(requestBody.toString(), "p");
                if (null == pValue || !pValue.equals(password)) {
                    String response = "DENIED: FAULTY OR MANIPULATED DATA REQUEST!";
                    // Send a response
                    t.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
                    OutputStream responseBody = t.getResponseBody();
                    responseBody.write(response.getBytes(StandardCharsets.UTF_8));
                    responseBody.close();
                    return;
                }
                 */
            }

            File file = path;
            String content_type = "text/html; charset=UTF-8";
            if(!file.exists()){
                String response = "COULDN'T SERVE: FILE IS EMPTY OR DELETED";
                // Send a response
                t.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
                OutputStream responseBody = t.getResponseBody();
                responseBody.write(response.getBytes(StandardCharsets.UTF_8));
                responseBody.close();
                return;
            }
            else {
                String ct = Files.probeContentType(Paths.get(file.getPath()));
                if (null != ct && !ct.isEmpty()) {
                    content_type = ct;
                }
            }

            Headers h = t.getResponseHeaders();
            h.add("Cache-Control", "no-cache");
            h.add("Content-Type", content_type);
            t.sendResponseHeaders(200, file.length());

            FileInputStream fis;

            OutputStream os = t.getResponseBody();

            fis = new FileInputStream(file);
            copyStream(fis, os);
            os.close();
            fis.close();
            t.close();
            log.println("PULL: File sent("+file.length()+" bytes) => "+path);
        }
    }

    private HttpHandler load_bucket(){
        return new http_bucket_handler();
    }
    private class http_bucket_handler implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            Headers h = t.getResponseHeaders();

            //Read request body
            InputStreamReader isr = new InputStreamReader(t.getRequestBody());
            BufferedReader br = new BufferedReader(isr);
            StringBuilder requestBody = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                requestBody.append(line);
            }
            br.close();
            isr.close();

            h.add("Cache-Control", "no-cache");
            h.add("Content-Type", "text/html");
            t.sendResponseHeaders(200, bundle_file.length());
            FileInputStream fis;
            fis = new FileInputStream(bundle_file);
            OutputStream os = t.getResponseBody();
            copyStream(fis, os);
            os.close();
            fis.close();
            log.println("PULL: Index sent and loaded by client");
        }
    }
    //Helper methods
    private static String extractParameterValue(String requestBody, String parameterName) {
        String[] pairs = requestBody.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2 && keyValue[0].equals(parameterName)) {
                return decodeURL(keyValue[1]);
            }
        }
        return null;
    }
    private static String decodeURL(String url) {
        StringBuilder decoded = new StringBuilder();
        char[] chars = url.toCharArray();
        int length = chars.length;
        int i = 0;
        while (i < length) {
            char c = chars[i];
            if (c == '%' && i + 2 < length) {
                char hex1 = Character.toLowerCase(chars[i + 1]);
                char hex2 = Character.toLowerCase(chars[i + 2]);
                int digit1 = Character.digit(hex1, 16);
                int digit2 = Character.digit(hex2, 16);
                if (digit1 != -1 && digit2 != -1) {
                    decoded.append((char) ((digit1 << 4) + digit2));
                    i += 3;
                    continue;
                }
            }
            decoded.append(c);
            i++;
        }
        return decoded.toString();
    }

    private static String getRandomFilename(){
        final String ALLOWED_CHARACTERS2 ="0123456789qwertyuiopasdfghjklzxcvbnmABCDEFGHIJKLMNOPQRSTUVWXYZ";
        final Random random=new Random();
        final StringBuilder sb=new StringBuilder(15);
        for(int i=0 ;i < 15; ++i){
            sb.append(ALLOWED_CHARACTERS2.charAt(random.nextInt(ALLOWED_CHARACTERS2.length())));

        }
        return sb.toString();
    }
}