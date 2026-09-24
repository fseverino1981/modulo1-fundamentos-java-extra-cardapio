package mx.florinda.cardapio;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SQLDatabase implements Database{

    @Override
    public List<ItemCardapio> listaItensCardapio() {

        ArrayList<ItemCardapio> itens = new ArrayList<>();
        String sql = "SELECT id, nome, descricao, categoria, preco, preco_promocional\n" +
                "FROM cardapio.item_cardapio;";

        try (PreparedStatement ps = SQLDatabaseConnection.getPreparedStatement(sql);
            ResultSet rs = ps.executeQuery()){
            
            while(rs.next()){
                long id = rs.getLong("id");
                String nome = rs.getString("nome");
                String descricao = rs.getString("descricao");
                String categoriaStr = rs.getString("categoria");
                BigDecimal preco = rs.getBigDecimal("preco");
                BigDecimal precoPromocional = rs.getBigDecimal("preco_promocional");

                var itemCardapio = new ItemCardapio(id, nome, descricao, ItemCardapio.CategoriaCardapio.valueOf(categoriaStr), preco, precoPromocional);
                itens.add(itemCardapio);

            };
            

        } catch (SQLException e){
            throw new RuntimeException(e);
        }
        return itens;
    }

    @Override
    public int totalItensCardapio() {

        String sql = "SELECT COUNT(*) FROM cardapio.item_cardapio;";

        try (PreparedStatement ps = SQLDatabaseConnection.getPreparedStatement(sql);
             ResultSet rs = ps.executeQuery()){

            int total = 0;
            if (rs.next()){
                total = rs.getInt(1);
            };

            return total;
        } catch (SQLException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void adicionaItemCardapio(ItemCardapio item) {
        String sql = "INSERT INTO cardapio.item_cardapio (nome, descricao, categoria, preco, preco_promocional)" +
                "VALUES" +
                "(?, ?, ?, ?, ?);";

        try (PreparedStatement ps = SQLDatabaseConnection.getPreparedStatement(sql)){

            ps.setString(1, item.nome());
            ps.setString(2, item.descricao());
            ps.setString(3, item.categoria().name());
            ps.setBigDecimal(4, item.preco());
            ps.setBigDecimal(5, item.precoPromocional());
            ps.execute();

        } catch (SQLException e){
            throw new RuntimeException(e);
        }

    }

    @Override
    public Optional<ItemCardapio> itemCardapioPorId(Long id) {
        String sql = "SELECT id, nome, descricao, categoria, preco, preco_promocional\n" +
                "FROM cardapio.item_cardapio\n" +
                "WHERE id = ?";

        try (PreparedStatement ps = SQLDatabaseConnection.getPreparedStatement(sql)){

            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()){
                id = rs.getLong("id");
                String nome = rs.getString("nome");
                String descricao = rs.getString("descricao");
                String categoriaStr = rs.getString("categoria");
                BigDecimal preco = rs.getBigDecimal("preco");
                BigDecimal precoPromocional = rs.getBigDecimal("preco_promocional");

                ItemCardapio itemCardapio = new ItemCardapio(id, nome, descricao,
                        ItemCardapio.CategoriaCardapio.valueOf(categoriaStr), preco, precoPromocional);

                return Optional.of(itemCardapio);

            };

            return Optional.empty();

        } catch (SQLException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean removeItemCardapio(Long id) {
        String sql = "DELETE FROM cardapio.item_cardapio\n" +
                     "WHERE id=?";

        try (PreparedStatement ps = SQLDatabaseConnection.getPreparedStatement(sql)){

            ps.setLong(1, id);
            int linhasApagadas = ps.executeUpdate();
            return linhasApagadas > 0;

        } catch (SQLException e){
            throw new RuntimeException(e);
        }

    }

    @Override
    public boolean alteraPrecoItemCardapio(Long id, BigDecimal novoPreco) {
        String sql = "UPDATE cardapio.item_cardapio\n" +
                     "   SET preco = ?\n" +
                     " WHERE id = ?";

        try(PreparedStatement ps = SQLDatabaseConnection.getPreparedStatement(sql)){
            ps.setBigDecimal(1, novoPreco);
            ps.setLong(2, id);

            int linhasAtualizadas = ps.executeUpdate();

            return linhasAtualizadas > 0;

        }catch (SQLException e){
            throw new RuntimeException(e);
        }
    }

}
