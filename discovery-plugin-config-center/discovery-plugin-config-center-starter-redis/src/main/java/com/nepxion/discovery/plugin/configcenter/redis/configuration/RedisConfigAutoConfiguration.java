package com.nepxion.discovery.plugin.configcenter.redis.configuration;

/**
 * <p>Title: Nepxion Discovery</p>
 * <p>Description: Nepxion Discovery</p>
 * <p>Copyright: Copyright (c) 2017-2050</p>
 * <p>Company: Nepxion</p>
 * @author Haojun Ren
 * @author JiKai Sun
 * @version 1.0
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

import com.nepxion.banner.BannerConstant;
import com.nepxion.banner.Description;
import com.nepxion.banner.LogoBanner;
import com.nepxion.banner.NepxionBanner;
import com.nepxion.discovery.common.entity.SubscriptionType;
import com.nepxion.discovery.common.redis.constant.RedisConstant;
import com.nepxion.discovery.plugin.configcenter.adapter.ConfigAdapter;
import com.nepxion.discovery.plugin.configcenter.redis.adapter.RedisConfigAdapter;
import com.nepxion.discovery.plugin.framework.adapter.PluginAdapter;
import com.taobao.text.Color;

@Configuration
public class RedisConfigAutoConfiguration {
    private static final Logger LOG = LoggerFactory.getLogger(RedisConfigAutoConfiguration.class);

    static {
        /*String bannerShown = System.getProperty(BannerConstant.BANNER_SHOWN, "true");
        if (Boolean.valueOf(bannerShown)) {
            System.out.println("");
            System.out.println("╔═══╗    ╔╗");
            System.out.println("║╔═╗║    ║║");
            System.out.println("║╚═╝╠══╦═╝╠╦══╗");
            System.out.println("║╔╗╔╣║═╣╔╗╠╣══╣");
            System.out.println("║║║╚╣║═╣╚╝║╠══║");
            System.out.println("╚╝╚═╩══╩══╩╩══╝");
            System.out.println(RedisConstant.TYPE + " Config");
            System.out.println("");
        }*/

        LogoBanner logoBanner = new LogoBanner(RedisConfigAutoConfiguration.class, "/com/nepxion/redis/resource/logo.txt", "Welcome to Nepxion", 5, 5, new Color[] { Color.red, Color.green, Color.cyan, Color.blue, Color.yellow }, true);

        NepxionBanner.show(logoBanner, new Description("Config:", RedisConstant.REDIS_TYPE, 0, 1), new Description(BannerConstant.GITHUB + ":", BannerConstant.NEPXION_GITHUB + "/Discovery", 0, 1));
    }

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @Autowired
    private PluginAdapter pluginAdapter;

    @Bean
    public RedisMessageListenerContainer configMessageListenerContainer(MessageListenerAdapter partialMessageListenerAdapter, MessageListenerAdapter globalMessageListenerAdapter) {
        String group = getGroup();
        String serviceId = getServiceId();

        RedisMessageListenerContainer configMessageListenerContainer = new RedisMessageListenerContainer();
        configMessageListenerContainer.setConnectionFactory(redisConnectionFactory);
        configMessageListenerContainer.addMessageListener(partialMessageListenerAdapter, new PatternTopic(group + "-" + serviceId));
        configMessageListenerContainer.addMessageListener(globalMessageListenerAdapter, new PatternTopic(group + "-" + group));

        return configMessageListenerContainer;
    }

    @Bean
    public MessageListenerAdapter partialMessageListenerAdapter(RedisConfigAdapter configAdapter) {
        String group = getGroup();
        String serviceId = getServiceId();
        SubscriptionType subscriptionType = configAdapter.getSubscriptionType(false);
        String configType = configAdapter.getConfigType();

        LOG.info("Subscribe {} config from {} server, group={}, dataId={}", subscriptionType, configType, group, serviceId);

        return new MessageListenerAdapter(configAdapter, "subscribePartialConfig");
    }

    @Bean
    public MessageListenerAdapter globalMessageListenerAdapter(RedisConfigAdapter configAdapter) {
        String group = getGroup();
        SubscriptionType subscriptionType = configAdapter.getSubscriptionType(true);
        String configType = configAdapter.getConfigType();

        LOG.info("Subscribe {} config from {} server, group={}, dataId={}", subscriptionType, configType, group, group);

        return new MessageListenerAdapter(configAdapter, "subscribeGlobalConfig");
    }

    @Bean
    public ConfigAdapter configAdapter() {
        return new RedisConfigAdapter();
    }

    private String getGroup() {
        return pluginAdapter.getGroup();
    }

    private String getServiceId() {
        return pluginAdapter.getServiceId();
    }
}