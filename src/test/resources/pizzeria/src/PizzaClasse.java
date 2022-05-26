import java.util.ArrayList;
import java.util.List;

public class PizzaClasse {
    private static final int prezzoBase = 42;

    public static void main(String[] args) {
        pizzaMetodo();
        pizzaMetodo(256);

        PizzaServizio pizzaServizio = new PizzaServizio() {
            @Override
            public PizzaAstratta fareLaPizza() {
                return new BuonaPizza();
            }
        };

        pizzaServizio.saluto();
        PizzaAstratta pizza = pizzaServizio.fareLaPizza();

    }

    public static void pizzaMetodo() {
        System.out.println("Io sono una buona pizza! Mi costa " + prezzoBase + " euro.");
    }

    public static void pizzaMetodo(int prezzo) {
        System.out.println("Io sono una buona pizza! Mi costa " + prezzo + " euro.");
    }
}

interface PizzaServizio {
    default void saluto() {
        System.out.println("Viva la pizza Italiana");
    }

    PizzaAstratta fareLaPizza();
}

abstract class PizzaAstratta {
    public void saluto() {
        System.out.println("Io sono una pizza astratta. Non posso essere istanziato");
    }

    public abstract List<String> selencaGliIngredienti();
}

class BuonaPizza extends PizzaAstratta {

    @Override
    public List<String> selencaGliIngredienti() {
        return List.of("Salame", "Mozzarella", "Salsa di pomodoro");
    }
}
