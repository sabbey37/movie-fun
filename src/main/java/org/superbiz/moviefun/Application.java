package org.superbiz.moviefun;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
})
public class Application {

    public static void main(String... args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public ServletRegistrationBean actionServletRegistration(ActionServlet actionServlet) {
        return new ServletRegistrationBean(actionServlet, "/moviefun/*");
    }

    @Bean
    public DatabaseServiceCredentials credentials(Environment env) {
        return new DatabaseServiceCredentials(env.getProperty("VCAP_SERVICES"));
    }

    @Bean
    public DataSource albumsDataSource(DatabaseServiceCredentials credentials) {
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setURL(credentials.jdbcUrl("albums-mysql"));
        HikariConfig dataConfig = new HikariConfig();
        dataConfig.setDataSource(dataSource);
        return new HikariDataSource(dataConfig);
    }

    @Bean
    public DataSource moviesDataSource(DatabaseServiceCredentials credentials) {
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setURL(credentials.jdbcUrl("movies-mysql"));
        HikariConfig dataConfig = new HikariConfig();
        dataConfig.setDataSource(dataSource);
        return new HikariDataSource(dataConfig);
    }

    @Bean
    public HibernateJpaVendorAdapter vendorAdapter() {
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setDatabasePlatform("org.hibernate.dialect.MySQL5Dialect");
        vendorAdapter.setGenerateDdl(true);
        vendorAdapter.setDatabase(Database.MYSQL);
        return vendorAdapter;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean albumsEntityManager(DataSource albumsDataSource,
                                                                      HibernateJpaVendorAdapter vendorAdapter) {
        LocalContainerEntityManagerFactoryBean entityManager = new LocalContainerEntityManagerFactoryBean();
        entityManager.setDataSource(albumsDataSource);
        entityManager.setJpaVendorAdapter(vendorAdapter);
        entityManager.setPackagesToScan("org.superbiz.moviefun.albums");
        entityManager.setPersistenceUnitName("albums");
        return entityManager;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean moviesEntityManager(DataSource moviesDataSource,
                                                                      HibernateJpaVendorAdapter vendorAdapter) {
        LocalContainerEntityManagerFactoryBean entityManager = new LocalContainerEntityManagerFactoryBean();
        entityManager.setDataSource(moviesDataSource);
        entityManager.setJpaVendorAdapter(vendorAdapter);
        entityManager.setPackagesToScan("org.superbiz.moviefun.movies");
        entityManager.setPersistenceUnitName("movies");
        return entityManager;
    }

    @Bean
    public PlatformTransactionManager albumsTransactionManager(EntityManagerFactory albumsEntityManager) {
        return new JpaTransactionManager(albumsEntityManager);
    }

    @Bean
    public PlatformTransactionManager moviesTransactionManager(EntityManagerFactory moviesEntityManager) {
        return new JpaTransactionManager(moviesEntityManager);
    }

}
