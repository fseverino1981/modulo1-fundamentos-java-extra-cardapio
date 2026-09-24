package mx.florinda.cardapio;

import com.google.gson.Gson;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ServidorItensCardapioComSocket {

    private static final Database database = new SQLDatabase();

    public static void main(String[] args) throws Exception {

        Executor executor = Executors.newFixedThreadPool(50);

        try(ServerSocket serverSocket = new ServerSocket(8000)) {
            System.out.println("Subiu servidor!");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                executor.execute(() -> trataRequisicao(clientSocket));
            }

        }
    }

    private static void trataRequisicao(Socket clientSocket) {

        try (clientSocket) {
            InputStream clientIS = clientSocket.getInputStream();

            StringBuilder requestBuilder = new StringBuilder();
            int data;
            do {
                data = clientIS.read();
                requestBuilder.append((char) data);
            } while (clientIS.available() > 0);

            String request = requestBuilder.toString();
            System.out.println(request);
            System.out.println("\n\nChegou um novo request");

            String[] requestChunks = request.split("\r\n\r\n");
            String requestLineAndHeaders = requestChunks[0];
            String[] requestLineAndHeadersChunks = requestLineAndHeaders.split("\r\n");
            String requestLine = requestLineAndHeadersChunks[0];
            String[] requestLineChunks = requestLine.split(" ");
            String method = requestLineChunks[0];
            String requestURI = requestLineChunks[1];
            String httpVersion = requestLineChunks[2];

            System.out.println("Method: " + method);
            System.out.println("Request URI: " + requestURI);
            System.out.println("HTTP Version: " + httpVersion);

            Thread.sleep(250);

            OutputStream clientOS = clientSocket.getOutputStream();
            PrintStream clientOut = new PrintStream(clientOS);

            if ("/itensCardapio.json".equals(requestURI)) {
                System.out.println("Chamou arquivo itensCardapio.json");

                Path path = Path.of("itensCardapio.json");
                String json = Files.readString(path);

                clientOut.println("HTTP/1.1 200 OK");
                clientOut.println("Content-type: application/json; charset=UTF-8");
                clientOut.println();
                clientOut.println(json);

            } else if ("GET".equals(method) && "/itens-cardapio".equals(requestURI)) {
                System.out.println("Chamou listagem de itens de cardápio");
                List<ItemCardapio> listaItensCardapio = database.listaItensCardapio();

                Gson gson = new Gson();
                String json = gson.toJson(listaItensCardapio);

                clientOut.println("HTTP/1.1 200 OK");
                clientOut.println("Content-type: application/json; charset=UTF-8");
                clientOut.println();
                clientOut.println(json);
            } else if ("GET".equals(method) && "/itens-cardapio/total".equals(requestURI)) {
                System.out.println("Chamou total de itens de cardápio");
                int totalItens = database.totalItensCardapio();

                clientOut.println("HTTP/1.1 200 OK");
                clientOut.println();
                clientOut.println(totalItens);
            } else if ("POST".equals(method) && "/itens-cardapio".equals(requestURI)) {
                System.out.println("Chamou adição de itens de cardápio");

                if (requestChunks.length == 1) {
                    clientOut.println("HTTP/1.1 400 Bad Request");
                }
                String body = requestChunks[1];

                Gson gson = new Gson();
                ItemCardapio item = gson.fromJson(body, ItemCardapio.class);

                database.adicionaItemCardapio(item);

                clientOut.println("HTTP/1.1 200 OK");
            } else if ("GET".equals(method) && requestURI.matches("/itens-cardapio/\\d+")){
                String idStr = requestURI.replaceAll("/itens-cardapio/", "");
                long id = Long.parseLong(idStr);

                System.out.println("Chamou item do cardápio por ID: " + id);

                Optional<ItemCardapio> itemCardapio = database.itemCardapioPorId(id);

                if(itemCardapio.isPresent()){
                    ItemCardapio item = itemCardapio.get();

                    Gson gson = new Gson();
                    String json = gson.toJson(item);

                    clientOut.println("HTTP/1.1 200 OK");
                    clientOut.println("Content-type: application/json; charset=UTF-8");
                    clientOut.println();
                    clientOut.println(json);
                }else{
                    clientOut.println("HTTP/1.1 404 Not Found");
                    clientOut.println("Content-Type: application/json; charset=UTF-8");
                    clientOut.println();
                }
            } else if ("DELETE".equals(method) && requestURI.matches("/itens-cardapio/\\d+")) {
                String idStr = requestURI.replaceAll("/itens-cardapio/", "");
                long id = Long.parseLong(idStr);

                System.out.println("Chamou delecao de item do cardápio ID: " + id);

                boolean b = database.removeItemCardapio(id);
                if (b) {
                    clientOut.println("HTTP/1.1 204 No Content");
                    clientOut.println("Content-type: application/json; charset=UTF-8");
                    clientOut.println();
                } else {
                    clientOut.println("HTTP/1.1 404 Not Found");
                    clientOut.println("Content-Type: application/json; charset=UTF-8");
                    clientOut.println();
                }
            } else if ("PATCH".equals(method) && requestURI.matches("/itens-cardapio/\\d+")) {
                String idStr = requestURI.replaceAll("/itens-cardapio/", "");
                long id = Long.parseLong(idStr);

                System.out.println("Chamou atualizacao de item do cardápio ID: " + id);

                if (requestChunks.length == 1) {
                    clientOut.println("HTTP/1.1 400 Bad Request");
                }
                String body = requestChunks[1];

                Gson gson = new Gson();
                com.google.gson.JsonObject jsonObject = gson.fromJson(body, com.google.gson.JsonObject.class);

                BigDecimal novoPreco = jsonObject.get("novo_preco").getAsBigDecimal();

                boolean b = database.alteraPrecoItemCardapio(id, novoPreco);
                if (b){
                    clientOut.println("HTTP/1.1 204 No Content");
                    clientOut.println();
                }else{
                    clientOut.println("HTTP/1.1 404 Not Found");
                    clientOut.println("Content-Type: application/json; charset=UTF-8");
                    clientOut.println();
                    clientOut.println("{\"erro\": \"Item não encontrado\"}");
                }

            }else{
                System.out.println("URI não encontrada: " + requestURI);
                clientOut.println("HTTP/1.1 404 Not Found");
            }

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

}
