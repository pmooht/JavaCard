/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gymcard.CardManager;

public class CardIdGenerator {
    private static int counter = 1;

    public static synchronized String nextId() {
        // GYM000001, GYM000002, ...
        return String.format("GYM%06d", counter++);
    }
}