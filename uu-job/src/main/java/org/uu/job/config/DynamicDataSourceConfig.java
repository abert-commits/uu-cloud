package org.uu.job.config;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.uu.job.Enum.DataSourceKey;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Admin
 */
@Configuration
public class DynamicDataSourceConfig {

    @Bean("walletDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.wallet")
    public DataSource walletDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean("managerDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.manager")
    public DataSource managerDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Primary
    @Bean("dynamicDataSource")
    public DataSource dynamicDataSource(@Qualifier("walletDataSource") DataSource walletDataSource,
                                        @Qualifier("managerDataSource") DataSource managerDataSource) {

        Map<Object, Object> dataSourceMap = new HashMap<>(2);
        dataSourceMap.put(DataSourceKey.WALLET.name(), walletDataSource);
        dataSourceMap.put(DataSourceKey.MANAGER.name(), managerDataSource);

        DynamicRoutingDataSource dynamicRoutingDataSource = new DynamicRoutingDataSource();
        dynamicRoutingDataSource.setDefaultTargetDataSource(walletDataSource);
        dynamicRoutingDataSource.setTargetDataSources(dataSourceMap);

        return dynamicRoutingDataSource;
    }

    @Bean
    public MybatisSqlSessionFactoryBean sqlSessionFactoryBean(@Qualifier("dynamicDataSource") DataSource dataSource) throws Exception {
        MybatisSqlSessionFactoryBean sqlSessionFactoryBean = new MybatisSqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource);
        org.springframework.core.io.Resource[] resources = new PathMatchingResourcePatternResolver().getResources("classpath*:*mapper/*.xml");
        sqlSessionFactoryBean.setMapperLocations(resources);
        MybatisConfiguration mybatisConfiguration = new MybatisConfiguration();
        sqlSessionFactoryBean.setConfiguration(mybatisConfiguration);
        return sqlSessionFactoryBean;
    }
}
