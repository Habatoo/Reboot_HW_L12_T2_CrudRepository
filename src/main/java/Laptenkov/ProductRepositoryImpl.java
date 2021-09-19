package Laptenkov;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Класс ProductRepositoryImpl, представляет CRUD репозиторий к таблице
 * объекта {@link Product}.
 * Класс {@link ProductRepositoryImpl} реализует интерфейс {@link ProductRepository}.
 * <p>
 * Конструктор класса ProductRepositoryImpl:
 * ProductRepositoryImpl(Connection connection)
 * <p>
 * Критерии приемки
 * Предоставить PR, в котором присутствует реализация класса ProductRepositoryImpl.
 * Каждый публичный метод должен быть покрыт unit тестом.
 */
public class ProductRepositoryImpl implements ProductRepository {

    Statement statement;

    /**
     * Конструктор объекта  {@link ProductRepositoryImpl} с параметром
     * в виде объекта типа {@link Connection}.
     */
    ProductRepositoryImpl(Connection connection) throws SQLException {
        this.statement = connection.createStatement();
    }

    /**
     * Метод {@link ProductRepositoryImpl#createTable()} осуществляет
     * создание таблицы типа {@link Product} в базе.
     */
    public void createTable() {
        String query = "create extension if not exists \"uuid-ossp\";" +
                "drop type if exists product_category;" +
                "create type product_category as enum('COSMETICS', 'FOOD', 'CHEMICAL', 'TECHNIC');" +
                "create table if not exists product (" +
                " id uuid default uuid_generate_v4 ()," +
                " name varchar(32)," +
                " description varchar(32)," +
                " category product_category," +
                " manufacture_date_time timestamp," +
                " manufacturer varchar(32)," +
                " has_expiry_time boolean," +
                " stock integer);";

        try {
            int updatedRowCount = statement.executeUpdate(query);
            System.out.println("Create table with rows " + updatedRowCount);
        } catch (SQLException throwables) {
            throw new RuntimeException("Cannot create table.");
        }
    }

    /**
     * Метод {@link ProductRepositoryImpl#dropTable()} осуществляет
     * удаление таблицы типа {@link Product} из базы.
     */
    public void dropTable() throws SQLException {

        String query = "drop table product";
        int updatedRowCount = statement.executeUpdate(query);
        System.out.println("Drop table with rows " + updatedRowCount);

    }

    /**
     * Метод {@link ProductRepositoryImpl#findAll()}
     * осуществляет поиск всех объектов типа {@link Product}.
     *
     * @return лист объектов типа {@link Product}.
     */
    public List<Product> findAll() {
        List<Product> productList = new ArrayList<>();

        String query = "select * from product;";

        try (ResultSet resultSet = statement.executeQuery(query)) {
            while (resultSet.next()) {
                String id = resultSet.getString("id");
                String name = resultSet.getString("name");
                String description = resultSet.getString("description");
                String productCategory = resultSet.getString("category");
                String manufactureDateTime = resultSet.getString(
                        "manufacture_date_time");
                String manufacturer = resultSet.getString("manufacturer");
                String hasExpiryTime = resultSet.getString("has_expiry_time");
                int stock = resultSet.getInt("stock");
                productList.add(new Product(
                        java.util.UUID.fromString(id),
                        name,
                        description,
                        ProductCategory.valueOf(productCategory),
                        LocalDateTime.parse(manufactureDateTime.replace(" ", "T")),
                        manufacturer,
                        Boolean.getBoolean(hasExpiryTime),
                        stock));
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Cannot select users.", ex);
        }

        return productList;
    }

    /**
     * Метод {@link ProductRepositoryImpl#findById(UUID)} осуществляет
     * поиск объекта типа {@link Product} по id этого объекта.
     *
     * @param id объекта {@link Product}
     * @return объект типа {@link Product}  или null если объекта с
     * запрашиваемым id не существует.
     */
    @Override
    public Product findById(UUID id) {
        String query = "select * from product where id::text = '" + id.toString() + "';";

        try {
            boolean result = statement.execute(query);
            ResultSet resultSet = statement.getResultSet();

            if (resultSet.next() == false) {
                return null;
            }

            String product_id = resultSet.getString("id");
            String name = resultSet.getString("name");
            String description = resultSet.getString("description");
            String productCategory = resultSet.getString("category");
            String manufactureDateTime = resultSet.getString("manufacture_date_time");
            String manufacturer = resultSet.getString("manufacturer");
            String hasExpiryTime = resultSet.getString("has_expiry_time");
            int stock = resultSet.getInt("stock");

            Product product = new Product(
                    java.util.UUID.fromString(product_id),
                    name,
                    description,
                    ProductCategory.valueOf(productCategory),
                    LocalDateTime.parse(manufactureDateTime.replace(" ", "T")),
                    manufacturer,
                    Boolean.getBoolean(hasExpiryTime),
                    stock);

            return product;
        } catch (SQLException ex) {
            throw new RuntimeException("Cannot select user.", ex);
        }
    }

    /**
     * Метод {@link ProductRepositoryImpl#deleteById(UUID)} осуществляет
     * удаление объекта типа {@link Product} по id этого объекта.
     *
     * @param id объекта {@link Product}
     */
    @Override
    public void deleteById(UUID id) {

        try {
            int updatedRowCount = statement.executeUpdate(
                    "delete from product where id::text ='" + id.toString() + "'");
            System.out.println("Delete product completed updateRowCount = " + updatedRowCount);
        } catch (SQLException ex) {
            throw new RuntimeException("Cannot delete user.", ex);
        }
    }

    /**
     * Метод {@link ProductRepositoryImpl#save(Product)} осуществляет
     * сохранение объекта типа {@link Product}.
     * Если id передаваемого объекта null, то создается новый объект,
     * либо осуществляется обновление полей существубющего объекта.
     *
     * @param product объект типа {@link Product}
     * @return новый или обновленный объект типа {@link Product}
     */
    @Override
    public Product save(Product product) {
        if (null != product.id) {
            String query = String.format("insert into product" +
                            " (id, name, description, category, manufacture_date_time," +
                            " manufacturer, has_expiry_time, stock) " +
                            " values " +
                            "('%s', '%s', '%s', '%s', '%s', '%s', '%s', '%d')",
                    product.getId(),
                    product.getName(),
                    product.getDescription(),
                    product.getCategory(),
                    product.getManufactureDateTime(),
                    product.getManufacturer(),
                    product.isHasExpiryTime(),
                    product.getStock());

            try {
                boolean result = statement.execute(query);
                int updatedRowCount = statement.getUpdateCount();
                System.out.println("Update table with rows " + updatedRowCount);
            } catch (SQLException ex) {
                throw new RuntimeException("Cannot update table with user.", ex);
            }

        } else {
            String query = String.format("update product" +
                            " set id = '%s', name = '%s', description = '%s', category = '%s'," +
                            " manufacture_date_time = '%s'," +
                            " manufacturer = '%s', has_expiry_time = '%s', stock = '%d';",
                    UUID.randomUUID(),
                    product.getName(),
                    product.getDescription(),
                    product.getCategory(),
                    product.getManufactureDateTime(),
                    product.getManufacturer(),
                    product.isHasExpiryTime(),
                    product.getStock()
            );

            try {
                boolean result = statement.execute(query);
                int updatedRowCount = statement.getUpdateCount();
                System.out.println("Update table with rows " + updatedRowCount);
            } catch (SQLException ex) {
                throw new RuntimeException("Cannot update table with user.", ex);
            }
        }

        return product;
    }

    /**
     * Метод {@link ProductRepositoryImpl#findAllByCategory(ProductCategory)}
     * осуществляет поиск объектов типа {@link Product} с указанным типом
     * категории объектов.
     *
     * @param category объект типа {@link ProductCategory}
     * @return лист объектов типа {@link Product}.
     */
    @Override
    public List<Product> findAllByCategory(ProductCategory category) {
        List<Product> productList = new ArrayList<>();

        String query = "select * from product where category = any(array['" +
                category.toString() +
                "']::product_category[]);";

        try (ResultSet resultSet = statement.executeQuery(query)) {
            while (resultSet.next()) {
                String id = resultSet.getString("id");
                String name = resultSet.getString("name");
                String description = resultSet.getString("description");
                String productCategory = resultSet.getString("category");
                String manufactureDateTime = resultSet.getString(
                        "manufacture_date_time");
                String manufacturer = resultSet.getString("manufacturer");
                String hasExpiryTime = resultSet.getString("has_expiry_time");
                int stock = resultSet.getInt("stock");
                productList.add(new Product(
                        java.util.UUID.fromString(id),
                        name,
                        description,
                        ProductCategory.valueOf(productCategory),
                        LocalDateTime.parse(manufactureDateTime.replace(" ", "T")),
                        manufacturer,
                        Boolean.getBoolean(hasExpiryTime),
                        stock));
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Cannot find all by category.", ex);
        }

        return productList;
    }
}
