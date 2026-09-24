package mx.florinda.cardapio;


import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class Main {
    public static void main(String[] args) {
        Database database = new SQLDatabase();

//        int total = database.totalItensCardapio();
//        System.out.println("Total de itens: " + total);

//        var novoItemCardapio = new ItemCardapio(0L, "Tacos de Carnitas", "Tacos recheadas com carne tenra",
//                ItemCardapio.CategoriaCardapio.PRATOS_PRINCIPAIS, new BigDecimal("25.9"), null);
//        database.adicionaItemCardapio(novoItemCardapio);
//        System.out.println("Item adicionado com sucesso!");

//        List<ItemCardapio> listaItensCardapio = database.listaItensCardapio();
//        listaItensCardapio.forEach(System.out::println);


        System.out.println(
                database.alteraPrecoItemCardapio(5L, BigDecimal.valueOf(9.99))
                    ? "Atualizado com sucesso"
                    : "Registro não atualizado");

        Optional<ItemCardapio> itemCardapio = database.itemCardapioPorId(5L);
        System.out.println(itemCardapio.toString());

//        System.out.println(
//                database.removeItemCardapio(10L)
//                        ? "Removido com sucesso"
//                        : "Registro não encontrado"
//        );


    }

}
