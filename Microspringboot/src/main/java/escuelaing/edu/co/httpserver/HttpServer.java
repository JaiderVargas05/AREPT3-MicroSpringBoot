package escuelaing.edu.co.httpserver;

import annotations.GetMapping;
import annotations.RequestParam;
import annotations.RestController;
import java.net.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HttpServer {
        private static final Map<String, String> mimeTypes = new HashMap<String, String>() {
        {
            put("html", "text/html");
            put("css", "text/css");
            put("js", "application/javascript");
            put("png", "image/png");
            put("jpg", "image/jpeg");
            put("jpeg", "image/jpeg");
        }
    };

    public static Map<String, Method> services = new HashMap();
    private static Path basePath;
    public static void runServer(String[] args) throws IOException, URISyntaxException, ClassNotFoundException {
        ServerSocket serverSocket = null;
        loadServices(args);
        try {
            serverSocket = new ServerSocket(35000);
        } catch (IOException e) {
            System.err.println("Could not listen on port: 35000.");
            System.exit(1);
        }
        Socket clientSocket = null;

        boolean running = true;
        while (running) {
            try {
                System.out.println("Listo para recibir ...");
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                System.err.println("Accept failed.");
                System.exit(1);
            }

            OutputStream out = new BufferedOutputStream(clientSocket.getOutputStream());
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            clientSocket.getInputStream()));
            String inputLine, outputLine;

            String path = null;
            boolean firstline = true;
            URI requri = null;

            while ((inputLine = in.readLine()) != null) {
                if (firstline) {
                    requri = new URI(inputLine.split(" ")[1]);
                    System.out.println("Path: " + requri.getPath());
                    firstline = false;
                }
                System.out.println("Received: " + inputLine);
                if (!in.ready()) {
                    break;
                }
            }

            if (requri.getPath().startsWith("/app")) {
                out.write(invokeService(requri).getBytes());
            } else {
                readFileService(requri, out);
            }

            out.close();
            in.close();
            clientSocket.close();
        }
        serverSocket.close();
    }
        private static String getMymeType(String fileName) {
        String[] parts = fileName.split("\\.");
        String extention = parts[parts.length - 1];
        String mymeType = mimeTypes.get(extention);
        if (mymeType == null || mymeType.isEmpty()) {
            mymeType = "application/octet-stream";
        }
        return mymeType;
    }
    private static void readFileService(URI requestUri, OutputStream out) throws IOException {
        String fileName = requestUri.getPath();
        String output = "";

        if (fileName.equals("/")) {
            fileName = "index.html";
        }

        Path filePath = Paths.get(basePath.toString(), fileName);
        if (!Files.exists(filePath) || Files.isDirectory(filePath)) {
            output = "HTTP/1.1 404 Not Found\n\r"
                    + "content-type: text/html\n\r"
                    + "\n\r"
                    + "<h1>File not found 404</h1>";
            byte[] body = "<h1>File not found 404</h1>".getBytes(java.nio.charset.StandardCharsets.UTF_8);

            out.write(output.getBytes(java.nio.charset.StandardCharsets.US_ASCII));
            out.write(body);
            out.flush();
            return;
        } else {
            String mymeType = getMymeType(filePath.getFileName().toString());
            byte[] fileBytes = Files.readAllBytes(filePath);
            output = "HTTP/1.1 200 OK\r\n"
                    + "Content-Type: " + mymeType + "\r\n"
                    + "Content-Length: " + fileBytes.length + "\r\n"
                    + "\r\n";
            out.write(output.getBytes(java.nio.charset.StandardCharsets.US_ASCII));
            out.write(fileBytes);
            out.flush();

        }

    }
    public static void loadServices(String args[]){
        try {
            Class c = Class.forName(args[0]);
            if(c.isAnnotationPresent(RestController.class)){
                Method[] methods = c.getDeclaredMethods();
                for(Method m : methods){
                    if(m.isAnnotationPresent(GetMapping.class)){
                        String key = m.getAnnotation(GetMapping.class).value();
                        services.put(key, m);
                    }
                }
            }
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(HttpServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


//    public static void get(String path, Service s) {
//        services.put(path, s);
//    }

    private static String invokeService(URI requri) {
        
        HttpRequest req = new HttpRequest(requri);
        HttpResponse res = new HttpResponse();
        String servicePath = requri.getPath().substring(4);
        Method s = services.get(servicePath);
        RequestParam rp = (RequestParam) s.getParameterAnnotations()[0][0];

        String[] argsValues = new String[]{};
        if(requri.getQuery() == null){
            argsValues = new String[] {rp.defaultValue()};
        }
        else{
            String queryParamName = rp.value();
            argsValues = new String[]{req.getValue(queryParamName)};
        }
        String header = "HTTP/1.1 200 OK\n\r"
                + "content-type: text/html\n\r"
                + "\n\r";
        try {
            return header + s.invoke(null, argsValues);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(HttpServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(HttpServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return header + "ERROR";
    }

    public static void staticfiles(String route) {
        basePath = Paths.get(route).toAbsolutePath().normalize();
    }

    public static void start(String[] args) throws IOException, URISyntaxException, ClassNotFoundException {
        runServer(args);
    }

    public static String defaultResponse() {
        return "HTTP/1.1 200 OK\r\n"
                + "content-type: text/html\r\n"
                + "\r\n"
                + "<!DOCTYPE html>\n"
                + "<html>\n"
                + "<head>\n"
                + "<title>Form Example</title>\n"
                + "<meta charset=\"UTF-8\">\n"
                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n"
                + "</head>\n"
                + "<body>\n"
                + "<h1>Form with GET</h1>\n"
                + "<form action=\"/hello\">\n"
                + "<label for=\"name\">Name:</label><br>\n"
                + "<input type=\"text\" id=\"name\" name=\"name\" value=\"John\"><br><br>\n"
                + "<input type=\"button\" value=\"Submit\" onclick=\"loadGetMsg()\">\n"
                + "</form>\n"
                + "<div id=\"getrespmsg\"></div>\n"
                + " \n"
                + "<script>\n"
                + "function loadGetMsg() {\n"
                + "let nameVar = document.getElementById(\"name\").value;\n"
                + "const xhttp = new XMLHttpRequest();\n"
                + "xhttp.onload = function() {\n"
                + "document.getElementById(\"getrespmsg\").innerHTML =\n"
                + "this.responseText;\n"
                + "}\n"
                + "xhttp.open(\"GET\", \"/app/hello?name=\"+nameVar);\n"
                + "xhttp.send();\n"
                + "}\n"
                + "</script>\n"
                + " \n"
                + "<h1>Form with POST</h1>\n"
                + "<form action=\"/hellopost\">\n"
                + "<label for=\"postname\">Name:</label><br>\n"
                + "<input type=\"text\" id=\"postname\" name=\"name\" value=\"John\"><br><br>\n"
                + "<input type=\"button\" value=\"Submit\" onclick=\"loadPostMsg(postname)\">\n"
                + "</form>\n"
                + " \n"
                + "<div id=\"postrespmsg\"></div>\n"
                + " \n"
                + "<script>\n"
                + "function loadPostMsg(name){\n"
                + "let url = \"/hellopost?name=\" + name.value;\n"
                + " \n"
                + "fetch (url, {method: 'POST'})\n"
                + ".then(x => x.text())\n"
                + ".then(y => document.getElementById(\"postrespmsg\").innerHTML = y);\n"
                + "}\n"
                + "</script>\n"
                + "</body>\n"
                + "</html>";
    }

}
