/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package escuelaing.edu.co.microspringboot.examples;

import annotations.GetMapping;
import annotations.RequestParam;
import annotations.RestController;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author jaider.vargas-n
 */
@RestController

public class GreetingController {
    	private static final String template = "Hello, %s!";
	private final AtomicLong counter = new AtomicLong();

	@GetMapping("/greeting")
	public static String greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
		return "Hola " + name;
	}
}
