package org.uu.common.redis.config;

import lombok.RequiredArgsConstructor;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.config.ClusterServersConfig;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.redisson.config.TransportMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class RedissonConfig {
    @Autowired
    RedisProperties redisProperties;



    @Bean
    public RedissonClient redissonClient() throws IOException{
        Config config = new Config();
        config.setCodec(StringCodec.INSTANCE);
        config.setTransportMode(TransportMode.NIO);
        if(redisProperties.getCluster()==null){
            SingleServerConfig singleServerConfig =  config.useSingleServer().setAddress("redis://"+redisProperties.getHost()+":"
                            +redisProperties.getPort())
                    .setDatabase(redisProperties.getDatabase()).setPassword(redisProperties.getPassword());
            singleServerConfig =     singleServerConfig.setPassword(redisProperties.getPassword());

            singleServerConfig.setTimeout(10000);
            RedissonClient redissonClient = Redisson.create(config);
            return redissonClient;
        }
        List<String> clusterList = new ArrayList<String>();
        for( int i=0;i<redisProperties.getCluster().getNodes().size();i++){
            clusterList.add("redis://"+redisProperties.getCluster().getNodes().get(i));
        }
        // clusterList.add("redis://8.222.160.178:6379");






        ClusterServersConfig  clusterServersConfig = config.useClusterServers().setScanInterval(2000).addNodeAddress(clusterList.toArray(new String[clusterList.size()]));
        clusterServersConfig.setPassword(redisProperties.getPassword());
//       SingleServerConfig singleServerConfig =  config.useSingleServer().setAddress("redis://"+redisProperties.getHost()+":"
//               +redisProperties.getPort())
//               .setDatabase(redisProperties.getDatabase()).setPassword(redisProperties.getPassword());
        clusterServersConfig.setPassword(redisProperties.getPassword());
//        singleServerConfig =     singleServerConfig.setPassword(redisProperties.getPassword());
//
//       // singleServerConfig.setTimeout(10000);
        RedissonClient redissonClient = Redisson.create(config);
        return redissonClient;



    }
}
