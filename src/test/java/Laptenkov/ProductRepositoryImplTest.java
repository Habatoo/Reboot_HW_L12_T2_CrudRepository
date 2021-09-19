package Laptenkov;

import Laptenkov.ProductCategory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Класс для тестирования public методов класса {@link ProductRepositoryImpl}.
 *
 * @author habatoo
 */
class ProductRepositoryImplTest {

    private ProductRepositoryImpl productNotEmptyRepository;
    private Product product_1;
    private Product product_2;
    private Product product_3;
    private Product product_4;
    private String url;
    private String login;
    private String password;
    private Connection connection;

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    /**
     * Инициализация экземпляров тестируемого класса {@link ProductRepositoryImpl}.
     * Инициализация экземпляра класса {@link PrintStream} для контроля отображаемых сообщений.
     */
    @BeforeEach
    void setUp() {
        url = "jdbc:postgresql://localhost:5432/testdb";
        login = "habatoo";
        password = "1234567890";

        try {
            connection = DriverManager.getConnection(url, login, password);
            productNotEmptyRepository = new ProductRepositoryImpl(connection);

        } catch (SQLException ex) {
            throw new RuntimeException("Cannot create connection.", ex);
        }

        product_1 = new Product(
                UUID.randomUUID(),
                "name_1",
                "description_1",
                ProductCategory.COSMETICS,
                LocalDateTime.of(2021, 1, 30, 11, 11),
                "manufacturer_1",
                true,
                100);

        product_2 = new Product(
                UUID.randomUUID(),
                "name_2",
                "description_2",
                ProductCategory.CHEMICAL,
                LocalDateTime.of(2021, 2, 10, 9, 8),
                "manufacturer_2",
                true,
                200);

        product_3 = new Product(
                UUID.randomUUID(),
                "name_3",
                "description_3",
                ProductCategory.COSMETICS,
                LocalDateTime.of(2021, 1, 20, 6, 3),
                "manufacturer_3",
                false,
                300);

        productNotEmptyRepository.createTable();
        productNotEmptyRepository.save(product_1);
        productNotEmptyRepository.save(product_2);

    }

    /**
     * Очистка экземпляров тестируемого класса {@link ProductRepositoryImpl}.
     */
    @AfterEach
    void tearDown() {
        url = null;
        login = null;
        password = null;
        connection = null;
        try {
            productNotEmptyRepository.dropTable();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        productNotEmptyRepository = null;
        product_1 = null;
        product_2 = null;
        product_3 = null;
        product_4 = null;
        System.setOut(originalOut);
    }

    /**
     * Проверяет успешную подгрузку контроллеров из контекста.
     */
    @Test
    void loadRepository_Test() {
        Assertions.assertNotNull(productNotEmptyRepository);
    }

    /**
     * Проверка значения, которое возвращает метод {@link ProductRepositoryImpl#findById(UUID)},
     * при подаче входящего параметров типа UUID.
     * <br>Сценарий, при котором метод {@link ProductRepositoryImpl#findById(UUID)}
     * возвращает объект типа {@link Product} при подаче существующего id.
     */
    @Test
    void findByIdSuccess_Test() {
        UUID uuid = product_1.getId();
        Assertions.assertEquals(uuid, productNotEmptyRepository.findById(uuid).getId());
    }

    /**
     * Проверка значения, которое возвращает метод {@link ProductRepositoryImpl#findById(UUID)},
     * при подаче входящего параметров типа UUID.
     * <br>Сценарий, при котором метод {@link ProductRepositoryImpl#findById(UUID)}
     * возвращает null при подаче не существующего id.
     */
    @Test
    void findByIdFail_Test() {
        UUID uuid = UUID.randomUUID();
        Assertions.assertEquals(null, productNotEmptyRepository.findById(uuid));
    }

    /**
     * Проверка значения, которое возвращает метод {@link ProductRepositoryImpl#deleteById(UUID)},
     * при подаче входящего параметров типа UUID.
     * <br>Сценарий, при котором метод {@link ProductRepositoryImpl#deleteById(UUID)}
     * удаляет объект типа {@link Product} при подаче не существующего id.
     */
    @Test
    void deleteByIdFail_Test() {
        Assertions.assertEquals(2, productNotEmptyRepository.findAll().size());
        UUID uuid = UUID.randomUUID();
        productNotEmptyRepository.deleteById(uuid);
        Assertions.assertEquals(2, productNotEmptyRepository.findAll().size());
    }

    /**
     * Проверка значения, которое возвращает метод {@link ProductRepositoryImpl#deleteById(UUID)},
     * при подаче входящего параметров типа UUID.
     * <br>Сценарий, при котором метод {@link ProductRepositoryImpl#deleteById(UUID)}
     * удаляет объект типа {@link Product} при подаче существующего id.
     */
    @Test
    void deleteByIdSuccess_Test() {
        Assertions.assertEquals(2, productNotEmptyRepository.findAll().size());
        UUID uuid = product_1.getId();
        productNotEmptyRepository.deleteById(uuid);
        Assertions.assertEquals(1, productNotEmptyRepository.findAll().size());

    }

    /**
     * Проверка значения, которое возвращает метод {@link ProductRepositoryImpl#save(Product)},
     * при подаче входящего параметров типа Product.
     * <br>Сценарий, при котором метод {@link ProductRepositoryImpl#save(Product)}
     * сохраняет объект типа {@link Product} при подаче не существующего id.
     */
    @Test
    void saveNew_Test() {
        product_4 = new Product(
                null,
                "name_4",
                "description_4",
                ProductCategory.FOOD,
                LocalDateTime.of(2021, 12, 20, 1, 4),
                "manufacturer_4",
                false,
                500);

        Assertions.assertEquals("description_4", productNotEmptyRepository.save(product_4).getDescription());
        Assertions.assertEquals("manufacturer_4", product_4.getManufacturer());
    }

    /**
     * Проверка значения, которое возвращает метод {@link ProductRepositoryImpl#save(Product)},
     * при подаче входящего параметров типа Product.
     * <br>Сценарий, при котором метод {@link ProductRepositoryImpl#save(Product)}
     * сохраняет объект типа {@link Product} при подаче существующего id.
     */
    @Test
    void saveExistence_Test() {
        product_1.setDescription("description_11");
        productNotEmptyRepository.save(product_1);
        Assertions.assertEquals(
                "description_11",
                productNotEmptyRepository.save(product_1).getDescription());
    }

    /**
     * Проверка значения, которое возвращает метод
     * {@link ProductRepositoryImpl#findAllByCategory(ProductCategory)},
     * при подаче входящего параметров типа {@link ProductCategory} .
     * <br>Сценарий, при котором метод
     * {@link ProductRepositoryImpl#findAllByCategory(ProductCategory)},
     * возвращает объект типа {@link ArrayList}.
     */
    @Test
    void findAllByCategorySuccess_Test() {
        Assertions.assertEquals(
                1,
                productNotEmptyRepository.findAllByCategory(ProductCategory.COSMETICS).size()
        );
    }

    /**
     * Проверка значения, которое возвращает метод
     * {@link ProductRepositoryImpl#findAllByCategory(ProductCategory)},
     * при подаче входящего параметров типа {@link ProductCategory} .
     * <br>Сценарий, при котором метод
     * {@link ProductRepositoryImpl#findAllByCategory(ProductCategory)},
     * возвращает объект типа {@link ArrayList}.
     */
    @Test
    void findAllByCategoryFail_Test() {
        Assertions.assertEquals(
                0,
                productNotEmptyRepository.findAllByCategory(ProductCategory.TECHNIC).size()
        );
    }

}