/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package escuelaing.edu.co.microspringboot;

import escuelaing.edu.co.httpserver.HttpServer;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author jaider.vargas-n
 */
public class Microspringboot {

    public static void main(String[] args) throws IOException, URISyntaxException, ClassNotFoundException, Exception {
        System.out.println("Running Microspringboot");
        String base = "escuelaing.edu.co.microspringboot.examples";
        Set<Class<?>> setControllers = FindControllers.find(base, annotations.RestController.class);
        List<Class<?>> controllers = new ArrayList<>(setControllers);
        String[] controllerNames = new String[controllers.size()];
        for (int i = 0; i < controllers.size(); i++) {
            controllerNames[i] = controllers.get(i).getName();
        }
        HttpServer.staticfiles("src/main/resources/static");
        HttpServer.runServer(controllerNames);
    }
}