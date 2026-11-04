package com.kb.cosmetic_wms.support;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;

public abstract class MySqlContainerSupport {

    private static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("cosmetic_wms")
            .withCommand("--character-set-server=utf8mb4", "--collation-server=utf8mb4_unicode_ci");

    static {
        MYSQL.start();
    }

    /**
     * 컨테이너의 root 계정으로 접속하는 JDBC URL. 마이그레이션 테스트처럼 별도 데이터베이스를 만들 때 쓴다.
     */
    protected static String rootJdbcUrl(String database) {
        return "jdbc:mysql://" + MYSQL.getHost() + ":" + MYSQL.getFirstMappedPort() + "/" + database
                + "?serverTimezone=Asia/Seoul&useSSL=false&allowPublicKeyRetrieval=true";
    }

    protected static String rootPassword() {
        return MYSQL.getPassword();
    }

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",
                () -> MYSQL.getJdbcUrl() + "?serverTimezone=Asia/Seoul&useSSL=false&allowPublicKeyRetrieval=true");
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
    }
}
