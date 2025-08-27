/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package escuelaing.edu.co.microspringboot;

import escuelaing.edu.co.httpserver.HttpServer;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 *
 * @author jaider.vargas-n
 */
public class Microspringboot {

    public static void main(String[] args) throws IOException, URISyntaxException, ClassNotFoundException {
        System.out.println("Running Microspringboot");
        HttpServer.runServer(args);
    }
}
